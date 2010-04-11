package hudson.plugins.virtualbox;

import hudson.Plugin;
import hudson.model.Hudson;
import hudson.slaves.Cloud;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Evgeny Mandrikov
 */
public class VirtualBoxPlugin extends Plugin {

  private static final Logger LOG = Logger.getLogger(VirtualBoxPlugin.class.getName());

  @Override
  public void start() throws Exception {
    LOG.info("Starting " + getClass().getSimpleName());
    super.start();
  }

  @Override
  public void stop() throws Exception {
    LOG.info("Stopping " + getClass().getSimpleName());
    super.stop();
  }

  public static List<VirtualBoxHost> getHosts() {
    List<VirtualBoxHost> result = new ArrayList<VirtualBoxHost>();
    for (Cloud cloud : Hudson.getInstance().clouds) {
      if (cloud instanceof VirtualBoxHost) {
        result.add((VirtualBoxHost) cloud);
      }
    }
    return result;
  }

  public static VirtualBoxHost getHost(String hostName) {
    for (VirtualBoxHost host : getHosts()) {
      if (hostName.equals(host.getDisplayName())) {
        return host;
      }
    }
    return null;
  }
}
