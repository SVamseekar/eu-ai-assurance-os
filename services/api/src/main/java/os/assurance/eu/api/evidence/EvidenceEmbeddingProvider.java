package os.assurance.eu.api.evidence;

public interface EvidenceEmbeddingProvider {
  String name();

  String embed(String text);

  double similarity(String left, String right);
}
