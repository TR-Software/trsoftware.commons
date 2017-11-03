package solutions.trsoftware.commons.client.util;

import com.google.gwt.benchmarks.client.Benchmark;
import com.google.gwt.benchmarks.client.IntRange;
import com.google.gwt.benchmarks.client.Operator;
import com.google.gwt.benchmarks.client.RangeField;
import static solutions.trsoftware.commons.client.util.Levenshtein.editDistance;
import static solutions.trsoftware.commons.client.util.StringUtils.methodCallToString;
import static solutions.trsoftware.commons.client.util.StringUtils.randString;

/**
 * Mar 14, 2011
 *
 * @author Alex
 */
public class LevenshteinGwtBenchmark extends Benchmark {

  // NOTE: fields used as parameter ranges (with @RangeField annotation) must not be private

  IntRange stringLengthRange = new IntRange(100, Integer.MAX_VALUE, Operator.ADD, 100);

  private String s;
  private String t;

  // HHKqWNAOXOHSpcs UwZ twoUsztFcaVelL
  // HHKqWNAOXOHSpcs ayH twoUsztFcaVelL

  // BmvSXptlyTxTpOycmFXNnylgTCgDunmXIXjkhJAzjoqKYgrBaqmXggGQhcFHmlzlwjbTNtKCQuVZYwuSbInOzzNfhPCOuGtizmVTgTciftRfKURITKumlgYsJwmFegJ uyF rBXMIqGZPLDbcERaAlndGMlCaGOAWJZKseSuFasuADARIEbCFkKCaeucLdMxtJLgFfPqyVITOIxigHQpleQPBBXOUiyrFNszQgjxvmQFaJvTFDrgZvAQCVsYDORawm
  // BmvSXptlyTxTpOycmFXNnylgTCgDunmXIXjkhJAzjoqKYgrBaqmXggGQhcFHmlzlwjbTNtKCQuVZYwuSbInOzzNfhPCOuGtizmVTgTciftRfKURITKumlgYsJwmFegJ ifP rBXMIqGZPLDbcERaAlndGMlCaGOAWJZKseSuFasuADARIEbCFkKCaeucLdMxtJLgFfPqyVITOIxigHQpleQPBBXOUiyrFNszQgjxvmQFaJvTFDrgZvAQCVsYDORawm

  @Override
  public String getModuleName() {
    return "solutions.trsoftware.commons.TestCommons";
  }

  private void initRandomStrings(Integer stringLength) {
    s = randString(stringLength);
    t = randString(stringLength);
  }


  // testEditDistance methods:
  public void testEditDistance(@RangeField("stringLengthRange") Integer stringLength) {
    System.out.println("Calling " + methodCallToString("editDistance", s, t));
    int editDistance = editDistance(s, t);
    System.out.println("Got the following edit distance:\n" + editDistance);
    System.out.println();  // empty line
  }
  public void beginEditDistance(Integer stringLength) {
    initRandomStrings(stringLength);
  }
  // Required for JUnit
  public void testEditDistance() {
  }


  // testEditSequence methods:
  public void testEditSequence(@RangeField("stringLengthRange") Integer stringLength) {
    System.out.println("Calling " + methodCallToString("editSequence", s, t));
    Levenshtein.EditSequence editSequence = Levenshtein.editSequence(s, t);
    System.out.println("Got the following edit sequence:\n" + editSequence);
    System.out.println();  // empty line
  }
  public void beginEditSequence(Integer stringLength) {
    initRandomStrings(stringLength);
  }
  // Required for JUnit
  public void testEditSequence() {
  }

  /**
   * Randomly replaces characters in the middle 10% of the string, s.t. the
   * result will still have the same prefix and suffix as the input string.
   *
   * @param str
   * @return
   */
  private String replaceMiddle(String str) {
    int len = str.length();
    if (len < 5)
      return str;
    int halfSpan = Math.max(1, (int)(.05 * len));
    int mid = len / 2;
    int first = mid - halfSpan;
    int last = mid + halfSpan;
    int spanlen = last - first + 1;
    return new StringBuilder(str).replace(first, last + 1, randString(spanlen)).toString();
  }

  // testEditDistanceOptimized methods:
  public void testEditDistanceOptimized(@RangeField("stringLengthRange") Integer stringLength) {
    System.out.println("Calling " + methodCallToString("editDistance", s, t, true, true));
    int editDistance = editDistance(s, t, true, true);
    System.out.println("Got the following edit distance:\n" + editDistance);
    System.out.println();  // empty line
  }
  public void beginEditDistanceOptimized(Integer stringLength) {
    initRandomStringsWithSharedPrefixSuffix(stringLength);
  }
  // Required for JUnit
  public void testEditDistanceOptimized() {
  }

  private void initRandomStringsWithSharedPrefixSuffix(Integer stringLength) {
    s = randString(stringLength);
    t = replaceMiddle(s);  // create a string that can take advantage of the common prefix and suffix optimizations
    assertEquals((int)stringLength, s.length());
    assertEquals((int)stringLength, t.length());
    assertFalse(s.equals(t));
    int editDistance = editDistance(s, t, true, true);
    // assert that the strings differ by between 5% and 20% percent
    assertTrue(editDistance > .05*stringLength);
    assertTrue(editDistance < .20*stringLength);
  }


  // testEditSequenceOptimized methods:
  public void testEditSequenceOptimized(@RangeField("stringLengthRange") Integer stringLength) {
    System.out.println("Calling " + methodCallToString("editSequence", s, t, true, true));
    Levenshtein.EditSequence editSequence = Levenshtein.editSequence(s, t, true, true);
    System.out.println("Got the following edit sequence:\n" + editSequence);
    System.out.println();  // empty line
  }
  public void beginEditSequenceOptimized(Integer stringLength) {
    initRandomStringsWithSharedPrefixSuffix(stringLength);
  }
  // Required for JUnit
  public void testEditSequenceOptimized() {
  }

  // testDiff methods:
  public void testDiff(@RangeField("stringLengthRange") Integer stringLength) {
    System.out.println("Calling " + methodCallToString("diff", s, t));
    Levenshtein.Diffs diffs = Levenshtein.diff(s, t);
    System.out.println("Got the following diffs:\n" + diffs);
    System.out.println();  // empty line
  }
  public void beginDiff(Integer stringLength) {
    initRandomStrings(stringLength);
  }
  // Required for JUnit
  public void testDiff() {
  }
}