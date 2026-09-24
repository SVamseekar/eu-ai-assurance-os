package os.assurance.eu.api.corpus;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public final class CorpusVersionHasher {
  private CorpusVersionHasher() {
  }

  public record InstrumentLine(
      String celex,
      String consolidationDate,
      String textHash,
      String applicationFrom) {
  }

  public record GuidanceLine(String sourceKey, String textHash) {
  }

  public static String hash(List<InstrumentLine> instruments, List<GuidanceLine> guidance) {
    List<String> lines = new ArrayList<>();
    for (InstrumentLine instrument : instruments) {
      lines.add(String.join(
          "|",
          "instrument",
          instrument.celex(),
          instrument.consolidationDate(),
          instrument.textHash(),
          instrument.applicationFrom()));
    }
    for (GuidanceLine document : guidance) {
      lines.add(String.join("|", "guidance", document.sourceKey(), document.textHash()));
    }
    lines.sort(String::compareTo);
    return sha256(String.join("\n", lines));
  }

  public static String sha256(String value) {
    return sha256(value.getBytes(StandardCharsets.UTF_8));
  }

  public static String sha256(byte[] value) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(value);
      return HexFormat.of().formatHex(digest);
    } catch (Exception ex) {
      throw new IllegalStateException("SHA-256 is not available", ex);
    }
  }
}
