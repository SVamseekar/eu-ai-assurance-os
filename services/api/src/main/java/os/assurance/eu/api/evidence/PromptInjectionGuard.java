package os.assurance.eu.api.evidence;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class PromptInjectionGuard {
  private static final List<String> DOCUMENT_INJECTION_PATTERNS = List.of(
      "ignore previous",
      "ignore all previous",
      "system prompt",
      "developer message",
      "reveal secret",
      "disable citation",
      "do not cite",
      "bypass policy",
      "override instruction");

  private static final String ZERO_WIDTH_CHARACTERS = "[\\u200B-\\u200F\\uFEFF]";

  public SanitizedText sanitizeDocumentText(String text) {
    List<String> lines = text.lines().toList();
    List<String> safeLines = new ArrayList<>();
    List<String> removed = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (looksLikeInjection(normalize(line))) {
        removed.add(line.strip());
        continue;
      }
      if (i + 1 < lines.size()) {
        String next = lines.get(i + 1);
        if (pairIntroducesNewPattern(line, next)) {
          removed.add(line.strip());
          if (!looksLikeInjection(normalize(next))) {
            removed.add(next.strip());
            i++;
          }
          continue;
        }
      }
      safeLines.add(line);
    }
    String sanitized = String.join("\n", safeLines).strip();

    String collapsedWholeText = normalize(text).replaceAll("\\s+", " ");
    if (looksLikeInjection(collapsedWholeText) && !removed.isEmpty()) {
      // A phrase was already caught above — no extra action needed for that case.
    } else if (looksLikeInjection(collapsedWholeText)) {
      return new SanitizedText("Document text removed by prompt-injection guard.", List.of(text.strip()));
    }

    return new SanitizedText(sanitized.isBlank() ? "Document text removed by prompt-injection guard." : sanitized, removed);
  }

  public String sanitizeQuestion(String question) {
    return question == null ? "" : question.replaceAll("\\s+", " ").strip();
  }

  private String normalize(String input) {
    String normalized = Normalizer.normalize(input, Normalizer.Form.NFKC);
    return normalized
        .replaceAll(ZERO_WIDTH_CHARACTERS, " ")
        .replaceAll("\\s+", " ")
        .toLowerCase(Locale.ROOT)
        .strip();
  }

  private boolean looksLikeInjection(String normalizedLowercaseText) {
    return DOCUMENT_INJECTION_PATTERNS.stream().anyMatch(normalizedLowercaseText::contains);
  }

  private boolean pairIntroducesNewPattern(String line, String next) {
    String normalizedLine = normalize(line);
    String normalizedNext = normalize(next);
    String pair = normalizedLine + " " + normalizedNext;
    if (!looksLikeInjection(pair)) {
      return false;
    }
    if (!looksLikeInjection(normalizedLine) && !looksLikeInjection(normalizedNext)) {
      return true;
    }
    for (String pattern : DOCUMENT_INJECTION_PATTERNS) {
      if (pair.contains(pattern) && !normalizedLine.contains(pattern) && !normalizedNext.contains(pattern)) {
        return true;
      }
    }
    return false;
  }

  public record SanitizedText(String text, List<String> removedLines) {
  }
}