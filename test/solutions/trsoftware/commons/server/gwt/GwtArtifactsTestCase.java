package solutions.trsoftware.commons.server.gwt;

import org.apache.catalina.Context;
import solutions.trsoftware.commons.server.io.ResourceLocator;
import solutions.trsoftware.commons.server.testutil.EmbeddedTomcatServer;
import solutions.trsoftware.commons.shared.BaseTestCase;

import javax.servlet.ServletException;

/**
 * Starts an embedded Tomcat server with the webapps in {@value #TEST_WEBAPPS_PATH}.
 * <p>
 * Can be used as a base class for testing the utils in the {@link solutions.trsoftware.commons.server.gwt} package.
 *
 * @author Alex
 * @since 3/6/2023
 */
public class GwtArtifactsTestCase extends BaseTestCase {

  /**
   * Base resource path for the webapps to run with the embedded Tomcat server.
   * Should contain subdirectories named "gwtHostedModeTests" and "gwtWebModeTests".
   */
  protected static final String TEST_WEBAPPS_PATH = "/GwtArtifactsTestCase/";
  /**
   * The GWT module contained by the webapps in {@value #TEST_WEBAPPS_PATH}
   */
  protected static final String MODULE_NAME = "solutions.trsoftware.commons.TestCommons.JUnit";

  protected Context hostedModeApp;
  protected Context webModeApp;
  protected EmbeddedTomcatServer embeddedTomcatServer;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    embeddedTomcatServer = new EmbeddedTomcatServer();
    addWebApps();
    embeddedTomcatServer.start();
  }

  protected void addWebApps() throws ServletException {
    hostedModeApp = addWebapp("gwtHostedModeTests");
    webModeApp = addWebapp("gwtWebModeTests");
  }

  @Override
  protected void tearDown() throws Exception {
    if (embeddedTomcatServer != null) {
      embeddedTomcatServer.stop();
      embeddedTomcatServer = null;
    }
    hostedModeApp = null;
    webModeApp = null;
    super.tearDown();
  }

  protected Context addWebapp(String name) throws ServletException {
    ResourceLocator webAppDir = new ResourceLocator(TEST_WEBAPPS_PATH + name + "_webapp", getClass());
    String filePath = webAppDir.toFilepath();
    assertNotNull(filePath);
    return embeddedTomcatServer.addWebapp("/" + name, filePath);
  }
}
