package os.assurance.eu.api.evidence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EvidenceChunker {
  private static final int TARGET_WORDS = 90;
  private static final int OVERLAP_WORDS = 15;
  private static final int MAX_CHUNK_CHARACTERS = 7_800;

  public List<ChunkDraft> chunk(String text) {
    String[] words = text.replaceAll("\\s+", " ").strip().split(" ");
    List<ChunkDraft> chunks = new ArrayList<>();
    if (words.length == 0 || words[0].isBlank()) {
      return List.of(new ChunkDraft("Section 1", "No extractable text was found."));
    }
    int ordinal = 1;
    for (int start = 0; start < words.length; start += TARGET_WORDS - OVERLAP_WORDS) {
      int end = Math.min(words.length, start + TARGET_WORDS);
      String content = String.join(" ", Arrays.asList(words).subList(start, end));
      for (String part : splitByCharacters(content)) {
        chunks.add(new ChunkDraft("Section %d".formatted(ordinal++), part));
      }
      if (end == words.length) {
        break;
      }
    }
    return chunks;
  }

  private List<String> splitByCharacters(String content) {
    if (content.length() <= MAX_CHUNK_CHARACTERS) {
      return List.of(content);
    }
    List<String> parts = new ArrayList<>();
    for (int start = 0; start < content.length(); start += MAX_CHUNK_CHARACTERS) {
      parts.add(content.substring(start, Math.min(content.length(), start + MAX_CHUNK_CHARACTERS)));
    }
    return parts;
  }

  public record ChunkDraft(String sectionRef, String content) {
  }
}
