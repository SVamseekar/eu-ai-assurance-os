package os.assurance.eu.api.sector;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * HR / employment vertical — hiring, ranking, and worker management themes.
 */
@Component
public class HrSectorPack implements SectorPack {
  @Override
  public String id() {
    return "hr";
  }

  @Override
  public String displayName() {
    return "HR / Employment";
  }

  @Override
  public String summary() {
    return "Hiring & ranking transparency plus employment human oversight";
  }

  @Override
  public Set<String> sectorKeys() {
    return Set.of("hr", "employment", "human_resources", "recruiting", "recruitment");
  }

  @Override
  public List<SectorControlDef> extraControls() {
    return List.of(
        new SectorControlDef(
            "HR_HIRING_TRANSPARENCY",
            "Hiring and ranking transparency",
            "Document ranking criteria, feature sources, and how candidates or workers can "
                + "obtain meaningful information about automated scoring used in hiring or promotion.",
            "HIGH",
            "TRANSPARENCY"),
        new SectorControlDef(
            "HR_HUMAN_OVERSIGHT_EMPLOYMENT",
            "Human oversight for employment decisions",
            "Ensure qualified human reviewers can override automated shortlists, rejections, "
                + "or performance flags before material employment outcomes.",
            "HIGH",
            "OVERSIGHT"),
        new SectorControlDef(
            "HR_CANDIDATE_NOTICE",
            "Candidate notice of AI screening",
            "Inform candidates and workers when AI systems are used for recruitment, screening, "
                + "or evaluation, with a contact path for questions.",
            "HIGH,LIMITED",
            "TRANSPARENCY"));
  }

  @Override
  public Map<String, Object> questionnaireDefaults() {
    return Map.of(
        "sector", "hr",
        "employment", true,
        "decision_impact", "employment",
        "profiling", true,
        "interacts_with_natural_persons", true);
  }

  @Override
  public List<SampleEvidenceTemplate> sampleEvidenceTemplates() {
    return List.of(
        new SampleEvidenceTemplate(
            "hr-transparency-notice",
            "Candidate AI screening notice",
            "TRANSPARENCY_NOTICE",
            "sector/hr/candidate-notice-template.md",
            "Sample notice for AI-assisted recruitment screening"),
        new SampleEvidenceTemplate(
            "hr-oversight-sop",
            "Employment AI human oversight SOP",
            "SOP",
            "sector/hr/oversight-sop-template.md",
            "Sample human oversight SOP for hiring / ranking systems"));
  }
}
