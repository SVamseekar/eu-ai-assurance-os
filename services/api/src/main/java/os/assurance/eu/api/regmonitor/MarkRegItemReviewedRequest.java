package os.assurance.eu.api.regmonitor;

import jakarta.validation.constraints.Size;

public record MarkRegItemReviewedRequest(
    @Size(max = 2048) String notes) {
}
