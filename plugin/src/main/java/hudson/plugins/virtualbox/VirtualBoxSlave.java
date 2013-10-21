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
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

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
  private final String virtualMachineStopMode;

  @DataBoundConstructor
  public VirtualBoxSlave(
      String name, String nodeDescription, String remoteFS, String numExecutors, Mode mode, String labelString,
      ComputerLauncher delegateLauncher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties,
      String hostName, String virtualMachineName, String virtualMachineType, String virtualMachineStopMode
  ) throws Descriptor.FormException, IOException {
    super(
        name,
        nodeDescription,
        remoteFS,
        numExecutors,
        mode,
        labelString,
        new VirtualBoxComputerLauncher(delegateLauncher, hostName, virtualMachineName, virtualMachineType, virtualMachineStopMode),
        retentionStrategy,
        nodeProperties
    );
    this.hostName = hostName;
    this.virtualMachineName = virtualMachineName;
    this.virtualMachineType = virtualMachineType;
    this.virtualMachineStopMode = virtualMachineStopMode;
  }

  public VirtualBoxSlave(
      String name, String nodeDescription, String remoteFS, String numExecutors, Mode mode, String labelString,
      ComputerLauncher delegateLauncher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties,
      String hostName, String virtualMachineName, String virtualMachineType
  ) throws Descriptor.FormException, IOException {
    this(
        name,
        nodeDescription,
        remoteFS,
        numExecutors,
        mode,
        labelString,
        delegateLauncher,
        retentionStrategy,
        nodeProperties,
        hostName,
        virtualMachineName,
        virtualMachineType,
        "pause");
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
   * @return type of virtual machine, can be headless, vrdp, gui, or sdl
   */
  public String getVirtualMachineType() {
    return virtualMachineType;
  }

  /**
   * @return type of stop mode for virtual machine, can be powerdown or pause
   */
  public String getVirtualMachineStopMode() {
    return virtualMachineStopMode;
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
    public List<VirtualBoxMachine> getDefinedVirtualMachines(String hostName) {
      return VirtualBoxPlugin.getDefinedVirtualMachines(hostName);
    }

    /**
     * For UI.
     *
     * @see VirtualBoxPlugin#getHosts()
     */
    public List<VirtualBoxCloud> getHosts() {
      return VirtualBoxPlugin.getHosts();
    }

    /**
     * For UI.
     * TODO Godin: doesn't work
     */
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
    public FormValidation doCheckVirtualMachineName(@QueryParameter String value) {
      LOG.info("Perform on the fly check - virtualMachineName");
      if (Util.fixEmptyAndTrim(value) == null) {
        return FormValidation.error("Virtual Machine Name is mandatory");
      }
      return FormValidation.ok();
    }
  }

}
