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

package solutions.trsoftware.commons.server.io.csv;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.testutil.PerformanceComparison;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

import java.util.Random;

/**
 * Mar 15, 2010
 *
 * @author Alex
 */
public class CSVObjectBinderBaseTest extends TestCase {
  static class MyClass {
    MyClass() {
    }

    MyClass(Integer foo, float bar) {
      this.foo = foo;
      this.bar = bar;
    }

    Integer foo;
    float bar;

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MyClass myClass = (MyClass)o;

      if (Float.compare(myClass.bar, bar) != 0) return false;
      if (foo != null ? !foo.equals(myClass.foo) : myClass.foo != null)
        return false;

      return true;
    }

    public int hashCode() {
      int result;
      result = (foo != null ? foo.hashCode() : 0);
      result = 31 * result + (bar != +0.0f ? Float.floatToIntBits(bar) : 0);
      return result;
    }
  }

  protected CSVObjectBinderBase<MyClass> myClassBinder;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myClassBinder = new CSVObjectBinderBase<MyClass>(MyClass.class, new String[]{"foo", "bar"}) {
      public Object fieldFromString(String name, String value) {
        if (name.equals("foo"))
          return new Integer(value);
        else if (name.equals("bar"))
          return Float.parseFloat(value);
        throw new IllegalArgumentException("No binding for field name " + name);
      }

      public String fieldToString(String name, Object value) {
        if (name.equals("foo"))
          return Integer.toString((Integer)value);
        else if (name.equals("bar"))
          return Float.toString((Float)value);
        throw new IllegalArgumentException("No binding for field name " + name);
      }
    };
  }

  /** Used to compare parsing speed */
  private MyClass parseMyClassWithoutReflection(String[] csvLine) {
    return new MyClass(new Integer(csvLine[0]), Float.parseFloat(csvLine[1]));
  }

  /** Used to compare parsing speed */
  private String[] writeMyClassWithoutReflection(MyClass instance) {
    return new String[]{String.valueOf(instance.foo), String.valueOf(instance.bar)};
  }

  public void testParse() throws Exception {
    MyClass result = myClassBinder.parseCsvLine(new String[]{"1", "2.5"});
    assertEquals((Integer)1, result.foo);
    assertEquals(2.5f, result.bar);
  }

  public void testWrite() throws Exception {
    MyClass input = new MyClass(1, 2.5f);
    AssertUtils.assertArraysEqual(new String[]{"1", "2.5"}, myClassBinder.writeCsvLine(input));
  }

  public void testPerformance() throws Exception {
    final Random rnd = new Random();
    PerformanceComparison.compare(
        () -> {
          MyClass input = new MyClass(rnd.nextInt(), rnd.nextFloat());
          String[] csv = writeMyClassWithoutReflection(input);
          MyClass output = parseMyClassWithoutReflection(csv);
          assertEquals(input, output);
        },
        "without reflection",
        () -> {
          MyClass input = new MyClass(rnd.nextInt(), rnd.nextFloat());
          try {
            String[] csv = myClassBinder.writeCsvLine(input);
            MyClass output = myClassBinder.parseCsvLine(csv);
            assertEquals(input, output);
          }
          catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        },
        "binder",
        100000);
  }
  
}