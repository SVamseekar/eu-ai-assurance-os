package os.assurance.eu.api.tenant;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Records when a key was last used, at most once per key every few minutes so busy CI stays cheap. */
@Component
public class ApiKeyUsageRecorder {
    static final Duration MIN_INTERVAL = Duration.ofMinutes(5);

    private final ConcurrentHashMap<UUID, Instant> lastWritten = new ConcurrentHashMap<>();
    private final ApiKeyJpaRepository keys;
    private final Clock clock;
    private final TransactionTemplate transaction;

    public ApiKeyUsageRecorder(ApiKeyJpaRepository keys, Clock clock, PlatformTransactionManager transactionManager) {
        this.keys = keys;
        this.clock = clock;
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void touch(UUID keyId) {
        Instant now = clock.instant();
        Instant previous = lastWritten.get(keyId);
        if (previous != null && previous.plus(MIN_INTERVAL).isAfter(now)) {
            return;
        }
        lastWritten.put(keyId, now);
        try {
            transaction.executeWithoutResult(status -> keys.touch(keyId, now));
        } catch (RuntimeException e) {
            lastWritten.remove(keyId); // usage stamp is best-effort; never fail the request over it
        }
    }
}
