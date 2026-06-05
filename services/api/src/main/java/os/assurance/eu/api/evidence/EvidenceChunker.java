package os.assurance.eu.api.evidence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EvidenceChunker {
  private static final int TARGET_WORDS = 90;
  private static final int OVERLAP_WORDS = 15;

  public List<ChunkDraft> chunk(String text) {
    String[] words = text.replaceAll("\\s+", " ").strip().split(" ");
    List<ChunkDraft> chunks = new ArrayList<>();
    if (words.length == 0 || words[0].isBlank()) {
      return List.of(new ChunkDraft("Section 1", "No extractable text was found."));
    }
    int ordinal = 1;
    for (int start = 0; start < words.length; start += TARGET_WORDS - OVERLAP_WORDS) {
      int end = Math.min(words.length, start + TARGET_WORDS);
      chunks.add(new ChunkDraft("Section %d".formatted(ordinal++), String.join(" ", Arrays.asList(words).subList(start, end))));
      if (end == words.length) {
        break;
      }
    }
    return chunks;
  }

  public record ChunkDraft(String sectionRef, String content) {
  }
}
