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
    try {
      machine.getHost().incrementActiveMachines();
      long result = getVboxControl(machine.getHost(), log).startVm(machine, virtualMachineType, log);
      if (result != 0) { machine.getHost().decrementActiveMachines(); }
      return result;
    }
    catch (InterruptedException e) {
      log.logFatalError("node " + machine.getName() + " error: InterruptedException thrown while waiting!");
      return 1;
    }
  }

  public static long stopVm(VirtualBoxMachine machine, String virtualMachineStopMode, VirtualBoxLogger log) {
    long result = getVboxControl(machine.getHost(), log).stopVm(machine, virtualMachineStopMode, log);
    if (result == 0) { machine.getHost().decrementActiveMachines(); }
    return result;
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

    org.virtualbox_5_2.VirtualBoxManager manager = org.virtualbox_5_2.VirtualBoxManager.createInstance(null);
    manager.connect(host.getUrl(), host.getUsername(), host.getPassword().getPlainText());
    version = manager.getVBox().getVersion();
    manager.disconnect();

    version = version.substring(0, 3);
    log.logInfo("Creating connection to VirtualBox version " + version);

    switch (version) {
      case "6.0": vboxControl = new VirtualBoxControlV60(host.getUrl(), host.getUsername(), host.getPassword());
                  break;
      case "5.2": vboxControl = new VirtualBoxControlV52(host.getUrl(), host.getUsername(), host.getPassword());
                  break;
      default:    log.logError("VirtualBox version " + version + " not supported.");
                  throw new UnsupportedOperationException("VirtualBox version " + version + " not supported.");
    }

    log.logInfo("Connected to VirtualBox version " + version + " on host " + host.getUrl());
    return vboxControl;
  }
}
