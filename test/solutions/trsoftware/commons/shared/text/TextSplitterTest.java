package solutions.trsoftware.commons.shared.text;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.List;

/**
 * @author Alex
 * @since 11/3/2018
 */
public class TextSplitterTest extends TestCase {

  public void testSplit() throws Exception {
    String[] inputs = new String[] {
        "adsfasdf asdfasdf asdfasdf asdfasdf",
        "There is no pain, you are receding.",
        "There is no pain, you are receding. A distant ship smoke on the horizon. You are only coming through in waves. Your lips move but I can't hear what you're saying.",
        "Well, the way they make shows is, they make one show. That show's called a pilot. Then they show that show to the people who make shows, and on the strength of that one show they decide if they're going to make more shows. Some pilots get picked and become television programs. Some don't, become nothing. She starred in one of the ones that became nothing.",
        "Hey Mr. Tambourine Man, play a song for me, I'm not sleepy and there is no place I'm going to; Hey Mr. Tambourine Man, play a song for me, in the jingle jangle morning I'll come followin' you."
    };
    for (String text : inputs) {
      String hr = StringUtils.repeat('=', text.length());
      System.out.println(hr);
      System.out.println(text);
      System.out.println(hr);
      TextSplitter splitter = new TextSplitter(text, Language.ENGLISH);
      int desiredSegments = 8;
      List<TextSplitter.WordLine> result = splitter.split(desiredSegments);
      printResult(result);
      AssertUtils.assertThat(result.size()).isLessThanOrEqualTo(desiredSegments);
    }
  }

  private void printResult(List<TextSplitter.WordLine> lines) {
    for (int i = 0; i < lines.size(); i++) {
      TextSplitter.WordLine line = lines.get(i);
      System.out.printf("%2d | %2d | %s%n", i, line.width(), line.getText());
    }
  }
}