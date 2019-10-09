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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * {@link Slave} running on VirtualBox.
 *
 * @author Evgeny Mandrikov
 * @author Brandon Jones
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
  String getHostName() {
    return hostName;
  }

  /**
   * @return virtual machine name
   */
  String getVirtualMachineName() {
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
     * @return A list of defined VirtualBoxMachines
     */
    public List<VirtualBoxMachine> getDefinedVirtualMachines(String hostName) {
      return VirtualBoxPlugin.getDefinedVirtualMachines(hostName);
    }

    /**
     * For UI.
     *
     * @see VirtualBoxPlugin#getHosts()
     * @return A list of VirtualBoxClouds
     */
    public List<VirtualBoxCloud> getHosts() {
      return VirtualBoxPlugin.getHosts();
    }

    /**
     * For UI.
     * @param HostName The name of the host to be validated.
     * @return FormValidation for error or ok
     */
    public FormValidation doCheckHostName(@QueryParameter String HostName) {
      if (Objects.equals(Util.fixEmptyAndTrim(HostName), Messages.VirtualBoxSlave_defaultHost())) {
        return FormValidation.error("VirtualBox Host is mandatory!");
      } else {
        return FormValidation.ok();
      }
    }

    /**
     * For UI.
     * @param VirtualMachineName The name of the virtual machine to be validated.
     * @return FormValidation for error or ok
     */
    public FormValidation doCheckVirtualMachineName(@QueryParameter String VirtualMachineName) {
      if (Util.fixEmptyAndTrim(VirtualMachineName) == null) {
        return FormValidation.error("Virtual Machine Name is mandatory!");
      } else {
        return FormValidation.ok();
      }
    }

    /**
     * Used to auto-populate the list of virtual machine names based on the selected host.
     * @param item The ancestor object used to access the saved virtual machine name.
     * @param HostName The name of the host to query virtual machine names from.
     * @return A ListBoxModel containing the virtual machine names.
     */
    public ListBoxModel doFillVirtualMachineNameItems(String HostName) {
      if (Messages.VirtualBoxSlave_defaultHost().equals(HostName)) {
        LOG.log(Level.INFO, "Default host name selected - returning null virtual machine list");
        return null;
      }
      LOG.log(Level.INFO, "Host name set as " + HostName);
      LOG.log(Level.INFO, "VBoxSlave object set as " + item.getFullDisplayName());
      ListBoxModel m = VirtualBoxPlugin.getDefinedVirtualMachinesListBox(HostName);



      // Find the index of the saved Virtual Machine Name.
      for (ListBoxModel.Option option : m) {
        if (option.value.equals("test")) {//VBoxSlave.getVirtualMachineName())) {
          option.selected = true;
        }
        else {
          LOG.log(Level.INFO, option.value.toString());
        }
      }
      return m;
    }
  }
}
