package os.assurance.eu.api.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

/**
 * Postgres has a unique index on {@code lower(email)}. Spring's derived {@code IgnoreCase} queries compare
 * {@code upper(email)}, which that index cannot serve, so every login and signup would scan the users table.
 * The lookups must therefore compare {@code lower(email)} explicitly.
 */
class UserEmailLookupIndexTest {
  @Test
  void caseInsensitiveLookupsCompareLowerEmailSoThePostgresIndexIsUsed() throws Exception {
    for (String method : new String[] {"findByEmailIgnoreCase", "existsByEmailIgnoreCase"}) {
      Query query = UserJpaRepository.class.getMethod(method, String.class).getAnnotation(Query.class);
      assertThat(query).as(method + " has an explicit query").isNotNull();
      assertThat(query.value().toLowerCase()).contains("lower(u.email) = lower(:email)");
    }
  }
}
