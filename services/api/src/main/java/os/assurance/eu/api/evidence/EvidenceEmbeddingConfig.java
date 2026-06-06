package os.assurance.eu.api.evidence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EvidenceEmbeddingConfig {
    private static final Logger log = LoggerFactory.getLogger(EvidenceEmbeddingConfig.class);

    @Bean
    @Primary
    @ConditionalOnProperty(name = "assurance.evidence.embedding-provider", havingValue = "djl-sentence")
    public EvidenceEmbeddingProvider djlSentenceEmbeddingProvider() throws Exception {
        log.info("Loading DJL sentence-transformer embedding provider (all-MiniLM-L6-v2)");
        return new DjlSentenceEmbeddingProvider();
    }
}
