package os.assurance.eu.api.evidence;

import java.util.List;

public record EvidenceQueryResponse(String answer, double confidence, List<Citation> citations) {
}
