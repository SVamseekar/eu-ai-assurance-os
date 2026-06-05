package os.assurance.eu.api.evidence;

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

  public SanitizedText sanitizeDocumentText(String text) {
    List<String> safeLines = new ArrayList<>();
    List<String> removed = new ArrayList<>();
    for (String line : text.lines().toList()) {
      if (looksLikeInjection(line)) {
        removed.add(line.strip());
      } else {
        safeLines.add(line);
      }
    }
    String sanitized = String.join("\n", safeLines).strip();
    return new SanitizedText(sanitized.isBlank() ? "Document text removed by prompt-injection guard." : sanitized, removed);
  }

  public String sanitizeQuestion(String question) {
    return question == null ? "" : question.replaceAll("\\s+", " ").strip();
  }

  private boolean looksLikeInjection(String line) {
    String lower = line.toLowerCase(Locale.ROOT);
    return DOCUMENT_INJECTION_PATTERNS.stream().anyMatch(lower::contains);
  }

  public record SanitizedText(String text, List<String> removedLines) {
  }
}
