package hudson.plugins.virtualbox;


import com.sun.xml.ws.commons.virtualbox_3_1.IVirtualBox;
import com.sun.xml.ws.commons.virtualbox_3_1.IWebsessionManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mihai Serban
 */
public final class VirtualBoxUtils {

  // public methods
  public static long startVm(VirtualBoxMachine machine, String virtualMachineType, VirtualBoxLogger log) {
    return getVboxControl(machine.getHost(), log).startVm(machine, virtualMachineType, log);
  }

  public static long stopVm(VirtualBoxMachine machine, VirtualBoxLogger log) {
    return getVboxControl(machine.getHost(), log).stopVm(machine, log);
  }

  public static List<VirtualBoxMachine> getMachines(VirtualBoxCloud host, VirtualBoxLogger log) {
    return getVboxControl(host, log).getMachines(host, log);
  }

  public static String getMacAddress(VirtualBoxMachine machine, VirtualBoxLogger log) {
    return getVboxControl(machine.getHost(), log).getMacAddress(machine, log);
  }

  public static void disconnectAll() {
    for (Map.Entry<String, VirtualBoxControl> entry: vboxControls.entrySet()) {
      entry.getValue().disconnect();
    }
    vboxControls.clear();
  }

  // private methods
  private VirtualBoxUtils() {
  }

  /**
   * Cache connections to VirtualBox hosts
   * TODO: keep the connections alive with a noop
   */
  private static HashMap<String, VirtualBoxControl> vboxControls = new HashMap<String, VirtualBoxControl>();

  private synchronized static VirtualBoxControl getVboxControl(VirtualBoxCloud host, VirtualBoxLogger log) {
    VirtualBoxControl vboxControl = (VirtualBoxControl)vboxControls.get(host.toString());
    if (null != vboxControl) {
      if (vboxControl.isConnected()) {
        return vboxControl;
      }
      log.logInfo("Lost connection to " + host.getUrl() + ", reconnecting");
      vboxControls.remove(host.toString()); // force a reconnect
    }
    vboxControl = createVboxControl(host, log);

    vboxControls.put(host.toString(), vboxControl);
    return vboxControl;
  }

  private static VirtualBoxControl createVboxControl(VirtualBoxCloud host, VirtualBoxLogger log) {
    VirtualBoxControl vboxControl = null;

    log.logInfo("Trying to connect to " + host.getUrl() + ", user " + host.getUsername());
    IWebsessionManager manager = new IWebsessionManager(host.getUrl());
    IVirtualBox vbox = manager.logon(host.getUsername(), host.getPassword());
    String version = vbox.getVersion();
    manager.disconnect(vbox);

    log.logInfo("Creating connection to VirtualBox version " + version);
      if (version.startsWith("4.3")) {
       vboxControl = new VirtualBoxControlV43(host.getUrl(), host.getUsername(), host.getPassword());
      } else if (version.startsWith("4.2")) {
       vboxControl = new VirtualBoxControlV42(host.getUrl(), host.getUsername(), host.getPassword());
      } else if (version.startsWith("4.1")) {
       vboxControl = new VirtualBoxControlV41(host.getUrl(), host.getUsername(), host.getPassword());
      } else if (version.startsWith("4.0")) {
      vboxControl = new VirtualBoxControlV40(host.getUrl(), host.getUsername(), host.getPassword());
    } else if (version.startsWith("3.")) {
      vboxControl = new VirtualBoxControlV31(host.getUrl(), host.getUsername(), host.getPassword());
    } else {
      log.logError("VirtualBox version " + version + " not supported.");
      throw new UnsupportedOperationException("VirtualBox version " + version + " not supported.");
    }

    log.logInfo("Connected to VirtualBox version " + version + " on host " + host.getUrl());
    return vboxControl;
  }
}
