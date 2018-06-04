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

package solutions.trsoftware.commons.shared.util;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.shared.util.ComparisonOperator.*;

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
}