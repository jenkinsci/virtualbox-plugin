package hudson.plugins.virtualbox;

import hudson.model.Slave;
import hudson.slaves.SlaveComputer;

/**
 * @author Evgeny Mandrikov
 */
public class VirtualBoxComputer extends SlaveComputer {
  public VirtualBoxComputer(Slave slave) {
    super(slave);
  }

  @Override
  public VirtualBoxSlave getNode() {
    return (VirtualBoxSlave) super.getNode();
  }

}
