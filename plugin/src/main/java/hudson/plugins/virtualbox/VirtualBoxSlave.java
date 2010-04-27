package hudson.plugins.virtualbox;

import hudson.Extension;
import hudson.Util;
import hudson.model.Computer;
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
  private final String virtualMachineType;

  @DataBoundConstructor
  public VirtualBoxSlave(
      String name, String nodeDescription, String remoteFS, String numExecutors, Mode mode, String labelString,
      ComputerLauncher delegateLauncher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties,
      String hostName, String virtualMachineName, String virtualMachineType
  ) throws Descriptor.FormException, IOException {
    super(
        name,
        nodeDescription,
        remoteFS,
        numExecutors,
        mode,
        labelString,
        new VirtualBoxComputerLauncher(delegateLauncher),
        retentionStrategy,
        nodeProperties
    );
    this.hostName = hostName;
    this.virtualMachineName = virtualMachineName;
    this.virtualMachineType = virtualMachineType;
  }

  @Override
  public Computer createComputer() {
    return new VirtualBoxComputer(this);
  }

  /**
   * @return host name
   */
  public String getHostName() {
    return hostName;
  }

  /**
   * @return virtual machine name
   */
  public String getVirtualMachineName() {
    return virtualMachineName;
  }

  /**
   * @return type of virtual machine, can be headless or vrdp
   */
  public String getVirtualMachineType() {
    return virtualMachineType;
  }

  @Override
  public VirtualBoxComputerLauncher getLauncher() {
    return (VirtualBoxComputerLauncher) super.getLauncher();
  }

  /**
   * For UI.
   *
   * @return original launcher
   */
  @SuppressWarnings({"UnusedDeclaration"})
  public ComputerLauncher getDelegateLauncher() {
    return getLauncher().getCore();
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
    public List<VirtualBoxCloud> getHosts() {
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
