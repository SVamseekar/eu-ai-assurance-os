package os.assurance.eu.api.email;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Runs work once the surrounding transaction has committed, or at once when there is none. Emails go through
 * this so a slow mail server does not hold database locks, and a rolled-back signup or reset never sends a link
 * that points at nothing.
 */
public final class AfterCommit {
  private AfterCommit() {
  }

  public static void run(Runnable work) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      work.run();
      return;
    }
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        work.run();
      }
    });
  }
}
