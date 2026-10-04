package os.assurance.eu.api.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class SmtpFailoverEmailSender implements EmailSender {
  private static final Logger log = LoggerFactory.getLogger(SmtpFailoverEmailSender.class);
  private final JavaMailSender primary;
  private final JavaMailSender fallback;
  private final String from;

  public SmtpFailoverEmailSender(JavaMailSender primary, JavaMailSender fallback, String from) {
    this.primary = primary;
    this.fallback = fallback;
    this.from = from;
  }

  @Override
  public void send(EmailMessage message) {
    SimpleMailMessage mail = new SimpleMailMessage();
    mail.setFrom(from);
    mail.setTo(message.to());
    mail.setSubject(message.subject());
    mail.setText(message.textBody());
    try {
      primary.send(mail);
      return;
    } catch (MailException primaryError) {
      log.warn("Primary SMTP failed ({}); trying fallback", primaryError.getMessage());
    }
    try {
      if (fallback == null) {
        throw new IllegalStateException("No fallback SMTP configured");
      }
      fallback.send(mail);
    } catch (RuntimeException fallbackError) {
      throw new IllegalStateException("Email delivery failed on primary and fallback", fallbackError);
    }
  }
}
