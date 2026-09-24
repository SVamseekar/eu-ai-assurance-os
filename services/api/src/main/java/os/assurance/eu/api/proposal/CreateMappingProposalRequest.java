package os.assurance.eu.api.proposal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateMappingProposalRequest(
    @NotBlank
    @Pattern(regexp = "supports|overlaps|tension_candidate|abstain")
    String relation,
    String provisionKey,
    String excerpt,
    String corpusVersion,
    String adapterVersion) {
}
