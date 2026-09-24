package os.assurance.eu.api.corpus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1100)
@ConditionalOnProperty(name = "assurance.corpus.bootstrap", havingValue = "true", matchIfMissing = true)
public class CorpusBootstrapRunner implements CommandLineRunner {
  private static final Logger LOGGER = LoggerFactory.getLogger(CorpusBootstrapRunner.class);
  private final CorpusIngestService ingestService;

  public CorpusBootstrapRunner(CorpusIngestService ingestService) {
    this.ingestService = ingestService;
  }

  @Override
  public void run(String... args) {
    String version = ingestService.ingestClasspath();
    LOGGER.info("Corpus version {}", version);
  }
}
