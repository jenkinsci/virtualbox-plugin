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

/**
 * {@link ComputerLauncher} implementation for VirtualBox.
 *
 * @author Evgeny Mandrikov
 */
public class VirtualBoxLauncher extends ComputerLauncherFilter {

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

  private VirtualBoxMachine getVirtualBoxMachine() {
    for (VirtualBoxMachine machine : VirtualBoxPlugin.getHost(hostName).getVirtualMachines()) {
      if (virtualMachineName.equals(machine.getName())) {
        return machine;
      }
    }
    return null;
  }

  @Override
  public void launch(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException {
    VirtualBoxMachine machine = getVirtualBoxMachine();
    log(listener, Messages.VirtualBoxLauncher_startVM(machine));
    try {
      long result = VirtualBoxUtils.startVm(machine);
      log(listener, "Result: " + result);
      if (result != 0) {
        log(listener, "Unable to start"); // TODO l10n
      }
    } catch (Exception e) {
      e.printStackTrace(listener.getLogger());
    }

    super.launch(computer, listener);
  }

  @Override
  public void beforeDisconnect(SlaveComputer computer, TaskListener listener) {
    super.beforeDisconnect(computer, listener);
  }

  @Override
  public void afterDisconnect(SlaveComputer computer, TaskListener listener) {
    VirtualBoxMachine machine = getVirtualBoxMachine();
    log(listener, Messages.VirtualBoxLauncher_stopVM(machine));
    try {
      VirtualBoxUtils.stopVm(machine);
    } catch (Exception e) {
      e.printStackTrace(listener.getLogger());
    }

    super.afterDisconnect(computer, listener);
  }

  private static void log(TaskListener listener, String message) {
    listener.getLogger().println("[VirtualBox] " + message);
  }

  @Override
  public Descriptor<ComputerLauncher> getDescriptor() {
    return Hudson.getInstance().getDescriptorOrDie(getClass());
  }

  /**
   * For UI.
   */
  @SuppressWarnings({"UnusedDeclaration"})
  public String getHostName() {
    return hostName;
  }

  public String getVirtualMachineName() {
    return virtualMachineName;
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<ComputerLauncher> {

    public DescriptorImpl() {
      super(VirtualBoxLauncher.class);
    }

    public String getDisplayName() {
      return Messages.VirtualBoxLauncher_displayName();
    }

  }
}
