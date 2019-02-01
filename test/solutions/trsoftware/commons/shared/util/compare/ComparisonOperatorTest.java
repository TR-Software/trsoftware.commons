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

import junit.framework.TestCase;

import java.util.function.BiPredicate;
import java.util.function.IntPredicate;

import static solutions.trsoftware.commons.shared.util.compare.ComparisonOperator.*;

/**
 * @author Alex
 * @since 4/24/2018
 */
public class ComparisonOperatorTest extends TestCase {

  public void testLookup() throws Exception {
    assertEquals(EQ, lookup(1, 1));
    assertEquals(LT, lookup(1, 2));
    assertEquals(GT, lookup(2, 1));
  }

  public void testPredicate() throws Exception {
    int cmp = "foo".compareTo("bar");
    assertIntPredicateResult(LT, cmp, false);
    assertIntPredicateResult(LEQ, cmp, false);
    assertIntPredicateResult(EQ, cmp, false);
    assertIntPredicateResult(NEQ, cmp, true);
    assertIntPredicateResult(GT, cmp, true);
    assertIntPredicateResult(GEQ, cmp, true);
  }

  public void testBiPredicate() throws Exception {
    int lhs = 4, rhs = 5;
    assertBiPredicateResult(LT::compare, lhs, rhs, true);
    assertBiPredicateResult(LEQ::compare, lhs, rhs, true);
    assertBiPredicateResult(NEQ::compare, lhs, rhs, true);
    assertBiPredicateResult(EQ::compare, lhs, rhs, false);
    assertBiPredicateResult(GT::compare, lhs, rhs, false);
    assertBiPredicateResult(GEQ::compare, lhs, rhs, false);
  }

  public static void assertIntPredicateResult(IntPredicate predicate, int arg, boolean expected) {
    assertEquals(expected, predicate.test(arg));
    // also test negation of the predicate
    assertEquals(!expected, predicate.negate().test(arg));
  }

  public static <T> void assertBiPredicateResult(BiPredicate<T, T> predicate, T lhs, T rhs, boolean expected) {
    assertEquals(expected, predicate.test(lhs, rhs));
    // also test negation of the predicate
    assertEquals(!expected, predicate.negate().test(lhs, rhs));
  }
}