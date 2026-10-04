package os.assurance.eu.api.billing;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "gate_run_counters")
@IdClass(GateRunCounterEntity.Key.class)
public class GateRunCounterEntity {
  public static class Key implements Serializable {
    private UUID tenantId;
    private String period;

    public Key() {
    }

    public Key(UUID tenantId, String period) {
      this.tenantId = tenantId;
      this.period = period;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Key k && Objects.equals(tenantId, k.tenantId) && Objects.equals(period, k.period);
    }

    @Override
    public int hashCode() {
      return Objects.hash(tenantId, period);
    }
  }

  @Id
  private UUID tenantId;

  @Id
  private String period;

  private int runs;

  protected GateRunCounterEntity() {
  }

  public GateRunCounterEntity(UUID tenantId, String period, int runs) {
    this.tenantId = tenantId;
    this.period = period;
    this.runs = runs;
  }

  public int runs() { return runs; }

  public void increment() { runs++; }
}
