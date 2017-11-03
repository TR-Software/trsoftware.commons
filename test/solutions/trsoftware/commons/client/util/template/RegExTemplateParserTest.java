package solutions.trsoftware.commons.client.util.template;

import solutions.trsoftware.commons.client.util.MapUtils;
import junit.framework.TestCase;

import java.util.HashMap;

public class RegExTemplateParserTest extends TestCase {

  private final String variablePattern = "%(\\w+)";
  private final String commentPattern = "/\\*.*?\\*/";

  public void testParseTemplate() throws Exception {
    HashMap<String, String> substitutions = MapUtils.stringMap(
        "d", "Bonobo",
        "v123_foo", "Gorilla",
        "something", "Great Ape");

    assertEquals("Bonobo and Gorilla are types of Great Ape",
        parse("%d and %v123_foo /* this is a comment */are types of %something").render(substitutions));

    assertEquals("Bonobo and Gorilla are types of Great Ape",
        parse("%d and %v123_foo /* this is a comment */are types of %something/* this is a comment at the end*/").render(substitutions));

    assertEquals("Bonobo[foo] and Gorilla are types of Great Ape",
        parse("%d[foo] and %v123_foo /* this is a comment */are types of %something").render(substitutions));

    // now try parsing a template that has no variables
    assertEquals("Bonobo and Gorilla are types of Great Ape",
        parse("/* this is a comment */Bonobo and Gorilla /* this is a comment */are types of Great Ape").render(substitutions));
    // now try parsing a template that has no comments
    assertEquals("Bonobo and Gorilla are types of Great Ape",
        parse("%d and %v123_foo are types of %something").render(substitutions));
    // now try parsing a template that has neither comments nor variables
    assertEquals("Bonobo and Gorilla are types of Great Ape",
        parse("Bonobo and Gorilla are types of Great Ape").render(substitutions));

    // now test with null variable and/or comment patterns
    assertEquals("Bonobo and Gorilla /* this is a comment */are types of Great Ape", new RegExTemplateParser(variablePattern, null)
        .parseTemplate("%d and %v123_foo /* this is a comment */are types of %something").render(substitutions));

    assertEquals("%d and %v123_foo are types of %something", new RegExTemplateParser(null, commentPattern)
        .parseTemplate("%d and %v123_foo /* this is a comment */are types of %something").render(substitutions));

    assertEquals("%d and %v123_foo /* this is a comment */are types of %something", new RegExTemplateParser(null, null)
        .parseTemplate("%d and %v123_foo /* this is a comment */are types of %something").render(substitutions));
  }

  private Template parse(String templateString) {
    return new RegExTemplateParser(variablePattern, commentPattern).parseTemplate(templateString);
  }
}