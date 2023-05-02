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

package solutions.trsoftware.commons.server.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.function.FloatConsumer;
import solutions.trsoftware.commons.shared.util.iterators.FloatIterator;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import static solutions.trsoftware.commons.server.util.PrimitiveFloatArrayList.FloatListIterator;
import static solutions.trsoftware.commons.server.util.PrimitiveFloatArrayList.of;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.shared.util.RandomUtils.rnd;

/**
 * Oct 22, 2012
 *
 * @author Alex
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class PrimitiveFloatArrayListTest extends TestCase {

  private PrimitiveFloatArrayList primitiveList;
  // we'll be testing the class versus a standard ArrayList<Float>
  private ArrayList<Float> arrayList;

  public void setUp() throws Exception {
    super.setUp();
    primitiveList = new PrimitiveFloatArrayList();
    arrayList = new ArrayList<>();
  }

  @Override
  protected void tearDown() throws Exception {
    primitiveList = null;
    arrayList = null;
    super.tearDown();
  }

  @SuppressWarnings("ConstantConditions")
  public void testAdd() throws Exception {
    // 1) test add(Float) and the add(float) primitive specialization
    for (int i = 0; i < 1000; i++) {
      @SuppressWarnings("UnnecessaryLocalVariable")
      float value = i;
      arrayList.add(value);
      // test both add(Float) and the add(float) primitive specialization
      if (value % 2 == 0)
        primitiveList.add(value);
      else
        primitiveList.add(Float.valueOf(value));
      assertEquals(arrayList, primitiveList);
      // might as well test get/getFloat here too
      assertEquals(value, primitiveList.get(i));
      assertEquals(value, primitiveList.getFloat(i), 0);
    }
    // 2) test insertion: add(int, Float) and the add(int, float) primitive specialization
    for (int i = 0; i < 1000; i++) {
      float value = rnd.nextFloat();
      int index = rnd.nextInt(arrayList.size());
      arrayList.add(index, value);
      // test both add(int, Float) and the add(int, float) primitive specialization
      if (value % 2 == 0)
        primitiveList.add(index, value);
      else
        primitiveList.add(index, Float.valueOf(value));
      assertEquals(arrayList, primitiveList);
      // might as well test get/getFloat here too
      assertEquals(value, primitiveList.get(index));
      assertEquals(value, primitiveList.getFloat(index), 0);
    }

    // test some illegal arguments
    assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> primitiveList.add(-1, 123f));
    assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> primitiveList.add(primitiveList.size() + 1, 123f));

    assertThrows(NullPointerException.class, () -> primitiveList.add(null));
    assertThrows(NullPointerException.class, (Runnable)() -> primitiveList.add(0, null));
  }

  public void testGet() throws Exception {
    // already tested in testAdd, so just a minimal test here
    PrimitiveFloatArrayList list = of(1f);
    assertEquals(1f, list.get(0));
    assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> list.get(-1));
    assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> list.get(1));
  }

  public void testGetFloat() throws Exception {
    // already tested in testAdd, so just a minimal test here
    PrimitiveFloatArrayList list = of(1f);
    assertEquals(1f, list.getFloat(0), 0);
    assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> list.getFloat(-1));
    assertThrows(IndexOutOfBoundsException.class, (Runnable)() -> list.getFloat(1));
  }

  public void testSize() throws Exception {
    assertEquals(0, new PrimitiveFloatArrayList().size());
    assertEquals(0, new PrimitiveFloatArrayList(10).size());
    assertEquals(1, of(1).size());
    assertEquals(3, of(1,2,3).size());
  }

  public void testToFloatArray() throws Exception {
    assertArraysEqual(new float[0], new PrimitiveFloatArrayList().toFloatArray());
    assertArraysEqual(new float[0], new PrimitiveFloatArrayList(10).toFloatArray());
    assertArraysEqual(new float[]{1f}, of(1).toFloatArray());
    assertArraysEqual(new float[]{1,2,3}, of(1,2,3).toFloatArray());
    // verify that a new array instance is returned each time (to make sure the internal array is not exposed)
    PrimitiveFloatArrayList list = new PrimitiveFloatArrayList(1, 2, 3);
    assertNotSame(list.toFloatArray(), list.toFloatArray());

    // compare with generic toArray()
    assertArraysEqual(new Float[]{1f, 2f, 3f}, of(1,2,3).toArray());
  }

  public void testSet() throws Exception {
    PrimitiveFloatArrayList list = new PrimitiveFloatArrayList(1, 2, 3);
    assertEquals(2f, list.set(1, 5f));
    assertArraysEqual(new float[]{1, 5, 3}, list.toFloatArray());
  }

  public void testSetFloat() throws Exception {
    PrimitiveFloatArrayList list = new PrimitiveFloatArrayList(1, 2, 3);
    assertEquals(2f, list.setFloat(1, 5f), 0);
    assertArraysEqual(new float[]{1, 5, 3}, list.toFloatArray());
  }

  public void testRemove() throws Exception {
    PrimitiveFloatArrayList list = new PrimitiveFloatArrayList(1, 2, 3);
    assertEquals(3, list.size());
    assertEquals(2f, list.remove(1));
    assertEquals(2, list.size());
    assertArraysEqual(new float[]{1, 3}, list.toFloatArray());
  }

  public void testRemoveFloat() throws Exception {
    PrimitiveFloatArrayList list = new PrimitiveFloatArrayList(1, 2, 3);
    assertEquals(3, list.size());
    assertEquals(2f, list.removeFloat(1), 0);
    assertEquals(2, list.size());
    assertArraysEqual(new float[]{1, 3}, list.toFloatArray());
  }

  public void testListIterator() throws Exception {
    PrimitiveFloatArrayList list = new PrimitiveFloatArrayList(1, 2, 3);
    FloatListIterator it = list.listIterator();

    // 1) traversal
    assertFalse(it.hasPrevious());
    assertTrue(it.hasNext());
    assertEquals(-1, it.previousIndex());
    assertEquals(0, it.nextIndex());
    assertThrows(NoSuchElementException.class, it::previous);
    assertThrows(NoSuchElementException.class, it::previousFloat);
    assertEquals(1f, it.next());
    assertEquals(2f, it.nextFloat(), 0);
    assertEquals(1, it.previousIndex());
    assertEquals(2, it.nextIndex());

    // 2) remove
    it.remove();
    assertEquals(2, list.size());
    assertArraysEqual(new float[]{1, 3}, list.toFloatArray());
    assertEquals(0, it.previousIndex());
    assertEquals(1, it.nextIndex());

    // 3) set
    assertThrows(IllegalStateException.class, (Runnable)() -> it.set(4));  // must make another call to previous or next
    assertEquals(3f, it.next());
    assertEquals(1, it.previousIndex());
    assertEquals(2, it.nextIndex());
    assertFalse(it.hasNext());
    it.set(Float.valueOf(4));  // ListIterator.set(Object)
    assertArraysEqual(new float[]{1, 4}, list.toFloatArray());
    assertEquals(1, it.previousIndex());
    assertEquals(2, it.nextIndex());
    assertEquals(4f, it.previousFloat(), 0);
    assertEquals(1f, it.previous());
    it.set(5);  // FloatListIterator.set(float)
    assertArraysEqual(new float[]{5, 4}, list.toFloatArray());

    // 4) add
    assertEquals(-1, it.previousIndex());
    assertEquals(0, it.nextIndex());
    it.add(Float.valueOf(6));  // ListIterator.add(Object)
    assertArraysEqual(new float[]{6, 5, 4}, list.toFloatArray());
    assertEquals(0, it.previousIndex());
    assertEquals(1, it.nextIndex());
    assertEquals(5f, it.next());
    assertEquals(1, it.previousIndex());
    assertEquals(2, it.nextIndex());
    it.add(7);  // FloatListIterator.add(float)
    assertArraysEqual(new float[]{6, 5, 7, 4}, list.toFloatArray());
    assertArraysEqual(new Float[]{6f, 5f, 7f, 4f}, list.toArray());
    assertEquals(2, it.previousIndex());
    assertEquals(3, it.nextIndex());

    // test fail-fast behavior of the iterator
    assertEquals(6f, list.removeFloat(0));
    assertThrows(ConcurrentModificationException.class, it::next);
    assertThrows(ConcurrentModificationException.class, it::previous);
  }

  public void testIterator() throws Exception {
    float[] elements = {1, 2, 3};
    PrimitiveFloatArrayList list = new PrimitiveFloatArrayList(elements);
    // basic iteration using next() and nextFloat()
    {
      FloatIterator it = list.iterator();
      assertEquals(1f, it.next());
      assertEquals(2f, it.nextFloat(), 0);
      assertEquals(3f, it.nextFloat(), 0);
      assertFalse(it.hasNext());
      assertThrows(NoSuchElementException.class, it::next);
      assertThrows(NoSuchElementException.class, it::nextFloat);
    }

    // forEachRemaining(FloatConsumer)
    {
      FloatIterator it = list.iterator();
      assertEquals(1f, it.next());  // advance to 2nd element
      PrimitiveFloatArrayList remaining = new PrimitiveFloatArrayList();
      it.forEachRemaining((FloatConsumer)remaining::add);
      assertEquals(of(2, 3), remaining);
    }

    // forEachRemaining(Consumer<Float>)
    {
      FloatIterator it = list.iterator();
      assertEquals(1f, it.next());  // advance to 2nd element
      ArrayList<Float> remaining = new ArrayList<>();
      it.forEachRemaining((Consumer<Float>)remaining::add);
      assertEquals(of(2, 3), remaining);
    }

    // TODO: test fail-fast (ConcurrentModificationException) behavior of forEachRemaining
  }

  public void testSort() throws Exception {
    PrimitiveFloatArrayList list = of(3, 2, 4, 2, 1);
    list.sort();
    assertEquals(of(1, 2, 2, 3, 4), list);
  }

  public void testTrimToSize() throws Exception {
    PrimitiveFloatArrayList list = new PrimitiveFloatArrayList(10);
    for (int i = 1; i <= 3; i++) {
      list.add(i);
    }
    assertEquals(3, list.size());
    assertEquals(of(1, 2, 3), list);
    // at this point, the list size is 3, but the internal capacity is still 10
    list.trimToSize();
    // we won't bother checking if the internal float[] was downsized; just check that the list itself wasn't modified
    assertEquals(3, list.size());
    assertEquals(of(1, 2, 3), list);
  }

}