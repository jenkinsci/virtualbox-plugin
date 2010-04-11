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

  private static IVirtualBox connect(VirtualBoxHost host) {
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
      ((BindingProvider) port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, host.getUrl());
      String vbox = port.iWebsessionManagerLogon(host.getUsername(), host.getPassword());
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
    IVirtualBox box = connect(host);
    for (IMachine machine : box.getMachines()) {
      result.add(new VirtualBoxMachine(host, machine.getName()));
    }
    box.logoff();
    return result;
  }

  public static long startVm(VirtualBoxMachine vbMachine) {
    IVirtualBox box = connect(vbMachine.getHost());
    ISession session = box.getSessionObject();
    IMachine machine = box.findMachine(vbMachine.getName());
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

  public static void stopVm(VirtualBoxMachine vbMachine) {
    IVirtualBox box = connect(vbMachine.getHost());
    IMachine machine = box.findMachine(vbMachine.getName());
    ISession session = box.getSessionObject();
    box.openExistingSession(box.getSessionObject(), machine.getId());
    session.getConsole().powerDown();
    box.logoff();
  }
}
