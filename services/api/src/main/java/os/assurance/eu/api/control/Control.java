package os.assurance.eu.api.control;

import java.util.UUID;

public record Control(
    UUID id,
    String code,
    String name,
    String description,
    String appliesToRiskClass,
    String category) {
}
