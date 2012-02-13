package hudson.plugins.virtualbox;

import hudson.model.TaskListener;

/**
 *
 * @author Mihai Serban
 */
public class VirtualBoxTaskListenerLog implements VirtualBoxLogger {

  private final TaskListener taskListener;
  private final String logPrefix;

  public VirtualBoxTaskListenerLog(TaskListener taskLister, String logPrefix) {
    this.taskListener = taskLister;
    this.logPrefix = logPrefix;
  }

  /* log methods from VirtualBoxLogger */

  public void logInfo(String message) {
    taskListener.getLogger().println(logPrefix + message);
  }

  public void logWarning(String message) {
    taskListener.error(logPrefix + message);
  }

  public void logError(String message) {
    taskListener.error(logPrefix + message);
  }

  public void logFatalError(String message) {
    taskListener.fatalError(logPrefix + message);
  }
}
