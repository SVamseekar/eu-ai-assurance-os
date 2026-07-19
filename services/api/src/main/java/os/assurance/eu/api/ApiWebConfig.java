package os.assurance.eu.api;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiWebConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    String[] localOrigins = {
        "http://localhost:3000",
        "http://localhost:4173",
        "http://localhost:5173",
        "http://localhost:8000",
        "http://127.0.0.1:3000",
        "http://127.0.0.1:4173",
        "http://127.0.0.1:5173",
        "http://127.0.0.1:8000"
    };
    registry.addMapping("/api/**")
        .allowedOrigins(localOrigins)
        .allowedMethods("GET", "POST", "PUT", "PATCH", "OPTIONS")
        .allowedHeaders("*");
    // OAuth start may be hit from the dashboard origin before redirecting to the IdP.
    registry.addMapping("/auth/**")
        .allowedOrigins(localOrigins)
        .allowedMethods("GET", "POST", "OPTIONS")
        .allowedHeaders("*");
  }

  @Bean
  @ConditionalOnMissingBean(Clock.class)
  Clock clock() {
    return Clock.systemUTC();
  }
}
