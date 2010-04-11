package hudson.plugins.virtualbox;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner;
import hudson.util.Scrambler;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@link Cloud} implementation for VirtualBox.
 *
 * @author Evgeny Mandrikov
 */
public class VirtualBoxHost extends Cloud {

  private final String url;
  private final String username;
  private final String password;

  private transient List<VirtualBoxMachine> virtualBoxMachines = null;

  @DataBoundConstructor
  public VirtualBoxHost(String displayName, String url, String username, String password) {
    super(displayName);
    this.url = url;
    this.username = username;
    this.password = Scrambler.scramble(Util.fixEmptyAndTrim(password));
    this.virtualBoxMachines = retrieveMachines();
  }

  @Override
  public Collection<NodeProvisioner.PlannedNode> provision(Label label, int excessWorkload) {
    return Collections.emptyList();
  }

  @Override
  public boolean canProvision(Label label) {
    return false;
  }

  private List<VirtualBoxMachine> retrieveMachines() {
    return VirtualBoxUtils.getMachines(this);
  }

  public List<VirtualBoxMachine> getVirtualMachines() {
    if (virtualBoxMachines == null) {
      virtualBoxMachines = retrieveMachines();
    }
    return virtualBoxMachines;
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<Cloud> {
    @Override
    public String getDisplayName() {
      return Messages.VirtualBoxHost_displayName();
    }
  }

  public String getUrl() {
    return url;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return Scrambler.descramble(password);
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append("VirtualBoxHost");
    sb.append("{url='").append(url).append('\'');
    sb.append(", username='").append(username).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
