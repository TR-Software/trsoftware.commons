package solutions.trsoftware.commons.client.auth;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;

/**
 * @author Alex
 * @since 3/2/2024
 */
public class IdConfigurationTest extends CommonsGwtTestCase {

  public void testIsEqualTo() throws Exception {
    assertEqual(IdConfiguration.create("client1"), IdConfiguration.create("client1"));
    assertEqual(
        IdConfiguration.create("client1").set("auto_select", true).cast(),
        IdConfiguration.create("client1").set("auto_select", true).cast()
    );
    // the "callback" property should be excluded from the comparisons
    assertEqual(
        IdConfiguration.create("client1").set("callback", JavaScriptObject.createFunction()).cast(),
        IdConfiguration.create("client1").set("callback", JavaScriptObject.createFunction()).cast()
    );

    assertNotEqual(IdConfiguration.create("client1"), null);
    assertNotEqual(IdConfiguration.create("client1"), IdConfiguration.create("client2"));
    assertNotEqual(
        IdConfiguration.create("client1"),
        IdConfiguration.create("client1").set("auto_select", true).cast()
    );
    assertNotEqual(
        IdConfiguration.create("client1").set("auto_select", false).cast(),
        IdConfiguration.create("client1").set("auto_select", true).cast()
    );

  }

  private void assertEqual(IdConfiguration c1, IdConfiguration c2) {
    assertTrue(Strings.lenientFormat("%s.isEqualTo(%s)", JsonUtils.stringify(c1), JsonUtils.stringify(c2)),
        c1.isEqualTo(c2));
  }

  private void assertNotEqual(IdConfiguration c1, IdConfiguration c2) {
    assertFalse(Strings.lenientFormat("%s.isEqualTo(%s)", JsonUtils.stringify(c1), JsonUtils.stringify(c2)),
        c1.isEqualTo(c2));
  }
}