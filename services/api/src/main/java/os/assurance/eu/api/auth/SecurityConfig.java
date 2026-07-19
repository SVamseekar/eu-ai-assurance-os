package os.assurance.eu.api.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security is deliberately {@code permitAll} for all HTTP routes.
 *
 * <p>Authentication and tenant binding are owned by {@code TenantContextFilter}
 * (Bearer JWT or {@code X-Api-Key}). Role checks use {@code TenantAuthorizationService}.
 * Do not add a second parallel security model without migrating the filter.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            // Intentional: TenantContextFilter is the authentication gate (see class Javadoc).
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .build();
    }
}