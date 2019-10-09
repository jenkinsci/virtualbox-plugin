package hudson.plugins.virtualbox;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import java.io.IOException;

import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * @author Evgeny Mandrikov
 */
public class VirtualBoxBuildWrapper extends BuildWrapper {
  private String hostName;
  private String virtualMachineName;

  @DataBoundConstructor
  public VirtualBoxBuildWrapper(String hostName, String virtualMachineName) {
    super();
    this.hostName = hostName;
    this.virtualMachineName = virtualMachineName;
  }

  @Override
  public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    VirtualBoxMachine machine = VirtualBoxPlugin.getVirtualBoxMachine(getHostName(), getVirtualMachineName());
    listener.getLogger().println(Messages.VirtualBoxLauncher_startVM(machine));
    VirtualBoxUtils.startVm(machine, "headless", new VirtualBoxTaskListenerLog(listener, "[VirtualBox] ")); // TODO type

    class EnvironmentImpl extends Environment {
      @Override
      public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
        VirtualBoxMachine machine = VirtualBoxPlugin.getVirtualBoxMachine(getHostName(), getVirtualMachineName());
        listener.getLogger().println(Messages.VirtualBoxLauncher_stopVM(machine));
        VirtualBoxUtils.stopVm(machine, "pause", new VirtualBoxTaskListenerLog(listener, "[VirtualBox] "));
        return true;
      }
    }

    return new EnvironmentImpl();
  }

  public String getHostName() {
    return hostName;
  }

  public String getVirtualMachineName() { return virtualMachineName; }

  @Extension
  public static final class DescriptorImpl extends BuildWrapperDescriptor {
    public ListBoxModel doFillVirtualMachineName(@QueryParameter String hostName) {
      return VirtualBoxPlugin.getDefinedVirtualMachinesListBox(hostName);
    }

    @Override
    public boolean isApplicable(AbstractProject<?, ?> item) {
      return false;
    }
  }
}
