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

package solutions.trsoftware.commons.server.io;

import junit.framework.TestCase;

/**
 * Mar 29, 2011
 *
 * @author Alex
 */
public class StringPrintStreamTest extends TestCase {


  public void testPrintingToString() throws Exception {
    StringPrintStream[] instances = new StringPrintStream[]{
        // test all the different constructor permutations
        new StringPrintStream(),
        new StringPrintStream(2),
        new StringPrintStream(2, true),
        new StringPrintStream(2, true, "UTF-8"),
    };
    for (StringPrintStream s : instances) {
      assertEquals("", s.toString());
      s.printf("foo %d\nbar", 1);
      assertEquals("foo 1\nbar", s.toString());
    }
  }
}