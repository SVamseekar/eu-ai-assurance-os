package os.assurance.eu.api;

import static org.assertj.core.api.Assertions.assertThat;

import os.assurance.eu.api.system.AiSystemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("postgres")
@EnabledIfEnvironmentVariable(named = "RUN_POSTGRES_SMOKE", matches = "true")
class PostgresProfileSmokeTest {
  @Autowired
  private AiSystemRepository systems;

  @Test
  void startsWithPostgresProfileAndRunsFlywayBootstrap() {
    assertThat(systems.findAll()).isNotEmpty();
  }
}
