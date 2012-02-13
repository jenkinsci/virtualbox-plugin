package hudson.plugins.virtualbox;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mihai Serban
 */
public class VirtualBoxSystemLog implements VirtualBoxLogger {

  private final Logger logger;
  private final String logPrefix;

  public VirtualBoxSystemLog(Logger logger, String logPrefix) {
    this.logger = logger;
    this.logPrefix = logPrefix;
  }

  public void logInfo(String message) {
    logger.log(Level.INFO, "{0}{1}", new Object[]{logPrefix, message});
  }

  public void logWarning(String message) {
    logger.log(Level.WARNING, "{0}{1}", new Object[]{logPrefix, message});
  }

  public void logError(String message) {
    logger.log(Level.SEVERE, "{0}{1}", new Object[]{logPrefix, message});
  }

  public void logFatalError(String message) {
    logger.log(Level.SEVERE, "{0}{1}", new Object[]{logPrefix, message});
  }

}
