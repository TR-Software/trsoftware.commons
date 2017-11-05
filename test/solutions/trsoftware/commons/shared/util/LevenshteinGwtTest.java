/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.shared.util;

import com.google.gwt.core.shared.GWT;
import solutions.trsoftware.commons.Slow;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;

/**
 * Mar 14, 2011
 *
 * @author Alex
 */
public class LevenshteinGwtTest extends CommonsGwtTestCase {

  LevenshteinTest delegate = new LevenshteinTest();

  public void testDiff() throws Exception {
    delegate.testDiff();
  }

  public void testDiffHelperAddDiffsNoCS() throws Exception {
    delegate.testDiffHelperAddDiffsNoCS();
  }

  public void testDiffHelperUnmergedDiffsGivenLCS() throws Exception {
    delegate.testDiffHelperUnmergedDiffsGivenLCS();
  }

  public void testEditDistance() throws Exception {
    delegate.testEditDistance();
  }

  public void testEditDistanceIncremental() throws Exception {
    delegate.testEditDistanceIncremental();
  }

  public void testEditDistanceIncrementalPerformance() throws Exception {
    // generate strings that are long-enough to make an impact on performance in web mode but not take too long to compute
    int n = 700;
    int m = 500;
    if (!GWT.isScript()) {
      // make them longer for hosted mode (which is faster than web mode)
      n*=20;
      m*=20;
    }
    String s = StringUtils.randString(n);
    String t = StringUtils.randString(m);
    delegate.checkEditDistanceIncrementalPerformance(s, t);
  }

  public void testEditDistanceVsSequenceVsDiffs() throws Exception {
    delegate.testEditDistanceVsSequenceVsDiffs();
  }

  public void testEditSequence() throws Exception {
    delegate.testEditSequence();
  }

  public void testIsSubsequence() throws Exception {
    delegate.testIsSubsequence();
  }

  public void testLongestCommonSubsequence() throws Exception {
    delegate.testLongestCommonSubsequence();
  }

  public void testLongStrings() throws Exception {
    // generate strings that are long-engough to make an impact on performance in web mode but not take too long to compute
    String s = StringUtils.randString(100);
    String t =  StringUtils.randString(90);
    delegate.checkLongStrings(s, t);
  }

  @Slow
  public void testRandomStrings() throws Exception {
    delegate.testRandomStrings();
  }

  public void testStripCommonPrefix() throws Exception {
    delegate.testStripCommonPrefix();
  }

  public void testStripCommonSuffix() throws Exception {
    delegate.testStripCommonSuffix();
  }
}