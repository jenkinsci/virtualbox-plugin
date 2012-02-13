package hudson.plugins.virtualbox;

/**
 * @author Mihai Serban
 */
public interface VirtualBoxLogger {
  public void logInfo(String message);
  public void logWarning(String message);
  public void logError(String message);
  public void logFatalError(String message);
}
