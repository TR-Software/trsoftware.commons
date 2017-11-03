package solutions.trsoftware.commons.client.util.template;

import junit.framework.TestCase;

/**
 * Apr 13, 2010
 *
 * @author Alex
 */
public class SimpleTemplateParserTest extends TestCase {


  public void testCustomTemplateParser() throws Exception {
    TemplateParser parser = new SimpleTemplateParser("{*", "*}", "/*", "*/");
    assertEquals(
        "AdamLyons is the number 1 player in the world",
        parser.parseTemplate("{*name*}{*lastName*} is the number {*foo*} {*bar*} in the world/* this is a template */")
            .render("lastName", "Lyons", "name", "Adam", "foo", "1", "bar", "player"));
  }
}