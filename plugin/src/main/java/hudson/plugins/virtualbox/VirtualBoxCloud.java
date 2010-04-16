package hudson.plugins.virtualbox;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner;
import hudson.util.FormValidation;
import hudson.util.Scrambler;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * {@link Cloud} implementation for VirtualBox.
 *
 * @author Evgeny Mandrikov
 */
public class VirtualBoxCloud extends Cloud {

  private static final Logger LOG = Logger.getLogger(VirtualBoxCloud.class.getName());

  private final String url;
  private final String username;
  private final String password;

  /**
   * Lazily computed list of virtual machines from this host.
   */
  private transient List<VirtualBoxMachine> virtualBoxMachines = null;

  @DataBoundConstructor
  public VirtualBoxCloud(String displayName, String url, String username, String password) {
    super(displayName);
    this.url = url;
    this.username = username;
    this.password = Scrambler.scramble(Util.fixEmptyAndTrim(password));
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

    /**
     * For UI.
     */
    @SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
    public FormValidation doTestConnection(
        @QueryParameter String url,
        @QueryParameter String username,
        @QueryParameter String password
    ) {
      LOG.info("Testing connection to " + url + " with username " + username);
      try {
        VirtualBoxUtils.getMachines(new VirtualBoxCloud("testConnection", url, username, password));
        return FormValidation.ok(Messages.VirtualBoxHost_success());
      } catch (Throwable e) {
        return FormValidation.error(e.getMessage());
      }
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
