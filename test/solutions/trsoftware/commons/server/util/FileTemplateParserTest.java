package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.client.util.MapUtils;
import solutions.trsoftware.commons.client.util.StringUtils;
import solutions.trsoftware.commons.client.util.template.Template;
import solutions.trsoftware.commons.server.testutil.TempFileTestCase;

import java.io.FileOutputStream;
import java.io.PrintStream;

import static solutions.trsoftware.commons.client.util.MapUtils.hashMap;

public class FileTemplateParserTest extends TempFileTestCase {

  private String templateString = "<!-- Example template -->\nHello ${NAME}, \nYour account number is ${ACCT_NUM}.\nTake care!";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    PrintStream out = new PrintStream(new FileOutputStream(tempFile));
    out.print(templateString);
    out.close();
    System.out.println("Wrote the following template to temp file @ " + tempFile + ":");
    System.out.println(templateString);
  }

  /** Checks variable substitution in a template file located in the project {@code resources} directory */
  public void testFileTemplate() throws Exception {
    FileTemplateParser instance = FileTemplateParser.getInstance();
    Template t = instance.getTemplate(tempFile);
    assertNotNull(t);
    // make sure the parsed template files are cached
    assertSame(t, instance.getTemplate(tempFile));
    // apply and print the templates
    System.out.println();
    System.out.println("Rendering template file " + tempFile + ":");
    String name = "Foo";
    int number = 1234;
    String result = t.render(hashMap("NAME", name, "ACCT_NUM", number));
    System.out.println(result);
    assertEquals("Hello " + name + ", \nYour account number is " + number + ".\nTake care!",
        result.trim());
  }

}