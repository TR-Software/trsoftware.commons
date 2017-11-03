package solutions.trsoftware.commons.client.util.template;

import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.client.util.MapUtils;

import java.util.Map;

/**
 * Dec 10, 2008
 *
 * @author Alex
 */
public class SimpleTemplateParserGwtTest extends CommonsGwtTestCase {

  public void testStringTemplate() throws Exception {
    assertEquals("Bonobo",
        SimpleTemplateParser.parseDefault("${foo}").render(
            map("foo", "Bonobo")));

    assertEquals(" Bonobo",
        SimpleTemplateParser.parseDefault(" ${foo}").render(
            map("foo", "Bonobo")));
    
    assertEquals(" Bonobo ",
        SimpleTemplateParser.parseDefault(" ${foo}").render(
            map("foo", "Bonobo ")));

    assertEquals("",
        SimpleTemplateParser.parseDefault("<!-- stuff -->").render(
            map("foo", "Bonobo ")));

    assertEquals("",
        SimpleTemplateParser.parseDefault("<!-- \nstuff \n-->").render(
            map("foo", "Bonobo ")));

    assertEquals("  ",
        SimpleTemplateParser.parseDefault(" <!-- stuff --> ").render(
            map("foo", "Bonobo ")));


    assertEquals("asdf rwer Bonobo def chimp",
        SimpleTemplateParser.parseDefault("asdf rwer ${foo} def ${bar}").render(
            map("foo", "Bonobo",
                "bar", "chimp")));

    // check multiline templates
    assertEquals("asdf \nrwer Bonobo\n def chimp",
        SimpleTemplateParser.parseDefault("asdf \nrwer ${foo}\n def ${bar}").render(
            map("foo", "Bonobo",
                "bar", "chimp")));

    // check multiline templates with comments
    assertEquals("asdf chimp",
        SimpleTemplateParser.parseDefault("asdf <!--\nrwer ${foo}\n def -->${bar}").render(
            map("foo", "Bonobo",
                "bar", "chimp")));

    // missing substitutions should result in an empty string inserted
    assertEquals("asdf rwer  def chimp",
        SimpleTemplateParser.parseDefault("asdf rwer ${foo} def ${bar}").render(
            map("bar", "chimp")));

    // make sure variables can't span multiple lines
    AssertUtils.assertThrows(IllegalArgumentException.class,
        new Runnable() {
          public void run() {
            SimpleTemplateParser.parseDefault("asdf rwer ${f\noo} def ${bar}");
          }
        });

    // make sure comments and variables hve to be terminated
    AssertUtils.assertThrows(IllegalArgumentException.class,
        new Runnable() {
          public void run() {
            SimpleTemplateParser.parseDefault("asdf rwer ${f\noo} def ${bar");
          }
        });
    AssertUtils.assertThrows(IllegalArgumentException.class,
        new Runnable() {
          public void run() {
            SimpleTemplateParser.parseDefault("asdf rwer ${f\noo} def <!--bar}");
          }
        });
  }

  /** Creates a map given the args in order key1, value2, key2, value2, ... */
  private static Map<String, String> map(String... keyValuePairs) {
    return MapUtils.stringMap(keyValuePairs);
  }
}