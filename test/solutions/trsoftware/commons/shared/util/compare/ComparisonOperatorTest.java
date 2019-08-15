/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.shared.util.compare;

import com.google.common.collect.ImmutableBiMap;
import junit.framework.TestCase;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.*;
import static solutions.trsoftware.commons.shared.util.compare.ComparisonOperator.*;

/**
 * @author Alex
 * @since 4/24/2018
 */
public class ComparisonOperatorTest extends TestCase {

  public void testDescribeRelationship() {
    assertEquals(EQ, describeRelationship(1, 1));
    assertEquals(LT, describeRelationship(1, 2));
    assertEquals(GT, describeRelationship(2, 1));
  }

  /**
   * Tests usage as an {@link IntPredicate} on the result of {@link Comparable#compareTo(Object)}
   */
  public void testTest() {
    int cmp = "foo".compareTo("bar");
    assertIntPredicateResult(LT, cmp, false);
    assertIntPredicateResult(LEQ, cmp, false);
    assertIntPredicateResult(EQ, cmp, false);
    assertIntPredicateResult(NEQ, cmp, true);
    assertIntPredicateResult(GT, cmp, true);
    assertIntPredicateResult(GEQ, cmp, true);
  }

  public void testCompare() {
    assertTrue(LT.compare(4, 5));
    assertTrue(LEQ.compare(4, 5));
    assertTrue(NEQ.compare(4, 5));
    assertFalse(EQ.compare(4, 5));
    assertFalse(GT.compare(4, 5));
    assertFalse(GEQ.compare(4, 5));
  }

  /**
   * Tests usage of the {@link ComparisonOperator#compare(Comparable, Object)} method as a {@link BiPredicate}
   */
  public void testBiPredicate() {
    assertBiPredicateResult(LT::compare, 4, 5, true);
    assertBiPredicateResult(LEQ::compare, 4, 5, true);
    assertBiPredicateResult(NEQ::compare, 4, 5, true);
    assertBiPredicateResult(EQ::compare, 4, 5, false);
    assertBiPredicateResult(GT::compare, 4, 5, false);
    assertBiPredicateResult(GEQ::compare, 4, 5, false);
  }

  public void testComparingTo() {
    assertPredicateResult(LT.comparingTo(5), 4, true);
    assertPredicateResult(LEQ.comparingTo(5), 4, true);
    assertPredicateResult(NEQ.comparingTo(5), 4, true);
    assertPredicateResult(EQ.comparingTo(5), 4, false);
    assertPredicateResult(GT.comparingTo(5), 4, false);
    assertPredicateResult(GEQ.comparingTo(5), 4, false);
  }

  /**
   * Correspondence between {@link ComparisonOperator}s and native Java operators.
   * @see #testToString()
   * @see #testLookup()
   */
  private static final ImmutableBiMap<ComparisonOperator, String> STRINGS =
      ImmutableBiMap.<ComparisonOperator, String>builder()
          .put(LT, "<")
          .put(LEQ, "<=")
          .put(NEQ, "!=")
          .put(EQ, "==")
          .put(GT, ">")
          .put(GEQ, ">=")
          .build();

  public void testLookup() throws Exception {
    // 1) test some illegal args
    assertThrows(NullPointerException.class, (Runnable)() -> lookup(null));
    assertThrows(IllegalArgumentException.class, (Runnable)() -> lookup(""));
    // 2) test some args manually
    assertNull(lookup("foo"));
    assertNull(lookup("+"));  // not a comparison op
    assertNull(lookup(">=="));  // close, but no
    assertSame(GEQ, lookup(">="));  // this works
    // 3) finally, test all the valid args in bulk
    for (Map.Entry<ComparisonOperator, String> entry : STRINGS.entrySet()) {
      assertEquals(entry.getKey(), lookup(entry.getValue()));
    }
  }

  public void testToString() throws Exception {
    for (ComparisonOperator op : ComparisonOperator.values()) {
      assertEquals(STRINGS.get(op), op.toString());
    }
  }

}