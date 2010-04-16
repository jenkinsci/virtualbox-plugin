package hudson.plugins.virtualbox;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

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
    VirtualBoxUtils.startVm(machine, "headless"); // TODO type
    // TODO wait for start

    class EnvironmentImpl extends Environment {
      @Override
      public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
        VirtualBoxMachine machine = VirtualBoxPlugin.getVirtualBoxMachine(getHostName(), getVirtualMachineName());
        listener.getLogger().println(Messages.VirtualBoxLauncher_stopVM(machine));
        VirtualBoxUtils.stopVm(machine);
        // TODO wait for stop
        return true;
      }
    }

    return new EnvironmentImpl();
  }

  public String getHostName() {
    return hostName;
  }

  public String getVirtualMachineName() {
    return virtualMachineName;
  }

  // TODO enable wrapper
//  @Extension
//  public static final class DescriptorImpl extends Descriptor<BuildWrapper> {
//    @Override
//    public String getDisplayName() {
//      return Messages.VirtualBoxBuildWrapper_displayName();
//    }
//
//    /**
//     * For UI.
//     *
//     * @see VirtualBoxPlugin#getHost(String)
//     */
//    @SuppressWarnings({"UnusedDeclaration"})
//    public List<VirtualBoxMachine> getDefinedVirtualMachines(String hostName) {
//      return VirtualBoxPlugin.getDefinedVirtualMachines(hostName);
//    }
//
//    /**
//     * For UI.
//     *
//     * @see VirtualBoxPlugin#getHosts()
//     */
//    @SuppressWarnings({"UnusedDeclaration"})
//    public List<VirtualBoxHost> getHosts() {
//      return VirtualBoxPlugin.getHosts();
//    }
//  }
}
