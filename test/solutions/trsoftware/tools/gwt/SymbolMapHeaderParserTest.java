package solutions.trsoftware.tools.gwt;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.io.FileUtils;
import solutions.trsoftware.commons.shared.util.MultimapDecorator;
import solutions.trsoftware.tools.gwt.artifacts.SymbolMapHeaderParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * @author Alex
 * @since 3/8/2018
 */
public class SymbolMapHeaderParserTest extends TestCase {

  public void testParsing() throws Exception {
    Path dummySymbolMap = createTempFile(
        "# { 2 }\n" +
            "# { 'chromeWithColNumbers' : 'false' , 'debug' : 'off' , 'user.agent' : 'ie10' }\n" +
            "# { 'chromeWithColNumbers' : 'false' , 'debug' : 'on' , 'user.agent' : 'ie10' }\n" +
            "# jsName, jsniIdent, className, memberName, sourceUri, sourceLine, fragmentNumber\n" +
            "boolean[],,boolean[],,Unknown,0,-1\n" +
            "byte[],,byte[],,Unknown,0,-1\n" +
            "char[],,char[],,Unknown,0,-1"
    );
    SymbolMapHeaderParser parser = new SymbolMapHeaderParser(dummySymbolMap);
    System.out.println("Parsed header: " + parser);
    assertEquals(2, parser.getPermutationId());
    assertEquals(
        Arrays.asList(
            "{ 'chromeWithColNumbers' : 'false' , 'debug' : 'off' , 'user.agent' : 'ie10' }",
            "{ 'chromeWithColNumbers' : 'false' , 'debug' : 'on' , 'user.agent' : 'ie10' }"),
        parser.getSelectionPropertyLines());
    Multimap<String, String> expectedProps = new MultimapDecorator<String, String>(LinkedHashMultimap.create())
        .put("chromeWithColNumbers", "false")
        .put("debug", "off", "on")
        .put("user.agent", "ie10")
        .getMultimap();
    assertEquals(expectedProps, parser.getSelectionProperties());

  }

  private Path createTempFile(String content) throws IOException {
    Path tempFile = Files.createTempFile("", ".symbolMap");
    try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
      writer.write(content);
      writer.newLine();
    }
    System.out.println("Created temp file:");
    System.out.println("--------------------------------------------------------------------------------");
    System.out.println(tempFile);
    System.out.println("--------------------------------------------------------------------------------");
    System.out.println(content);
    System.out.println("--------------------------------------------------------------------------------");
    return FileUtils.deleteOnExit(tempFile);
  }

}