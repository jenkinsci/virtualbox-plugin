package hudson.plugins.virtualbox;

import java.util.ArrayList;
import java.util.List;

import org.virtualbox_4_0.IMachine;
import org.virtualbox_4_0.IProgress;
import org.virtualbox_4_0.ISession;
import org.virtualbox_4_0.MachineState;
import org.virtualbox_4_0.VirtualBoxManager;

/**
 * @author Evgeny Mandrikov, Lars Gregori
 */
public final class VirtualBoxUtils {

  private VirtualBoxUtils() {
  }

  private static VirtualBoxManager connect(VirtualBoxCloud host) {
	  VirtualBoxManager mgr = VirtualBoxManager.createInstance("Unter Null - The Failure Epiphany - You Have Fallen From Grace");
    mgr.connect(host.getUrl(), host.getUsername(), host.getPassword());
    return mgr;
  }

  /**
   * Get virtual machines installed on specified host.
   * 
   * @param host VirtualBox host
   * @return list of virtual machines installed on specified host
   */
  public static List<VirtualBoxMachine> getMachines(VirtualBoxCloud host) {
    List<VirtualBoxMachine> result = new ArrayList<VirtualBoxMachine>();
    VirtualBoxManager mgr = connect(host);
    for (IMachine machine : mgr.getVBox().getMachines()) {
      result.add(new VirtualBoxMachine(host, machine.getName()));
    }
    mgr.disconnect();
    return result;
  }

  /**
   * Starts specified VirtualBox virtual machine.
   *
   * @param vbMachine virtual machine to start
   * @param type      session type (can be headless, vrdp, gui, sdl)
   * @return result code
   */
  public static long startVm(VirtualBoxMachine vbMachine, String type) {
    VirtualBoxManager mgr = connect(vbMachine.getHost());
    IMachine machine;
    try {
      machine = mgr.getVBox().findMachine(vbMachine.getName());
    } catch (Exception e) {
      try {
        // try again ("idea" taken from VirtualBox 3.x example)
        machine = mgr.getVBox().findMachine(vbMachine.getName());
      } catch (Exception e2) {
        return -1;
      }
    }

    // check virtual machine state - if started, then do nothing
    // TODO actually this should be in VirtualBoxComputerLauncher
    if (MachineState.Running == machine.getState()) {
      return 0;
    }

    ISession session = mgr.getSessionObject();
    String env = "";
    IProgress progress = machine.launchVMProcess(session, type, env);
    progress.waitForCompletion(-1);
    long result = progress.getResultCode();
    mgr.disconnect();
    return result;
  }

  /**
   * Stops specified VirtualBox virtual machine.
   *
   * @param vbMachine virtual machine to stop
   * @return result code
   */
  public static long stopVm(VirtualBoxMachine vbMachine) {
    VirtualBoxManager mgr = connect(vbMachine.getHost());
    IMachine machine = mgr.getVBox().findMachine(vbMachine.getName());
    // check virtual machine state - if not running, then do nothing
    // TODO actually this should be in VirtualBoxComputerLauncher
    if (MachineState.Running != machine.getState()) {
      return 0;
    }
    ISession session;
    try {
      session = mgr.openMachineSession(machine);
    } catch (Exception e) {
      return -1;
    }

    IProgress progress = session.getConsole().powerDown();
    progress.waitForCompletion(-1);
    long result = progress.getResultCode();
    mgr.disconnect();
    return result;
  }

  /**
   * MAC Address of specified virtual machine.
   * 
   * @param vbMachine virtual machine
   * @return MAC Address of specified virtual machine
   */
  public static String getMacAddress(VirtualBoxMachine vbMachine) {
    VirtualBoxManager mgr = connect(vbMachine.getHost());
    IMachine machine = mgr.getVBox().findMachine(vbMachine.getName());
    String macAddress = machine.getNetworkAdapter(0L).getMACAddress();
    mgr.disconnect();
    return macAddress;
  }
}
