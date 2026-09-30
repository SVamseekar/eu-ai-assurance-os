package os.assurance.eu.api.proposal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateMappingProposalRequest(
    @NotBlank
    @Pattern(regexp = "supports|overlaps|tension_candidate|abstain")
    String relation,
    @jakarta.validation.constraints.Size(max = 160) String provisionKey,
    @jakarta.validation.constraints.Size(max = 4000) String excerpt,
    @jakarta.validation.constraints.Size(max = 64) String corpusVersion,
    @jakarta.validation.constraints.Size(max = 64) String adapterVersion) {
}
