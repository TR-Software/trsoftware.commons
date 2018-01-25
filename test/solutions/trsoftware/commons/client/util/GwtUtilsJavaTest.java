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

import java.util.Map;

import static solutions.trsoftware.commons.client.util.GwtUtilsGwtTest.InnerStatic;


/**
 * Mar 21, 2011
 *
 * @author Alex
 */
public class GwtUtilsJavaTest extends TestCase {

  private GwtUtilsGwtTest delegate = new GwtUtilsGwtTest();

  public void testIsAssignableFrom() throws Exception {
    delegate.testIsAssignableFrom();
  }

  public void testGetSimpleName() throws Exception {
    delegate.testGetSimpleName();
    System.out.println("\n\n\n");
    // upper-level class:
    assertSimpleNameMatchesJRE(Integer.class);
    // inner classes:
    assertSimpleNameMatchesJRE(Map.Entry.class);
    assertSimpleNameMatchesJRE(GwtUtilsGwtTest.Inner.class);
    assertSimpleNameMatchesJRE(InnerStatic.class);
    // nested inner classes
    assertSimpleNameMatchesJRE(InnerStatic.InnerStaticA.class);
    assertSimpleNameMatchesJRE(InnerStatic.InnerStaticB.class);
    assertSimpleNameMatchesJRE(InnerStatic.InnerStaticA.InnerStaticAa.class);
    assertSimpleNameMatchesJRE(InnerStatic.InnerStaticA.InnerStaticAb.class);
  }

  /**
   * Asserts that the result of {@link GwtUtils#getSimpleName(Class)}
   * matches {@link Class#getSimpleName()} for the same class.
   */
  private static void assertSimpleNameMatchesJRE(Class cls) {
    String name = cls.getName();
    String jreSimpleName = cls.getSimpleName();
    String gwtUtilsSimpleName = GwtUtils.getSimpleName(cls);
    System.out.println(name);
    System.out.printf("  jreSimpleName: '%s'; gwtUtilsSimpleName: '%s'%n", jreSimpleName, gwtUtilsSimpleName);
    assertEquals(jreSimpleName, gwtUtilsSimpleName);
  }

}