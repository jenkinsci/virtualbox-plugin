package hudson.plugins.virtualbox;

import hudson.model.Slave;
import hudson.slaves.SlaveComputer;
import java.io.IOException;
import org.kohsuke.stapler.HttpResponse;

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

  @Override
  public HttpResponse doDoDelete() throws IOException {
    // TODO powerOff on delete
    return super.doDoDelete();
  }
}
