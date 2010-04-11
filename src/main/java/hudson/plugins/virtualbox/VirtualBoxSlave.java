package hudson.plugins.virtualbox;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Slave;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;

/**
 * {@link Slave} implementation for VirtualBox.
 *
 * @author Evgeny Mandrikov
 */
public class VirtualBoxSlave extends Slave {

  private final String hostName;
  private final String virtualMachineName;

  @DataBoundConstructor
  public VirtualBoxSlave(
      String name, String nodeDescription, String remoteFS, int numExecutors, Mode mode, String labelString,
      ComputerLauncher launcher, RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties,
      String hostName, String virtualMachineName
  ) throws Descriptor.FormException, IOException {
    super(name, nodeDescription, remoteFS, numExecutors, mode, labelString, new VirtualBoxLauncher(launcher, hostName, virtualMachineName), retentionStrategy, nodeProperties);
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

  /**
   * For UI.
   *
   * @see VirtualBoxPlugin#getHosts()
   */
  @SuppressWarnings({"UnusedDeclaration"})
  public List<VirtualBoxHost> getHosts() {
    return VirtualBoxPlugin.getHosts();
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
  }

}
