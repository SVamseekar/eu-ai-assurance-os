package os.assurance.eu.api.control;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "controls")
public class ControlEntity {
  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private String appliesToRiskClass;

  @Column(nullable = false)
  private String category;

  protected ControlEntity() {}

  public ControlEntity(Control control) {
    this.id = control.id();
    this.code = control.code();
    this.name = control.name();
    this.description = control.description();
    this.appliesToRiskClass = control.appliesToRiskClass();
    this.category = control.category();
  }

  public Control toDomain() {
    return new Control(id, code, name, description, appliesToRiskClass, category);
  }
}
