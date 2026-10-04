package os.assurance.eu.api.email;

import java.time.LocalDate;

public final class EmailTemplates {
  private static final String FOOTER =
      "\n\n--\nAssurance OS · evidence and release gates for AI systems · not legal advice\n";

  private EmailTemplates() {
  }

  public static EmailMessage verifyEmail(String link) {
    return new EmailMessage("", "Confirm your email for Assurance OS",
        "Confirm your email address to finish creating your workspace:\n\n" + link
            + "\n\nThe link works once and expires in 24 hours. If you did not sign up, ignore this email." + FOOTER);
  }

  public static EmailMessage resetPassword(String link) {
    return new EmailMessage("", "Reset your Assurance OS password",
        "Someone asked to reset the password for this email address. To choose a new password, open:\n\n" + link
            + "\n\nThe link works once and expires in 1 hour. If this was not you, ignore this email; "
            + "your password has not changed." + FOOTER);
  }

  public static EmailMessage workspaceDeletionScheduled(String orgName, LocalDate purgeOn) {
    return new EmailMessage("", "Your Assurance OS workspace is scheduled for deletion",
        "The workspace \"" + orgName + "\" was deleted by an administrator. Access has ended. All workspace data "
            + "will be permanently erased on " + purgeOn + ". To cancel, reply to this email before that date." + FOOTER);
  }
}
