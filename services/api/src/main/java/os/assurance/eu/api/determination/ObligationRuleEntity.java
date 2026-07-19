package os.assurance.eu.api.determination;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import os.assurance.eu.api.persistence.JsonMapConverter;
import os.assurance.eu.api.persistence.StringListConverter;

@Entity
@Table(name = "obligation_rules")
public class ObligationRuleEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private String code;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private String legalRefs;

  @Convert(converter = JsonMapConverter.class)
  @Column(nullable = false, columnDefinition = "text")
  private Map<String, Object> appliesWhen;

  @Column(nullable = false)
  private String severity;

  @Convert(converter = StringListConverter.class)
  @Column(nullable = false, columnDefinition = "text")
  private List<String> controlCodes;

  @Column(nullable = false)
  private String rulesetVersion;

  @Column(nullable = false)
  private boolean active;

  protected ObligationRuleEntity() {
  }

  public ObligationRule toDomain() {
    return new ObligationRule(
        id, code, title, description, legalRefs, appliesWhen, severity, controlCodes, rulesetVersion, active);
  }
}
