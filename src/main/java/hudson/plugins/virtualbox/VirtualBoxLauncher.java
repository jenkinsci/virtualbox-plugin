package hudson.plugins.virtualbox;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.JNLPLauncher;
import hudson.slaves.SlaveComputer;
import hudson.util.Scrambler;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * TODO see {@link hudson.slaves.ComputerLauncherFilter}
 *
 * @author Evgeny Mandrikov
 */
public class VirtualBoxLauncher extends JNLPLauncher {

  private final String url;
  private final String username;
  private final String password;
  private final String vmName;

  @DataBoundConstructor
  public VirtualBoxLauncher(String url, String username, String password, String vmName) {
    this.url = url;
    this.username = username;
    this.password = Scrambler.scramble(Util.fixEmptyAndTrim(password));
    this.vmName = vmName;
  }

  @Override
  public boolean isLaunchSupported() {
    return true;
  }

  @Override
  public void launch(SlaveComputer computer, TaskListener listener) {
    log(listener, Messages.VirtualBoxLauncher_startVM(getVmName()));
    try {
      long result = VirtualBoxUtils.startVm(getUrl(), getUsername(), getPassword(), getVmName());
      if (result != 0) {
        log(listener, "Unable to start"); // TODO l10n
      }
    } catch (Exception e) {
      e.printStackTrace(listener.getLogger());
    }
    
    super.launch(computer, listener);
  }

  @Override
  public void afterDisconnect(SlaveComputer computer, TaskListener listener) {
    log(listener, Messages.VirtualBoxLauncher_stopVM(getVmName()));
    try {
      VirtualBoxUtils.stopVm(getUrl(), getUsername(), getPassword(), getVmName());
    } catch (Exception e) {
      e.printStackTrace(listener.getLogger());
    }

    super.afterDisconnect(computer, listener);
  }

  private static void log(TaskListener listener, String message) {
    listener.getLogger().println("[VirtualBox] " + message);
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

  public String getUrl() {
    return url;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return Scrambler.descramble(password);
  }

  public String getVmName() {
    return vmName;
  }
}
