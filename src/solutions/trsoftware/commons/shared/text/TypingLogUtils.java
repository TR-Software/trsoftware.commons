package solutions.trsoftware.commons.shared.text;

import solutions.trsoftware.commons.client.util.Levenshtein;
import solutions.trsoftware.commons.client.util.TimeUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Alex, 7/16/2017
 */
public class TypingLogUtils {

  /**
   * @return the most basic sequence of TypingEdits that will produce the given
   * text with constant delay between keystrokes.
   */
  public static List<TypingEdit> dummyTypingEditSequence(Language language, String textTyped, int delayBetweenChars) {
    ArrayList<TypingEdit> ret = new ArrayList<TypingEdit>();
    String[] words = language.getTokenizer().tokenize(textTyped);
    TextCharCounts textCharCounts = new TextCharCounts(words, language);
    if (!language.isLogographic()) {
      // append a space to the end of each word (except for the last word)
      for (int w = 0; w < words.length-1; w++) {
        words[w] += ' ';
      }
    }
    int totalTime = 0;
    for (int w = 0; w < words.length; w++) {
      String word = words[w];
      for (int c = 0; c < word.length(); c++) {
        ret.add(new TypingEdit(textCharCounts.getCharCountUpToWord(w), Arrays.<Levenshtein.EditOperation>asList(new Levenshtein.Insertion(c, word.charAt(c))), totalTime += delayBetweenChars));
      }
    }
    return ret;
  }

  /**
   * @return the most basic {@link TypingLog} for given text (with no mistakes)
   * with the given constant delay between keystrokes.
   */
  public static TypingLog dummyTypingLog(Language language, String textTyped, int delayBetweenChars) {
    // compute charTimings from the sequence of edits
    List<TypingEdit> typingEdits = dummyTypingEditSequence(language, textTyped, delayBetweenChars);
    int[] charTimings = new int[typingEdits.size()];
    for (int i = 0; i < charTimings.length; i++) {
      charTimings[i] = typingEdits.get(i).getTime();
    }
    return new TypingLog(textTyped, language, charTimings, typingEdits);
  }

  /**
   * @return the most basic {@link TypingLog} for given text (with no mistakes) at the given WPM.
   */
  public static TypingLog dummyTypingLog(String text, Language language, double wpm) {
    double cpm = TypingSpeed.wpmToCpm(wpm, language);
    int delayBetweenChars = (int)(cpm / TimeUnit.MINUTES.toMillis(1));
    return dummyTypingLog(language, text, delayBetweenChars);
  }
}
