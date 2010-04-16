package hudson.plugins.virtualbox;

import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author Evgeny Mandrikov
 */
public class VirtualBoxCloudTest extends HudsonTestCase {
  public void testConfigRoundtrip() throws Exception {
    VirtualBoxCloud orig = new VirtualBoxCloud("Test", "http://localhost:18083", "godin", "12345");
    hudson.clouds.add(orig);
    submit(createWebClient().goTo("configure").getFormByName("config"));

    assertEqualBeans(orig, hudson.clouds.iterator().next(), "name,url,username,password");
  }
}
