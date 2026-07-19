package os.assurance.eu.api.readiness;

/**
 * One weighted readiness dimension (0–100 local score before weight application).
 */
public record ReadinessDimensionScore(
    String code,
    String label,
    int weight,
    int score,
    int weightedPoints,
    String status,
    String summary) {
}
