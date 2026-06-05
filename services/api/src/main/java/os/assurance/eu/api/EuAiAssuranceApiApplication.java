package os.assurance.eu.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EuAiAssuranceApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(EuAiAssuranceApiApplication.class, args);
  }
}
