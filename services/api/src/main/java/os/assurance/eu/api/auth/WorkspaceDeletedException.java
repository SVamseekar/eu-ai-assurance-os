package os.assurance.eu.api.auth;

/** The user's workspace has been deleted or is waiting to be purged. */
public class WorkspaceDeletedException extends RuntimeException {
  public WorkspaceDeletedException() {
    super("workspace_deleted");
  }
}
