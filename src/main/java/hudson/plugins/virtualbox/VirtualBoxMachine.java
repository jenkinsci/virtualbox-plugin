package hudson.plugins.virtualbox;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * @author Evgeny Mandrikov
 */
public class VirtualBoxMachine implements Serializable, Comparable<VirtualBoxMachine> {

  private final VirtualBoxCloud host;
  private final String name;

  @DataBoundConstructor
  public VirtualBoxMachine(VirtualBoxCloud host, String name) {
    this.host = host;
    this.name = name;
  }

  public VirtualBoxCloud getHost() {
    return host;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VirtualBoxMachine)) {
      return false;
    }
    VirtualBoxMachine that = (VirtualBoxMachine) obj;
    if (host != null ? !host.equals(that.host) : that.host != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    return 31 * result + (host != null ? host.hashCode() : 0);
  }

  public int compareTo(VirtualBoxMachine obj) {
    // TODO Godin compare host ? check on null?
    return name.compareTo(obj.getName());
  }

  @Override
  public String toString() {
    return new StringBuffer()
        .append("VirtualBoxMachine{")
        .append("host=").append(host).append(",")
        .append("name='").append(name).append("'")
        .append("}").toString();
  }
}
