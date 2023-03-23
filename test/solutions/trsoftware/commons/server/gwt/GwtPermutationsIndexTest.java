package solutions.trsoftware.commons.server.gwt;

import com.google.common.collect.ImmutableSet;
import org.apache.catalina.Context;
import solutions.trsoftware.commons.shared.annotations.Slow;

import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Set;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;

/**
 * @author Alex
 * @since 3/6/2023
 */
@Slow
public class GwtPermutationsIndexTest extends GwtArtifactsTestCase {

  public void testGetAvailablePermutations() throws Exception {
    /*
      1) the "gwtWebModeTests" app contains 2 compiled permutations
    */
    String moduleBasePath = "/" + MODULE_NAME;
    testGetAvailablePermutations(webModeApp, moduleBasePath,
        ImmutableSet.of("C05E049EEAE22C890F9F0EC6E8B006FC", "C25A1304ECE38B2D7FE01C93C9505713"));
    /*
      2) the "gwtHostedModeTests" app does not contain any compiled permutations
         (since it runs the Java code directly, instead of compiled JS)
    */
    testGetAvailablePermutations(hostedModeApp, moduleBasePath, Collections.emptySet());
    // 3) invalid module module path
    for (Context app : embeddedTomcatServer.getContexts()) {
      assertThrows(IllegalArgumentException.class, (Runnable)() ->
          testGetAvailablePermutations(app, "/invalid", Collections.emptySet()));
    }
  }

  private void testGetAvailablePermutations(Context app, String moduleBasePath, Set<String> expected) {
    GwtPermutationsIndex index = new GwtPermutationsIndex();
    ServletContext servletContext = app.getServletContext();
    Set<String> permutations = index.getAvailablePermutations(moduleBasePath, servletContext);
    System.out.printf("Permutations found in %s%s:%n  %s%n", servletContext.getContextPath(), moduleBasePath, permutations);
    assertEquals(expected, permutations);
    // the result should be cached (subsequent invocations should return the same Set instance)
    assertSame(permutations, index.getAvailablePermutations(moduleBasePath, servletContext));
  }
}