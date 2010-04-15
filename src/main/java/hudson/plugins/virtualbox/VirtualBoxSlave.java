package hudson.plugins.virtualbox;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.Slave;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * {@link Slave} running on VirtualBox.
 *
 * @author Evgeny Mandrikov
 */
public class VirtualBoxSlave extends Slave {
  private static final Logger LOG = Logger.getLogger(VirtualBoxSlave.class.getName());

  private final String hostName;
  private final String virtualMachineName;

  @DataBoundConstructor
  public VirtualBoxSlave(
      String name, String nodeDescription, String remoteFS, String numExecutors, Mode mode, String labelString,
      ComputerLauncher delegateLauncher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties,
      String hostName, String virtualMachineName
  ) throws Descriptor.FormException, IOException {
    super(name, nodeDescription, remoteFS, numExecutors, mode, labelString, new VirtualBoxLauncher(delegateLauncher, hostName, virtualMachineName), retentionStrategy, nodeProperties);
    this.hostName = hostName;
    this.virtualMachineName = virtualMachineName;
  }

  /**
   * For UI.
   *
   * @return host name
   */
  @SuppressWarnings({"UnusedDeclaration"})
  public String getHostName() {
    return hostName;
  }

  /**
   * For UI.
   *
   * @return virtual machine name
   */
  @SuppressWarnings({"UnusedDeclaration"})
  public String getVirtualMachineName() {
    return virtualMachineName;
  }

  /**
   * For UI.
   *
   * @return original launcher
   */
  @SuppressWarnings({"UnusedDeclaration"})
  public ComputerLauncher getDelegateLauncher() {
    return ((VirtualBoxLauncher) getLauncher()).getCore();
  }

  @Extension
  public static final class DescriptorImpl extends SlaveDescriptor {
    @Override
    public String getDisplayName() {
      return Messages.VirtualBoxSlave_displayName();
    }

    /**
     * For UI.
     *
     * @see VirtualBoxPlugin#getHost(String)
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public List<VirtualBoxMachine> getDefinedVirtualMachines(String hostName) {
      return VirtualBoxPlugin.getDefinedVirtualMachines(hostName);
    }

    /**
     * For UI.
     *
     * @see VirtualBoxPlugin#getHosts()
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public List<VirtualBoxHost> getHosts() {
      return VirtualBoxPlugin.getHosts();
    }

    /**
     * For UI.
     * TODO Godin: doesn't work
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public FormValidation doCheckHostName(@QueryParameter String value) {
      LOG.info("Perform on the fly check - hostName");
      if (Util.fixEmptyAndTrim(value) == null) {
        return FormValidation.error("VirtualBox Host is mandatory");
      }
      return FormValidation.ok();
    }

    /**
     * For UI.
     * TODO Godin: doesn't work
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public FormValidation doCheckVirtualMachineName(@QueryParameter String value) {
      LOG.info("Perform on the fly check - virtualMachineName");
      if (Util.fixEmptyAndTrim(value) == null) {
        return FormValidation.error("Virtual Machine Name is mandatory");
      }
      return FormValidation.ok();
    }
  }

}
