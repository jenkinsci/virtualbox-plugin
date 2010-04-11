package hudson.plugins.virtualbox;

import com.sun.xml.ws.commons.virtualbox.*;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 */
public final class VirtualBoxUtils {

  private VirtualBoxUtils() {
  }

  public static IVirtualBox connect(String url, String username, String password) {
    // working around https://jax-ws.dev.java.net/issues/show_bug.cgi?id=554
    // this is also necessary when context classloader doesn't have the JAX-WS API
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(IVirtualBox.class.getClassLoader());
    try {
      URL wsdl = VirtualBox.class.getClassLoader().getResource("vboxwebService.wsdl");
      if (wsdl == null)
        throw new LinkageError("vboxwebService.wsdl not found, but it should have been in the jar");
      VboxService svc = new VboxService(wsdl, new QName("http://www.virtualbox.org/Service", "vboxService"));
      VboxPortType port = svc.getVboxServicePort();
      ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
      String vbox = port.iWebsessionManagerLogon(username, password);
      return new IVirtualBox(vbox, port);
    } catch (InvalidObjectFaultMsg e) {
      throw new WebServiceException(e);
    } catch (RuntimeFaultMsg e) {
      throw new WebServiceException(e);
    } finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  public static List<VirtualBoxMachine> getMachines(VirtualBoxHost host) {
    List<VirtualBoxMachine> result = new ArrayList<VirtualBoxMachine>();
    IVirtualBox box = connect(host.getUrl(), host.getUsername(), host.getPassword());
    for (IMachine machine : box.getMachines()) {
      result.add(new VirtualBoxMachine(host, machine.getName()));
    }
    return result;
  }

  public static long startVm(VirtualBoxMachine machine) {
    return startVm(machine.getHost(), machine.getName());
  }

  public static void stopVm(VirtualBoxMachine machine) {
    stopVm(machine.getHost(), machine.getName());
  }

  public static long startVm(VirtualBoxHost host, String vmName) {
    return startVm(host.getUrl(), host.getUsername(), host.getPassword(), vmName);
  }

  public static void stopVm(VirtualBoxHost host, String vmName) {
    stopVm(host.getUrl(), host.getUsername(), host.getPassword(), vmName);
  }

  private static long startVm(String url, String username, String password, String vmName) {
    IVirtualBox box = connect(url, username, password);
    ISession session = box.getSessionObject();
    IMachine machine = box.findMachine(vmName);
    IProgress progress = box.openRemoteSession(
        session,
        machine.getId(),
        "headless", // sessionType
        "" // env
    );
    progress.waitForCompletion(-1);
    box.logoff();
    return progress.getResultCode();
  }

  private static void stopVm(String url, String username, String password, String vmName) {
    IVirtualBox box = connect(url, username, password);
    IMachine machine = box.findMachine(vmName);
    ISession session = box.getSessionObject();
    box.openExistingSession(box.getSessionObject(), machine.getId());
    session.getConsole().powerDown();
    box.logoff();
  }
}
