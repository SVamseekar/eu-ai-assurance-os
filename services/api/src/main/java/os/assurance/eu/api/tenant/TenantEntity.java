package os.assurance.eu.api.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class TenantEntity {
  @Id
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String plan;

  @Column(nullable = false)
  private String dataRegion;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private String status = "ACTIVE";

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "purge_after")
  private Instant purgeAfter;

  @Column(name = "trial_ends_at")
  private Instant trialEndsAt;

  @Column(name = "dpa_accepted_at")
  private Instant dpaAcceptedAt;

  @Column(name = "dpa_version", length = 16)
  private String dpaVersion;

  protected TenantEntity() {
  }

  public TenantEntity(UUID id, String name, String plan, String dataRegion, Instant createdAt) {
    this.id = id;
    this.name = name;
    this.plan = plan;
    this.dataRegion = dataRegion;
    this.createdAt = createdAt;
  }

  public UUID id() {
    return id;
  }

  public String name() {
    return name;
  }

  public String plan() {
    return plan;
  }

  public String dataRegion() {
    return dataRegion;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public String status() {
    return status;
  }

  public Instant purgeAfter() {
    return purgeAfter;
  }

  public Instant trialEndsAt() {
    return trialEndsAt;
  }

  public void setTrialEndsAt(Instant trialEndsAt) {
    this.trialEndsAt = trialEndsAt;
  }

  public boolean active() {
    return "ACTIVE".equals(status);
  }

  public void scheduleDeletion(Instant now, Instant purgeAt) {
    this.status = "DELETION_PENDING";
    this.deletedAt = now;
    this.purgeAfter = purgeAt;
  }

  public Instant dpaAcceptedAt() {
    return dpaAcceptedAt;
  }

  public String dpaVersion() {
    return dpaVersion;
  }

  public void acceptDpa(String version, Instant at) {
    this.dpaVersion = version;
    this.dpaAcceptedAt = at;
  }

  public void setPlan(String plan) {
    this.plan = plan;
  }
}
