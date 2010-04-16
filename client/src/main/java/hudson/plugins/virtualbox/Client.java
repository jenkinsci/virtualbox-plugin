package hudson.plugins.virtualbox;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Enumeration;


/**
 * Inspired by Swarm Plugin.
 *
 * @author Evgeny Mandrikov
 */
public class Client {
  private String macAddress;
  private final DatagramSocket socket;

  public Client() throws IOException {
    socket = new DatagramSocket();
    socket.setBroadcast(true);
    this.macAddress = getMacAddress();
  }

  public void start() throws Exception {
    DatagramPacket packet = discover();

    InputStream is = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
    Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
    String url = dom.getElementsByTagName("url").item(0).getTextContent();

    System.out.println("Hudson: " + url);
    hudson.remoting.Launcher.main("-jnlpUrl", getJnlp(url));
  }

  public String getJnlp(String hudsonUrl) {
    return hudsonUrl + "plugin/virtualbox/getSlaveAgent?macAddress=" + macAddress;
  }

  /**
   * @return MAC Address, null if not found
   */
  private String getMacAddress() {
    try {
      Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface ni = (NetworkInterface) interfaces.nextElement();
        byte[] mac = ni.getHardwareAddress();
        if (mac != null) {
          StringBuilder sb = new StringBuilder();
          for (byte aMac : mac) {
            sb.append(String.format("%02X", aMac));
          }
          String macAddress = sb.toString();
          System.out.println("MAC Address for interface '" + ni.getDisplayName() + "': " + macAddress);
          return macAddress;
        }
      }
    } catch (SocketException e) {
      // ignore
    }
    return null;
  }

  protected void sendBroadcast() throws IOException {
    DatagramPacket packet = new DatagramPacket(new byte[0], 0);
    packet.setAddress(InetAddress.getByName("255.255.255.255"));
    packet.setPort(Integer.getInteger("hudson.udp", 33848));
    socket.send(packet);
  }

  protected DatagramPacket discover() throws IOException {
    sendBroadcast();
    long limit = System.currentTimeMillis() + 5 * 1000;
    try {
      socket.setSoTimeout(Math.max(1, (int) (limit - System.currentTimeMillis())));

      DatagramPacket packet = new DatagramPacket(new byte[2048], 2048);
      socket.receive(packet);

      return packet;
    } catch (SocketTimeoutException e) {
      // Timeout
      return null;
    }
  }

  public static void main(String[] args) throws Exception {
    new Client().start();
  }
}
