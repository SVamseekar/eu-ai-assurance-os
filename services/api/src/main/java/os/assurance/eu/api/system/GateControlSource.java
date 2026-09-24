package os.assurance.eu.api.system;

import java.util.List;
import java.util.UUID;

public interface GateControlSource {
  List<GateControl> acceptedLinks(UUID systemId);
}
