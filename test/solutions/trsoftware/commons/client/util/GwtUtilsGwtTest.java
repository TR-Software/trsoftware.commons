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

import solutions.trsoftware.commons.client.CommonsGwtTestCase;

import java.util.Map;

import static solutions.trsoftware.commons.client.util.GwtUtils.getSimpleName;
import static solutions.trsoftware.commons.client.util.GwtUtils.isAssignableFrom;


/**
 * Mar 21, 2011
 *
 * @author Alex
 */
public class GwtUtilsGwtTest extends CommonsGwtTestCase {

  public void testIsAssignableFrom() throws Exception {
    Class<Number> number = Number.class;
    Class<Integer> integer = Integer.class;

    assertTrue(isAssignableFrom(number, integer));
    assertFalse(isAssignableFrom(integer, number));
  }

  public void testGetSimpleName() throws Exception {
    System.out.println("---------- Outer Classes ----------");
    assertEquals("java.lang.Integer", Integer.class.getName());
    assertEquals("Integer", getSimpleNameAndPrint(Integer.class));
    System.out.println("---------- Inner Classes ----------");
    assertEquals("java.util.Map$Entry", Map.Entry.class.getName());
    assertEquals("Entry", getSimpleNameAndPrint(Map.Entry.class));
    assertEquals("InnerStatic", getSimpleNameAndPrint(InnerStatic.class));
    assertEquals("Inner", getSimpleNameAndPrint(Inner.class));
    System.out.println("---------- Nested Inner Classes ----------");
    // now test some nested inner classes
    assertEquals("InnerStaticA", getSimpleNameAndPrint(InnerStatic.InnerStaticA.class));
    assertEquals("InnerStaticB", getSimpleNameAndPrint(InnerStatic.InnerStaticB.class));
    assertEquals("InnerStaticAa", getSimpleNameAndPrint(InnerStatic.InnerStaticA.InnerStaticAa.class));
    assertEquals("InnerStaticAb", getSimpleNameAndPrint(InnerStatic.InnerStaticA.InnerStaticAb.class));
    // now test some anonymous classes
    /*
      Unlike the JRE version of Class.getSimpleName, which returns an empty string for anon classes,
      we expect our version to return something more tangible
     */
    System.out.println("---------- Anonymous Inner Classes ----------");
    assertEquals("GwtUtilsGwtTest$1::InnerStatic", getSimpleNameAndPrint(anon_InnerStatic_1.getClass()));
    assertEquals("GwtUtilsGwtTest$2::InnerStatic", getSimpleNameAndPrint(anon_InnerStatic_2.getClass()));
    assertEquals("GwtUtilsGwtTest$3::Inner", getSimpleNameAndPrint(anon_Inner_1.getClass()));
    assertEquals("GwtUtilsGwtTest$4::Inner", getSimpleNameAndPrint(anon_Inner_2.getClass()));
    assertEquals("GwtUtilsGwtTest$5::InnerStaticA", getSimpleNameAndPrint(anon_InnerStaticA_1.getClass()));
    assertEquals("GwtUtilsGwtTest$6::InnerStaticA", getSimpleNameAndPrint(anon_InnerStaticA_2.getClass()));
    assertEquals("GwtUtilsGwtTest$7::InnerStaticB", getSimpleNameAndPrint(anon_InnerStaticB_1.getClass()));
    assertEquals("GwtUtilsGwtTest$8::InnerStaticB", getSimpleNameAndPrint(anon_InnerStaticB_2.getClass()));
    assertEquals("GwtUtilsGwtTest$9::InnerStaticAa", getSimpleNameAndPrint(anon_InnerStaticAa.getClass()));
    assertEquals("GwtUtilsGwtTest$10::InnerStaticAb", getSimpleNameAndPrint(anon_InnerStaticAb.getClass()));
    System.out.println("---------- Anonymous class implementing only an interface ----------");
    assertEquals("GwtUtilsGwtTest$11", getSimpleNameAndPrint(anon_Runnable.getClass()));
    System.out.println("---------- Anonymous Classes Nested Within Inner Classes ----------");
    assertEquals("GwtUtilsGwtTest$InnerStatic$1::InnerStatic", getSimpleNameAndPrint(InnerStatic.nested_anon_InnerStatic.getClass()));
    assertEquals("GwtUtilsGwtTest$InnerStatic$2::InnerStaticA", getSimpleNameAndPrint(InnerStatic.nested_anon_InnerStaticA.getClass()));
    assertEquals("GwtUtilsGwtTest$InnerStatic$3::InnerStaticAa", getSimpleNameAndPrint(InnerStatic.nested_anon_InnerStaticAa.getClass()));
  }

  static String getSimpleNameAndPrint(Class cls) {
    String result = getSimpleName(cls);
    System.out.println(cls.getName());
    System.out.println("  --> \"" + result + "\"");
    return result;
  }

  // define some inner classes
  static class InnerStatic {
    static class InnerStaticA {
      static class InnerStaticAa {}
      static class InnerStaticAb {}
    }
    static class InnerStaticB {}
    // define some nested anon classes
    static InnerStatic nested_anon_InnerStatic = new InnerStatic(){};
    static InnerStaticA nested_anon_InnerStaticA = new InnerStaticA(){};
    static InnerStaticA.InnerStaticAa nested_anon_InnerStaticAa = new InnerStaticA.InnerStaticAa(){};
  }
  class Inner {}
  
  // define some anonymous subclasses of the above inner classes
  InnerStatic anon_InnerStatic_1 = new InnerStatic(){};
  InnerStatic anon_InnerStatic_2 = new InnerStatic(){};
  Inner anon_Inner_1 = new Inner(){};
  Inner anon_Inner_2 = new Inner(){};
  InnerStatic.InnerStaticA anon_InnerStaticA_1 = new InnerStatic.InnerStaticA(){};
  InnerStatic.InnerStaticA anon_InnerStaticA_2 = new InnerStatic.InnerStaticA(){};
  InnerStatic.InnerStaticB anon_InnerStaticB_1 = new InnerStatic.InnerStaticB(){};
  InnerStatic.InnerStaticB anon_InnerStaticB_2 = new InnerStatic.InnerStaticB(){};
  InnerStatic.InnerStaticA.InnerStaticAa anon_InnerStaticAa = new InnerStatic.InnerStaticA.InnerStaticAa(){};
  InnerStatic.InnerStaticA.InnerStaticAb anon_InnerStaticAb = new InnerStatic.InnerStaticA.InnerStaticAb(){};

  // define some anon classes that inherit from an interface rather than a base class
  Runnable anon_Runnable = new Runnable() {
    @Override
    public void run() { }
  };
}