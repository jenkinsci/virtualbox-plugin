package hudson.plugins.virtualbox;

import java.io.Serializable;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Evgeny Mandrikov
 */
public class VirtualBoxMachine implements Serializable, Comparable<VirtualBoxMachine> {

  private final VirtualBoxCloud host;
  private final String machineName;
  private final String machineId;
  private final String snapshotId;

  @DataBoundConstructor
  public VirtualBoxMachine(VirtualBoxCloud host, String machineName, String machineId, String snapshotId) {
    this.host = host;
    this.machineName = machineName;
    this.machineId = machineId;
    this.snapshotId = snapshotId;
  }

  public VirtualBoxCloud getHost() {
    return host;
  }

  public String getName() {
    return machineName;
  }

  public String getMachineId() {
    return machineId;
  }

  public String getMachineName() {
    return machineName;
  }

  public String getSnapshotId() {
    return snapshotId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final VirtualBoxMachine other = (VirtualBoxMachine) obj;
    if (this.host != other.host && (this.host == null || !this.host.equals(other.host))) {
      return false;
    }
    if ((this.machineName == null) ? (other.machineName != null) : !this.machineName.equals(other.machineName)) {
      return false;
    }
    if ((this.machineId == null) ? (other.machineId != null) : !this.machineId.equals(other.machineId)) {
      return false;
    }
    if ((this.snapshotId == null) ? (other.snapshotId != null) : !this.snapshotId.equals(other.snapshotId)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    return hash;
  }

  public int compareTo(VirtualBoxMachine obj) {
    // TODO Godin compare host ? check on null?
    return machineName.compareTo(obj.getName());
  }

  @Override
  public String toString() {
    return "VirtualBoxMachine{" + "host=" + host + ", machineName=" + machineName + ", machineId=" + machineId + ", snapshotId=" + snapshotId + '}';
  }
}
