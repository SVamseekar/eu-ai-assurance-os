package os.assurance.eu.api.sector;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Webhook-style stub payload mapping an external claims model into registry fields.
 * Does not call proprietary vendor APIs.
 */
public record ClaimsModelRegisterRequest(
    @NotBlank String externalModelId,
    @NotBlank String name,
    String owner,
    String purpose,
    String vendorName,
    String modelName,
    String modelVersion,
    List<String> dataSources,
    String decisionImpact) {
}
