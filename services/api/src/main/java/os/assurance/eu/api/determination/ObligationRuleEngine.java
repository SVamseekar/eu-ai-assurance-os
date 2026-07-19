package os.assurance.eu.api.determination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Deterministic pure-Java evaluator over obligation_rules.applies_when JSON.
 * No LLM required for v1.
 *
 * <p>Condition grammar (map nodes):
 * <ul>
 *   <li>{@code applicableIf} / {@code uncertainIf} — group evaluated with match()</li>
 *   <li>Group: {@code all} | {@code any} list of leaf conditions or nested groups</li>
 *   <li>Leaf: {@code field}, {@code op} (eq|neq|in|not_in), {@code value}</li>
 * </ul>
 */
@Component
public class ObligationRuleEngine {

  public List<DeterminationObligation> evaluate(
      List<ObligationRule> rules, Map<String, Object> answers) {
    Map<String, Object> normalized = normalizeAnswers(answers);
    List<DeterminationObligation> out = new ArrayList<>();
    for (ObligationRule rule : rules) {
      if (!rule.active()) {
        continue;
      }
      Applicability applicability = evaluateRule(rule.appliesWhen(), normalized);
      String rationale = buildRationale(rule, applicability, normalized);
      out.add(new DeterminationObligation(
          null,
          rule.code(),
          rule.title(),
          applicability,
          rationale,
          rule.controlCodes() == null ? List.of() : List.copyOf(rule.controlCodes()),
          rule.legalRefs(),
          rule.severity()));
    }
    return out;
  }

  Applicability evaluateRule(Map<String, Object> appliesWhen, Map<String, Object> answers) {
    if (appliesWhen == null || appliesWhen.isEmpty()) {
      return Applicability.NOT_APPLICABLE;
    }
    Object uncertainNode = appliesWhen.get("uncertainIf");
    if (uncertainNode instanceof Map<?, ?> uncertainMap && matchGroup(castMap(uncertainMap), answers)) {
      return Applicability.UNCERTAIN;
    }
    Object applicableNode = appliesWhen.get("applicableIf");
    if (applicableNode instanceof Map<?, ?> applicableMap && matchGroup(castMap(applicableMap), answers)) {
      return Applicability.APPLICABLE;
    }
    // Empty applicableIf.all means always applicable (baseline rules).
    if (applicableNode instanceof Map<?, ?> applicableMap) {
      Map<String, Object> group = castMap(applicableMap);
      Object all = group.get("all");
      if (all instanceof Collection<?> col && col.isEmpty() && !group.containsKey("any")) {
        return Applicability.APPLICABLE;
      }
    }
    return Applicability.NOT_APPLICABLE;
  }

  @SuppressWarnings("unchecked")
  private boolean matchGroup(Map<String, Object> group, Map<String, Object> answers) {
    if (group == null || group.isEmpty()) {
      return false;
    }
    if (group.containsKey("all")) {
      Object all = group.get("all");
      if (!(all instanceof Collection<?> conditions)) {
        return false;
      }
      if (conditions.isEmpty()) {
        return true;
      }
      for (Object item : conditions) {
        if (!matchNode(item, answers)) {
          return false;
        }
      }
      return true;
    }
    if (group.containsKey("any")) {
      Object any = group.get("any");
      if (!(any instanceof Collection<?> conditions) || conditions.isEmpty()) {
        return false;
      }
      for (Object item : conditions) {
        if (matchNode(item, answers)) {
          return true;
        }
      }
      return false;
    }
    // bare leaf condition stored as map with field/op
    if (group.containsKey("field")) {
      return matchLeaf(group, answers);
    }
    return false;
  }

  private boolean matchNode(Object node, Map<String, Object> answers) {
    if (!(node instanceof Map<?, ?> raw)) {
      return false;
    }
    Map<String, Object> map = castMap(raw);
    if (map.containsKey("all") || map.containsKey("any")) {
      return matchGroup(map, answers);
    }
    return matchLeaf(map, answers);
  }

  private boolean matchLeaf(Map<String, Object> leaf, Map<String, Object> answers) {
    String field = stringVal(leaf.get("field"));
    if (field.isBlank()) {
      return false;
    }
    String op = stringVal(leaf.get("op")).toLowerCase(Locale.ROOT);
    Object expected = leaf.get("value");
    Object actual = answers.get(field);

    return switch (op) {
      case "eq" -> equalsNormalized(actual, expected);
      case "neq" -> !equalsNormalized(actual, expected);
      case "in" -> inCollection(actual, expected);
      case "not_in" -> !inCollection(actual, expected);
      default -> false;
    };
  }

  private boolean inCollection(Object actual, Object expected) {
    if (!(expected instanceof Collection<?> collection)) {
      return equalsNormalized(actual, expected);
    }
    for (Object item : collection) {
      if (equalsNormalized(actual, item)) {
        return true;
      }
    }
    return false;
  }

  private boolean equalsNormalized(Object actual, Object expected) {
    if (actual == null && expected == null) {
      return true;
    }
    if (actual == null || expected == null) {
      return false;
    }
    if (actual instanceof Boolean || expected instanceof Boolean) {
      return Objects.equals(asBoolean(actual), asBoolean(expected));
    }
    if (actual instanceof Number && expected instanceof Number) {
      return ((Number) actual).doubleValue() == ((Number) expected).doubleValue();
    }
    return normalizeToken(actual).equals(normalizeToken(expected));
  }

  private Boolean asBoolean(Object value) {
    if (value instanceof Boolean b) {
      return b;
    }
    String s = normalizeToken(value);
    if ("true".equals(s) || "yes".equals(s) || "1".equals(s)) {
      return true;
    }
    if ("false".equals(s) || "no".equals(s) || "0".equals(s)) {
      return false;
    }
    return null;
  }

  private String normalizeToken(Object value) {
    return Objects.toString(value, "").trim().toLowerCase(Locale.ROOT);
  }

  private String stringVal(Object value) {
    return value == null ? "" : value.toString().trim();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> castMap(Map<?, ?> raw) {
    Map<String, Object> out = new LinkedHashMap<>();
    for (Map.Entry<?, ?> e : raw.entrySet()) {
      if (e.getKey() != null) {
        out.put(e.getKey().toString(), e.getValue());
      }
    }
    return out;
  }

  private Map<String, Object> normalizeAnswers(Map<String, Object> answers) {
    Map<String, Object> out = new LinkedHashMap<>();
    if (answers == null) {
      return out;
    }
    for (Map.Entry<String, Object> e : answers.entrySet()) {
      if (e.getKey() == null) {
        continue;
      }
      String key = e.getKey().trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
      Object value = e.getValue();
      if (value instanceof String s) {
        String token = s.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(token) || "false".equals(token)) {
          out.put(key, Boolean.parseBoolean(token));
        } else if ("unknown".equals(token)) {
          out.put(key, "unknown");
        } else {
          out.put(key, token);
        }
      } else {
        out.put(key, value);
      }
    }
    return out;
  }

  private String buildRationale(
      ObligationRule rule, Applicability applicability, Map<String, Object> answers) {
    return switch (applicability) {
      case APPLICABLE ->
          "Suggested applicable based on questionnaire answers and rule "
              + rule.code()
              + ". Not a legal determination.";
      case UNCERTAIN ->
          "Insufficient or unknown answers for rule "
              + rule.code()
              + "; human legal review required.";
      case NOT_APPLICABLE ->
          "Suggested not applicable based on questionnaire answers for rule " + rule.code() + ".";
    };
  }
}
