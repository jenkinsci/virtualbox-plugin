package hudson.plugins.virtualbox;

import hudson.Plugin;
import hudson.model.Hudson;
import hudson.slaves.Cloud;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

  /**
   * @return all registered {@link VirtualBoxCloud}
   */
  public static List<VirtualBoxCloud> getHosts() {
    List<VirtualBoxCloud> result = new ArrayList<VirtualBoxCloud>();
    for (Cloud cloud : Hudson.getInstance().clouds) {
      if (cloud instanceof VirtualBoxCloud) {
        result.add((VirtualBoxCloud) cloud);
      }
    }
    return result;
  }

  /**
   * @param hostName host name
   * @return {@link VirtualBoxCloud} by specified name, null if not found
   */
  public static VirtualBoxCloud getHost(String hostName) {
    if (hostName == null) {
      return null;
    }
    for (VirtualBoxCloud host : getHosts()) {
      if (hostName.equals(host.getDisplayName())) {
        return host;
      }
    }
    return null;
  }

  /**
   * @param hostName host name
   * @return all registered {@link VirtualBoxMachine} from specified host, empty list if unknown host
   */
  public static List<VirtualBoxMachine> getDefinedVirtualMachines(String hostName) {
    VirtualBoxCloud host = getHost(hostName);
    if (host == null) {
      return Collections.emptyList();
    }
    return host.getVirtualMachines();
  }

  /**
   * @param hostName           host name
   * @param virtualMachineName virtual machine name
   * @return {@link VirtualBoxMachine} from specified host with specified name, null if not found
   */
  public static VirtualBoxMachine getVirtualBoxMachine(String hostName, String virtualMachineName) {
    if (virtualMachineName == null) {
      return null;
    }
    VirtualBoxCloud host = VirtualBoxPlugin.getHost(hostName);
    if (host == null) {
      return null;
    }
    for (VirtualBoxMachine machine : host.getVirtualMachines()) {
      if (virtualMachineName.equals(machine.getName())) {
        return machine;
      }
    }
    return null;
  }

  /**
   * For UI.
   */
  @SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
  public void doComputerNameValues(StaplerRequest req, StaplerResponse resp, @QueryParameter("hostName") String hostName)
      throws IOException, ServletException {
    ListBoxModel m = new ListBoxModel();
    List<VirtualBoxMachine> virtualMachines = getDefinedVirtualMachines(hostName);
    if (virtualMachines != null && virtualMachines.size() > 0) {
      for (VirtualBoxMachine vm : virtualMachines) {
        m.add(new ListBoxModel.Option(vm.getName(), vm.getName()));
      }
      m.get(0).selected = true;
    }
    m.writeTo(req, resp);
  }
}
