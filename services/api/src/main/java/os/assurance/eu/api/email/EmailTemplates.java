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

  public static EmailMessage accountExists(String resetLink) {
    return new EmailMessage("", "You already have an Assurance OS account",
        "Someone tried to sign up with this email address, which already has an account. Nothing was changed.\n\n"
            + "If it was you, sign in as usual, or choose a new password here:\n\n" + resetLink
            + "\n\nThe link works once and expires in 1 hour. If it was not you, ignore this email." + FOOTER);
  }

  public static EmailMessage passwordChanged() {
    return new EmailMessage("", "Your Assurance OS password was changed",
        "The password for this account was just changed and all signed-in sessions were ended. "
            + "If this was not you, reset your password again straight away and contact your workspace administrator."
            + FOOTER);
  }

  public static EmailMessage workspaceDeletionScheduled(String orgName, LocalDate purgeOn) {
    return new EmailMessage("", "Your Assurance OS workspace is scheduled for deletion",
        "The workspace \"" + orgName + "\" was deleted by an administrator. Access has ended. All workspace data "
            + "will be permanently erased on " + purgeOn + ". To cancel, reply to this email before that date." + FOOTER);
  }
}
