package hudson.plugins.virtualbox;

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

  public static long stopVm(VirtualBoxMachine machine, String virtualMachineStopMode, VirtualBoxLogger log) {
    return getVboxControl(machine.getHost(), log).stopVm(machine, virtualMachineStopMode, log);
  }

  public static List<VirtualBoxMachine> getMachines(VirtualBoxCloud host, VirtualBoxLogger log) {
    return getVboxControl(host, log).getMachines(host, log);
  }

  public static String getMacAddress(VirtualBoxMachine machine, VirtualBoxLogger log) {
    return getVboxControl(machine.getHost(), log).getMacAddress(machine, log);
  }

  public static void disconnectAll() {
    for (Map.Entry<String, VirtualBoxControl> entry : vboxControls.entrySet()) {
      entry.getValue().disconnect();
    }
    vboxControls.clear();
  }

  // private methods
  private VirtualBoxUtils() {
  }

  /**
   * Cache connections to VirtualBox hosts
   * TODO: keep the connections alive with a no-op
   */
  private static HashMap<String, VirtualBoxControl> vboxControls = new HashMap<>();

  private synchronized static VirtualBoxControl getVboxControl(VirtualBoxCloud host, VirtualBoxLogger log) {
    VirtualBoxControl vboxControl = vboxControls.get(host.toString());
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
    String version = null;

    try {
      org.virtualbox_5_1.VirtualBoxManager manager = org.virtualbox_5_1.VirtualBoxManager.createInstance(null);
      manager.connect(host.getUrl(), host.getUsername(), host.getPassword().getPlainText());
      version = manager.getVBox().getVersion();
      manager.disconnect();
    } catch (Exception e) { 
      // fallback to old method
      com.sun.xml.ws.commons.virtualbox_3_1.IWebsessionManager manager = new com.sun.xml.ws.commons.virtualbox_3_1.IWebsessionManager(host.getUrl());
      com.sun.xml.ws.commons.virtualbox_3_1.IVirtualBox vbox = manager.logon(host.getUsername(), host.getPassword().getPlainText());
      version = vbox.getVersion();
      manager.disconnect(vbox);
    }

    version = version.substring(0, 3);
    log.logInfo("Creating connection to VirtualBox version " + version);

    switch (version) {
      case "6.0": vboxControl = new VirtualBoxControlV60(host.getUrl(), host.getUsername(), host.getPassword());
                  break;
      case "5.2": vboxControl = new VirtualBoxControlV52(host.getUrl(), host.getUsername(), host.getPassword());
                  break;
      case "5.1": vboxControl = new VirtualBoxControlV51(host.getUrl(), host.getUsername(), host.getPassword());
                  break;
      case "5.0": vboxControl = new VirtualBoxControlV50(host.getUrl(), host.getUsername(), host.getPassword());
                  break;
      case "4.3": vboxControl = new VirtualBoxControlV43(host.getUrl(), host.getUsername(), host.getPassword());
                  break;
      case "4.2": vboxControl = new VirtualBoxControlV42(host.getUrl(), host.getUsername(), host.getPassword());
                  break;
      case "4.1": vboxControl = new VirtualBoxControlV41(host.getUrl(), host.getUsername(), host.getPassword());
                  break;
      case "4.0": vboxControl = new VirtualBoxControlV40(host.getUrl(), host.getUsername(), host.getPassword());
                  break;
      case "3.":  vboxControl = new VirtualBoxControlV31(host.getUrl(), host.getUsername(), host.getPassword());
                  break;
      default:    log.logError("VirtualBox version " + version + " not supported.");
                  throw new UnsupportedOperationException("VirtualBox version " + version + " not supported.");
    }

    log.logInfo("Connected to VirtualBox version " + version + " on host " + host.getUrl());
    return vboxControl;
  }
}
