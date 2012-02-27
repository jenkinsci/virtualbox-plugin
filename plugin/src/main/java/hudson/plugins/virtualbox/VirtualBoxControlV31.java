package hudson.plugins.virtualbox;

import com.sun.xml.ws.commons.virtualbox_3_1.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 */
public final class VirtualBoxControlV31 implements VirtualBoxControl {

  private final String hostUrl;
  private final String userName;
  private final String password;

  public VirtualBoxControlV31(String hostUrl, String userName, String password) {
    // verify connection
    this.hostUrl = hostUrl;
    this.userName = userName;
    this.password = password;

    ConnectionHolder holder = connect(hostUrl, userName, password);
    holder.disconnect();
  }

  static class ConnectionHolder {
    IWebsessionManager manager;
    IVirtualBox vbox;

    public void disconnect() {
      manager.disconnect(vbox);
    }
  }

  private static ConnectionHolder connect(String hostUrl, String userName, String password) {
    IWebsessionManager manager = new IWebsessionManager(hostUrl);
    ConnectionHolder holder = new ConnectionHolder();
    holder.manager = manager;
    holder.vbox = manager.logon(userName, password);
    return holder;
  }

  public void disconnect() {
  }

  public boolean isConnected() {
    try {
      ConnectionHolder holder = connect(hostUrl, userName, password);
      holder.vbox.getVersion();
      holder.disconnect();
      return true;
    } catch (Throwable e) {
      return false;
    }
  }

  /**
   * @param host VirtualBox host
   * @return list of virtual machines installed on specified host
   */
  public synchronized List<VirtualBoxMachine> getMachines(VirtualBoxCloud host, VirtualBoxLogger log) {
    List<VirtualBoxMachine> result = new ArrayList<VirtualBoxMachine>();
    ConnectionHolder holder = connect(hostUrl, userName, password);
    for (IMachine machine : holder.vbox.getMachines()) {
      result.add(new VirtualBoxMachine(host, machine.getName(), machine.getId(), null));
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
  public synchronized long startVm(VirtualBoxMachine vbMachine, String type, VirtualBoxLogger log) {
    ConnectionHolder holder = connect(hostUrl, userName, password);
    IMachine machine = holder.vbox.findMachine(vbMachine.getName());
    if (org.virtualbox_3_1.MachineState.RUNNING == machine.getState()) {
      holder.disconnect();
      return 0;
    }
    ISession session = holder.manager.getSessionObject(holder.vbox);
    // start the virtual machine in a separate process
    IProgress progress = holder.vbox.openRemoteSession(
        session,
        machine.getId(),
        type, // sessionType (headless, vrdp)
        "" // env
    );
    progress.waitForCompletion(-1);
    long result = progress.getResultCode();
    session.close(); // match openRemoteSession
    holder.disconnect();
    return result;
  }

  /**
   * Stops specified VirtualBox virtual machine.
   *
   * @param vbMachine virtual machine to stop
   * @return result code
   */
  public synchronized long stopVm(VirtualBoxMachine vbMachine, VirtualBoxLogger log) {
    ConnectionHolder holder = connect(hostUrl, userName, password);
    IMachine machine = holder.vbox.findMachine(vbMachine.getName());
    if (org.virtualbox_3_1.MachineState.RUNNING != machine.getState()) {
      holder.disconnect();
      return 0;
    }
    ISession session = holder.manager.getSessionObject(holder.vbox);

    holder.vbox.openExistingSession(session, machine.getId());
    IProgress progress = session.getConsole().powerDown();
    progress.waitForCompletion(-1);
    long result = progress.getResultCode();
    session.close(); // match openExistingSession
    holder.disconnect();
    return result;
  }

  /**
   * @param vbMachine virtual machine
   * @return MAC Address of specified virtual machine
   */
  public synchronized String getMacAddress(VirtualBoxMachine vbMachine, VirtualBoxLogger log) {
    ConnectionHolder holder = connect(hostUrl, userName, password);
    IMachine machine = holder.vbox.findMachine(vbMachine.getName());
    String macAddress = machine.getNetworkAdapter(0L).getMACAddress();
    holder.disconnect();
    return macAddress;
  }
}
