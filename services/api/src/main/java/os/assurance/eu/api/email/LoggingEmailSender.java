package os.assurance.eu.api.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Local/test sender: logs instead of sending. Never used when assurance.email.mode=smtp. */
public class LoggingEmailSender implements EmailSender {
  private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

  @Override
  public void send(EmailMessage message) {
    log.info("EMAIL to={} subject={}\n{}", message.to(), message.subject(), message.textBody());
  }
}
