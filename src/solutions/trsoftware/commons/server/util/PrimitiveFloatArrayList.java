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

import solutions.trsoftware.commons.shared.util.ArrayUtils;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

/**
 * A Java Collections adapter for a primitive {@code float[]} array, which uses 5x less space than {@code ArrayList<Float>}.
 * <p>
 * Some basic testing showed that a primitive {@code float[]} with a million elements used up 3,880.688 KB of memory, while the
 * equivalent wrapper {@code Float[]} of the same size used up 19,531.086 KB of memory.
 * </p>
 *
 * @author Alex
 * @since Oct 22, 2012
 * @see <a href="https://commons.apache.org/dormant/commons-primitives/">Apache Commons Primitives</a>
 */
public class PrimitiveFloatArrayList extends AbstractList<Float>
    implements List<Float>, RandomAccess, Cloneable, java.io.Serializable {

  private static final long serialVersionUID = 1L;

  private float[] elementData;
  private int size;
  

  public PrimitiveFloatArrayList() {
    this(8);
  }

  public PrimitiveFloatArrayList(float[] initialArr) {
    size = initialArr.length;
    elementData = new float[size];
    // defensively clone the given array
    System.arraycopy(initialArr, 0, elementData, 0, size);

  }

  public PrimitiveFloatArrayList(int initialSize) {
    elementData = new float[initialSize];
  }

  public Float get(int index) {
    return elementData[index];
  }

  public int size() {
    return size;
  }

  @Override
  public boolean add(Float e) {
    modCount++;  // support fail-fast iterators (ConcurrentModificationExceptions)
    elementData = ArrayUtils.flexibleArrayAdd(elementData, size++, e.floatValue());
    return true;
  }

  /**
   * Replaces the element at the specified position in this list with
   * the specified element.
   *
   * @param index index of the element to replace
   * @param element element to be stored at the specified position
   * @return the element previously at the specified position
   */
  public Float set(int index, Float element) {
    Float oldValue = elementData[index];
    elementData[index] = element;
    return oldValue;
  }

}
