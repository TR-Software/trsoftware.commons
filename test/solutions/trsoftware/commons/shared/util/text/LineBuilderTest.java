package solutions.trsoftware.commons.shared.util.text;

import solutions.trsoftware.commons.shared.BaseTestCase;

import java.util.Arrays;

/**
 * @author Alex
 * @since 10/30/2023
 */
public class LineBuilderTest extends BaseTestCase {

  public void testLineBuilder() throws Exception {
    LineBuilder builder = new LineBuilder();
    int nLines = 3;
    int expectedLineCount = 1;
    for (int i = 0; i < nLines; i++) {
      assertEquals(expectedLineCount, builder.lineCount());
      assertEquals("", builder.line().toString());
      builder.line().append("L").append(i);
      assertEquals(builder.line().toString(), "L"+i);
      assertSame(builder.line(), builder.line(i));
      builder.newLine();
      assertEquals(++expectedLineCount, builder.lineCount());
    }
    assertEquals(Arrays.asList("L0", "L1", "L2", ""), builder.buildLines());
    assertEquals("L0\nL1\nL2\n", builder.joinLines("\n"));
  }

}