package hudson.plugins.virtualbox;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.ComputerLauncherFilter;
import hudson.slaves.SlaveComputer;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ComputerLauncher} implementation for VirtualBox.
 *
 * @author Evgeny Mandrikov
 */
public class VirtualBoxLauncher extends ComputerLauncherFilter {

  private static final Logger LOG = Logger.getLogger(VirtualBoxLauncher.class.getName());

  private String hostName;
  private String virtualMachineName;

  @DataBoundConstructor
  public VirtualBoxLauncher(ComputerLauncher delegate, String hostName, String virtualMachineName) {
    super(delegate);
    this.hostName = hostName;
    this.virtualMachineName = virtualMachineName;
  }

  @Override
  public boolean isLaunchSupported() {
    return true;
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

  @Override
  public void launch(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException {
    LOG.info("Launch: " + computer.getName());
    // TODO NPE
    VirtualBoxMachine machine = VirtualBoxPlugin.getVirtualBoxMachine(getHostName(), getVirtualMachineName());
    log(listener, Messages.VirtualBoxLauncher_startVM(machine));
    try {
      long result = VirtualBoxUtils.startVm(machine);
      log(listener, "Result: " + result);
      if (result != 0) {
        log(listener, Messages.VirtualBoxLauncher_startFailed(machine));
      }
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Exception", e);
      e.printStackTrace(listener.getLogger());
    }

    if (getCore() != null) {
      super.launch(computer, listener);
    }
  }

  @Override
  public void beforeDisconnect(SlaveComputer computer, TaskListener listener) {
    LOG.info("Before disconnect: " + computer.getName());

    if (getCore() != null) {
      super.beforeDisconnect(computer, listener);
    }

    // TODO NPE
    VirtualBoxMachine machine = VirtualBoxPlugin.getVirtualBoxMachine(getHostName(), getVirtualMachineName());
    log(listener, Messages.VirtualBoxLauncher_stopVM(machine));
    try {
      VirtualBoxUtils.stopVm(machine);
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Exception", e);
      e.printStackTrace(listener.getLogger());
    }
  }

  @Override
  public void afterDisconnect(SlaveComputer computer, TaskListener listener) {
    LOG.info("After disconnect: " + computer.getName());
    if (getCore() != null) {
      super.afterDisconnect(computer, listener);
    }
  }

  private static void log(TaskListener listener, String message) {
    listener.getLogger().println("[VirtualBox] " + message);
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

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) Hudson.getInstance().getDescriptorOrDie(VirtualBoxLauncher.class);
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<ComputerLauncher> {

    public DescriptorImpl() {
      super(VirtualBoxLauncher.class);
    }

    public String getDisplayName() {
      return Messages.VirtualBoxLauncher_displayName();
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
