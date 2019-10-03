package hudson.plugins.virtualbox;

import hudson.util.Secret;
import org.jvnet.hudson.test.HudsonTestCase;
//TODO: Use instead: http://javadoc.jenkins-ci.org/org/jvnet/hudson/test/JenkinsRule.html

/**
 * @author Evgeny Mandrikov
 */
public class VirtualBoxCloudTest extends HudsonTestCase {
  public void testConfigRoundtrip() throws Exception {
    //VirtualBoxCloud orig = new VirtualBoxCloud("Test", "http://localhost:18083", "godin", Secret.fromString("12345"));
    //hudson.clouds.add(orig);
    //submit(createWebClient().goTo("configure").getFormByName("config"));

    //assertEqualBeans(orig, hudson.clouds.iterator().next(), "name,url,username,password");
  }
}
