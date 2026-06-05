package os.assurance.eu.api.evidence;

import java.util.Arrays;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class EvidenceEmbeddingService {
  static final int DIMENSIONS = 64;

  public String embed(String text) {
    double[] vector = vectorize(text);
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < vector.length; i++) {
      if (i > 0) {
        builder.append(',');
      }
      builder.append(vector[i]);
    }
    return builder.toString();
  }

  public double similarity(String left, String right) {
    return cosine(parse(left), parse(right));
  }

  private double[] vectorize(String text) {
    double[] vector = new double[DIMENSIONS];
    tokenize(text).forEach(token -> {
      int index = Math.floorMod(token.hashCode(), DIMENSIONS);
      vector[index] += 1.0;
    });
    normalize(vector);
    return vector;
  }

  private java.util.stream.Stream<String> tokenize(String text) {
    return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
        .filter(token -> token.length() > 2);
  }

  private double[] parse(String embedding) {
    double[] vector = new double[DIMENSIONS];
    String[] parts = embedding.split(",");
    for (int i = 0; i < Math.min(parts.length, DIMENSIONS); i++) {
      vector[i] = Double.parseDouble(parts[i]);
    }
    return vector;
  }

  private void normalize(double[] vector) {
    double magnitude = Math.sqrt(Arrays.stream(vector).map(value -> value * value).sum());
    if (magnitude == 0) {
      return;
    }
    for (int i = 0; i < vector.length; i++) {
      vector[i] = vector[i] / magnitude;
    }
  }

  private double cosine(double[] left, double[] right) {
    double dot = 0;
    for (int i = 0; i < Math.min(left.length, right.length); i++) {
      dot += left[i] * right[i];
    }
    return dot;
  }
}
