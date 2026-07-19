package os.assurance.eu.api.regmonitor;

import java.util.List;

public record RegMonitorFeedResponse(
    String productLabel,
    String disclaimer,
    String latencyNote,
    List<RegItemResponse> items) {
}
