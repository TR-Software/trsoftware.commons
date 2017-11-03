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

package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;

/**
 * Oct 17, 2012
 *
 * @author Alex
 */
public class HashCodeBuilderTest extends TestCase {

  public void testHashCode() throws Exception {
    HashCodeBuilder b1 = new HashCodeBuilder();
    HashCodeBuilder b2 = new HashCodeBuilder();
    assertTrue(b1.hashCode() == b2.hashCode());
    Object o1 = new Object();
    Object o2 = new Object();
    Object o3 = new Object();
    b1.update(o1);
    assertTrue(b1.hashCode() != b2.hashCode());
    b2.update(o1);
    assertTrue(b1.hashCode() == b2.hashCode());
    b1.update(o2,o3);
    assertTrue(b1.hashCode() != b2.hashCode());
    b2.update(o2);
    assertTrue(b1.hashCode() != b2.hashCode());
    b2.update(o3);
    assertTrue(b1.hashCode() == b2.hashCode());

    // check that objects that have the same equals methods have the same hash code
    assertTrue(
        new HashCodeBuilder().update("asdf").hashCode() ==
        new HashCodeBuilder().update("asdf").hashCode());
    assertTrue(
        new HashCodeBuilder().update("asdf").update("foo").hashCode() ==
        new HashCodeBuilder().update("asdf").update("foo").hashCode());
    assertTrue(
        new HashCodeBuilder().update(1.1).update(2.2).hashCode() ==
        new HashCodeBuilder().update(1.1).update(2.2).hashCode());
  }
}