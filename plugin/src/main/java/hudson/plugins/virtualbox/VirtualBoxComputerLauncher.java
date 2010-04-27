package hudson.plugins.virtualbox;

import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ComputerLauncher} for VirtualBox that waits for the instance to really come up before processing to
 * the real user-specified {@link ComputerLauncher}.
 * <p>
 * TODO check relaunch during launch
 * </p>
 *
 * @author Evgeny Mandrikov
 */
public class VirtualBoxComputerLauncher extends ComputerLauncher {
  private static final Logger LOG = Logger.getLogger(VirtualBoxComputerLauncher.class.getName());

  private static final int SECOND = 1000;

  private ComputerLauncher delegate;

  /**
   * @param delegate real user-specified {@link ComputerLauncher}.
   */
  public VirtualBoxComputerLauncher(ComputerLauncher delegate) {
    this.delegate = delegate;
  }

  @Override
  public void launch(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException {
    VirtualBoxSlave slave = ((VirtualBoxComputer) computer).getNode();
    try {
      // Connect to VirtualBox host
      VirtualBoxMachine machine = VirtualBoxPlugin.getVirtualBoxMachine(slave.getHostName(), slave.getVirtualMachineName());
      if (machine == null) {
        listener.fatalError("Unable to find specified machine");
        return;
      }
      // TODO check virtual machine state - if started, then do nothing
      // if no, then start
      log(listener, Messages.VirtualBoxLauncher_startVM(machine));
      long result = VirtualBoxUtils.startVm(machine, slave.getVirtualMachineType());
      if (result != 0) {
        listener.fatalError("Unable to launch");
        return;
      }
      // TODO result may be != 0
    } catch (Throwable e) {
      listener.fatalError(e.getMessage(), e);
      e.printStackTrace(listener.getLogger());
      LOG.log(Level.WARNING, e.getMessage(), e);
      return;
    }
    // Stage 2 of the launch. Called after the VirtualBox instance comes up.
    boolean successful = false;
    int attempt = 0;
    while (!successful) {
      attempt++;
      log(listener, "Sleep before stage 2 launcher, attempt " + attempt);
      Thread.sleep(10 * SECOND);
      successful = delegateLaunch(computer, listener);
      if (!successful && attempt > 10) {
        log(listener, "Maximum number of attempts reached");
        return;
      }
    }
  }

  /**
   * @param computer {@link hudson.model.Computer} for which agent should be launched
   * @param listener The progress of the launch, as well as any error, should be sent to this listener.
   * @return true, if successfully launched, otherwise false
   */
  protected boolean delegateLaunch(SlaveComputer computer, TaskListener listener) {
    try {
      log(listener, "Starting stage 2 launcher (" + delegate.getClass().getSimpleName() + ")");
      getCore().launch(computer, listener);
      log(listener, "Stage 2 launcher completed");
      return true;
    } catch (IOException e) {
      log(listener, "Unable to launch: " + e.getMessage());
      return false;
    } catch (InterruptedException e) {
      log(listener, "Unable to launch: " + e.getMessage());
      return false;
    }
  }

  private static void log(TaskListener listener, String message) {
    listener.getLogger().println("[VirtualBox] " + message);
  }

  @Override
  public void beforeDisconnect(SlaveComputer computer, TaskListener listener) {
    log(listener, "Starting stage 2 beforeDisconnect");
    getCore().beforeDisconnect(computer, listener);
    log(listener, "Stage 2 beforeDisconnect completed");
  }

  @Override
  public void afterDisconnect(SlaveComputer computer, TaskListener listener) {
    VirtualBoxSlave slave = ((VirtualBoxComputer) computer).getNode();
    // Stage 2 of the afterDisconnect
    log(listener, "Starting stage 2 afterDisconnect");
    getCore().afterDisconnect(computer, listener);
    log(listener, "Stage 2 afterDisconnect completed");
    try {
      // Connect to VirtualBox host
      VirtualBoxMachine machine = VirtualBoxPlugin.getVirtualBoxMachine(slave.getHostName(), slave.getVirtualMachineName());
      if (machine == null) {
        listener.fatalError("Unable to find specified machine");
      }
      // TODO check virtual machine state - if stopped, then do nothing
      // if no, then stop
      log(listener, Messages.VirtualBoxLauncher_stopVM(machine));
      long result = VirtualBoxUtils.stopVm(machine);
      if (result != 0) {
        listener.fatalError("Unable to stop");
        return;
      }
    } catch (Throwable e) {
      listener.fatalError(e.getMessage(), e);
    }
  }

  /**
   * @return delegation target
   */
  public ComputerLauncher getCore() {
    return delegate;
  }

  @Override
  public Descriptor<ComputerLauncher> getDescriptor() {
    // Don't allow creation of launcher from UI
    throw new UnsupportedOperationException();
  }
}
