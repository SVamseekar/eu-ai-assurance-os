package os.assurance.eu.api.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import os.assurance.eu.api.audit.AuditService;

/** Builds a zip with one JSON file per tenant table, read with the same predicates the purge uses. */
@Service
public class WorkspaceExportService {
  private static final Set<String> SECRET_COLUMNS = Set.of("password_hash", "key_hash", "token_hash", "replaced_by_token_hash");

  private final JdbcTemplate jdbc;
  private final ObjectMapper json;
  private final AuditService audit;
  private final Clock clock;

  public WorkspaceExportService(JdbcTemplate jdbc, ObjectMapper json, AuditService audit, Clock clock) {
    this.jdbc = jdbc;
    this.json = json;
    this.audit = audit;
    this.clock = clock;
  }

  @Transactional
  public byte[] export(UUID tenantId) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
      write(zip, "README.txt", ("Assurance OS workspace export. One JSON file per table. Generated "
          + clock.instant() + ".\n").getBytes(StandardCharsets.UTF_8));
      for (String delete : WorkspacePurgeJob.PURGE_SQL) {
        String select = delete.replaceFirst("^delete from", "select * from");
        String table = delete.split("\\s+")[2];
        List<Map<String, Object>> rows = jdbc.queryForList(select, tenantId).stream()
            .map(WorkspaceExportService::withoutSecrets)
            .toList();
        write(zip, table + ".json", json.writerWithDefaultPrettyPrinter().writeValueAsBytes(rows));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    audit.append(null, "account.exported", "tenant", tenantId.toString(), Map.of());
    return bytes.toByteArray();
  }

  private static Map<String, Object> withoutSecrets(Map<String, Object> row) {
    Map<String, Object> clean = new LinkedHashMap<>();
    row.forEach((column, value) -> {
      String name = column.toLowerCase(Locale.ROOT);
      if (!SECRET_COLUMNS.contains(name)) {
        clean.put(name, value);
      }
    });
    return clean;
  }

  private static void write(ZipOutputStream zip, String name, byte[] content) throws IOException {
    zip.putNextEntry(new ZipEntry(name));
    zip.write(content);
    zip.closeEntry();
  }
}
