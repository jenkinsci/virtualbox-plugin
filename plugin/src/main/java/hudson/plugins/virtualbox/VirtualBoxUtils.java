package hudson.plugins.virtualbox;

import com.sun.xml.ws.commons.virtualbox_3_1.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 */
public final class VirtualBoxUtils {

  private VirtualBoxUtils() {
  }

  static class ConnectionHolder {
    IWebsessionManager manager;
    IVirtualBox vbox;

    public void disconnect() {
      manager.disconnect(vbox);
    }
  }

  private static ConnectionHolder connect(VirtualBoxCloud host) {
    IWebsessionManager manager = new IWebsessionManager(host.getUrl());
    ConnectionHolder holder = new ConnectionHolder();
    holder.manager = manager;
    holder.vbox = manager.logon(host.getUsername(), host.getPassword());
    return holder;
  }

  /**
   * @param host VirtualBox host
   * @return list of virtual machines installed on specified host
   */
  public static List<VirtualBoxMachine> getMachines(VirtualBoxCloud host) {
    List<VirtualBoxMachine> result = new ArrayList<VirtualBoxMachine>();
    ConnectionHolder holder = connect(host);
    for (IMachine machine : holder.vbox.getMachines()) {
      result.add(new VirtualBoxMachine(host, machine.getName()));
    }
    holder.disconnect();
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
    ConnectionHolder holder = connect(vbMachine.getHost());
    ISession session = holder.manager.getSessionObject(holder.vbox);
    IMachine machine = holder.vbox.findMachine(vbMachine.getName());
    // check virtual machine state - if started, then do nothing
    // TODO actually this should be in VirtualBoxComputerLauncher
    if (org.virtualbox_3_1.MachineState.RUNNING == machine.getState()) {
      return 0;
    }
    IProgress progress = holder.vbox.openRemoteSession(
        session,
        machine.getId(),
        type, // sessionType (headless, vrdp)
        "" // env
    );
    progress.waitForCompletion(-1);
    long result = progress.getResultCode();
    holder.disconnect();
    return result;
  }

  /**
   * Stops specified VirtualBox virtual machine.
   *
   * @param vbMachine virtual machine to stop
   * @return result code
   */
  public static long stopVm(VirtualBoxMachine vbMachine) {
    ConnectionHolder holder = connect(vbMachine.getHost());
    ISession session = holder.manager.getSessionObject(holder.vbox);
    IMachine machine = holder.vbox.findMachine(vbMachine.getName());
    // check virtual machine state - if not running, then do nothing
    // TODO actually this should be in VirtualBoxComputerLauncher
    if (org.virtualbox_3_1.MachineState.RUNNING != machine.getState()) {
      return 0;
    }
    holder.vbox.openExistingSession(session, machine.getId());
    IProgress progress = session.getConsole().powerDown();
    progress.waitForCompletion(-1);
    long result = progress.getResultCode();
    holder.disconnect();
    return result;
  }

  /**
   * @param vbMachine virtual machine
   * @return MAC Address of specified virtual machine
   */
  public static String getMacAddress(VirtualBoxMachine vbMachine) {
    ConnectionHolder holder = connect(vbMachine.getHost());
    IMachine machine = holder.vbox.findMachine(vbMachine.getName());
    String macAddress = machine.getNetworkAdapter(0L).getMACAddress();
    holder.disconnect();
    return macAddress;
  }
}
