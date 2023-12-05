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

package solutions.trsoftware.commons.shared.text.markovchain.state;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.text.markovchain.dict.CodingDictionary;
import solutions.trsoftware.commons.shared.text.markovchain.dict.ShortHashArrayCodingDictionary;

/**
 * Oct 20, 2009
 *
 * @author Alex
 */
public abstract class ShortStateTestCase extends TestCase {
  protected CodingDictionary<Short> dict;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dict = new ShortHashArrayCodingDictionary();
    dict.encode("a");
    dict.encode("foo");
    dict.encode("bar");
    dict.encode("foo");
    dict.encode("b");
    dict.encode("bar");
    dict.encode("baz");
    dict.encode("c");
  }

}