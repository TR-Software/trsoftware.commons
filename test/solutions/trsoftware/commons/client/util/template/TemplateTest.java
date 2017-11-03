package solutions.trsoftware.commons.client.util.template;

import static solutions.trsoftware.commons.client.util.MapUtils.stringMap;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Dec 10, 2008
 *
 * @author Alex
 */
public class TemplateTest extends TestCase {

  private final Template template = new Template(Arrays.asList(
      new VariablePart("foo"),
      new StringPart(" is a type of "),
      new VariablePart("bar"),
      new StringPart(".")
  ));

  public void testRender() throws Exception {
    assertEquals("Bonobo is a type of ape.", template.render(
        stringMap(
            "foo", "Bonobo",
            "bar", "ape")));
  }

  public void testRenderPositional() throws Exception {
    assertEquals("5 is a type of number.", template.renderPositional(5, "number"));
  }

  public void testPrintf() throws Exception {
    assertEquals("5 is a type of number.", Template.printf("%d is a type of %s.", 5, "number"));
    assertEquals("5 is a type of number", Template.printf("%foobar is a type of %bar", 5, "number"));
  }

}