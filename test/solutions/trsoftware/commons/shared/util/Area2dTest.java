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
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

/**
 * @author Alex, 9/20/2017
 */
public class Area2dTest extends TestCase {


  public void testGetArea() throws Exception {
    assertEquals(100, new Area2d(10, 10).getArea());
    assertEquals(50, new Area2d(5, 10).getArea());
    assertEquals(12, new Area2d(3, 4).getArea());
    assertEquals(0, new Area2d(3, 0).getArea());
    assertEquals(0, new Area2d(0, 3).getArea());
    assertEquals(0, new Area2d(0, 0).getArea());
  }

  public void testToString() throws Exception {
    assertEquals("10x10", new Area2d(10, 10).toString());
    assertEquals("5x10", new Area2d(5, 10).toString());
    assertEquals("3x4", new Area2d(3, 4).toString());
    assertEquals("3x0", new Area2d(3, 0).toString());
    assertEquals("0x3", new Area2d(0, 3).toString());
    assertEquals("0x0", new Area2d(0, 0).toString());
  }

  public void testEquals() throws Exception {
    AssertUtils.assertEqualsAndHashCode(new Area2d(10, 10), new Area2d(10, 10));
    AssertUtils.assertEqualsAndHashCode(new Area2d(5, 10), new Area2d(5, 10));
    AssertUtils.assertEqualsAndHashCode(new Area2d(3, 4), new Area2d(3, 4));
    AssertUtils.assertEqualsAndHashCode(new Area2d(3, 0), new Area2d(3, 0));
    AssertUtils.assertEqualsAndHashCode(new Area2d(0, 3), new Area2d(0, 3));
    AssertUtils.assertEqualsAndHashCode(new Area2d(0, 0), new Area2d(0, 0));
    
    AssertUtils.assertNotEqualsAndHashCode(new Area2d(10, 1), new Area2d(10, 10));
    AssertUtils.assertNotEqualsAndHashCode(new Area2d(5, 1), new Area2d(5, 10));
    AssertUtils.assertNotEqualsAndHashCode(new Area2d(3, 1), new Area2d(3, 4));
    AssertUtils.assertNotEqualsAndHashCode(new Area2d(3, 1), new Area2d(3, 0));
    AssertUtils.assertNotEqualsAndHashCode(new Area2d(0, 1), new Area2d(0, 3));
    AssertUtils.assertNotEqualsAndHashCode(new Area2d(1, 0), new Area2d(0, 0));
  }

  public void testCompareTo() throws Exception {
    AssertUtils.assertComparablesEqual(new Area2d(10, 10), new Area2d(10, 10));
    AssertUtils.assertComparablesEqual(new Area2d(5, 10), new Area2d(5, 10));
    AssertUtils.assertComparablesEqual(new Area2d(3, 4), new Area2d(3, 4));
    AssertUtils.assertComparablesEqual(new Area2d(3, 0), new Area2d(3, 0));
    AssertUtils.assertComparablesEqual(new Area2d(4, 0), new Area2d(3, 0));
    AssertUtils.assertComparablesEqual(new Area2d(0, 3), new Area2d(0, 3));
    AssertUtils.assertComparablesEqual(new Area2d(0, 2), new Area2d(0, 3));
    AssertUtils.assertComparablesEqual(new Area2d(0, 0), new Area2d(0, 0));

    AssertUtils.assertLessThan(new Area2d(10, 9), new Area2d(10, 10));
    AssertUtils.assertLessThan(new Area2d(4, 10), new Area2d(5, 10));
    AssertUtils.assertLessThan(new Area2d(3, 3), new Area2d(3, 4));
  }

  public void testConstructor() throws Exception {
    int width = 5;
    int height = 10;
    Area2d x = new Area2d(width, height);
    assertEquals(width, x.getWidth());
    assertEquals(height, x.getHeight());

    assertConstructorFails(-1, 0);
    assertConstructorFails(-1, 5);
    assertConstructorFails(-1, -2);
    assertConstructorFails(-1, -5);
    assertConstructorFails(0, -1);
    assertConstructorFails(5, -1);
    assertConstructorFails(-2, -1);
    assertConstructorFails(-5, -1);
  }

  private static void assertConstructorFails(final int width, final int height) {
    AssertUtils.assertThrows(AssertionError.class, new Runnable() {
      @Override
      public void run() {
        new Area2d(width, height);
      }
    });
  }

  public void testParse() throws Exception {
    // test some valid size strings
    assertEquals(Area2d.parse("10x10"), new Area2d(10, 10));
    assertEquals(Area2d.parse("5x10"), new Area2d(5, 10));
    assertEquals(Area2d.parse("3x4"), new Area2d(3, 4));
    assertEquals(Area2d.parse("3x0"), new Area2d(3, 0));
    assertEquals(Area2d.parse("0x3"), new Area2d(0, 3));

    // test some strings specifying only a single dimension
    assertEquals(Area2d.parse("10"), new Area2d(10, 10));
    assertEquals(Area2d.parse("5"), new Area2d(5, 5));
    assertEquals(Area2d.parse("0"), new Area2d(0, 0));
    assertEquals(Area2d.parse(""), new Area2d(0, 0));

    // now test some strings containing an 'x' symbol, but missing one dimension
    assertEquals(Area2d.parse("x10"), new Area2d(0, 10));
    assertEquals(Area2d.parse("10x"), new Area2d(10, 0));
    assertEquals(Area2d.parse("x5"), new Area2d(0, 5));
    assertEquals(Area2d.parse("5x"), new Area2d(5, 0));

    // finally, test some unparseable strings
    String[] badStrings = new String[] {"a", "axb", "xa", "ax", "foo", "fooxbar"};
    for (String badString : badStrings) {
      AssertUtils.assertThrows(NumberFormatException.class, new Runnable() {
        @Override
        public void run() {
          Area2d.parse(badString);
        }
      });
    }
  }

  public void testGetRenderer() throws Exception {
    Area2d.Renderer renderer = Area2d.getRenderer();
    assertNotNull(renderer);
    assertSame(renderer, Area2d.getRenderer());
    Area2d x = new Area2d(5, 10);
    assertEquals("5x10", renderer.render(x));
    // to support all possible usages of the renderer in GWT widgets (such as ValueListBox), it must accept null values
    assertEquals("", renderer.render(null));
  }
  
  public void testGetParser() throws Exception {
    Area2d.Parser parser = Area2d.getParser();
    assertNotNull(parser);
    assertSame(parser, Area2d.getParser());
    assertEquals(new Area2d(5, 10), parser.parse("5x10"));
  }

}