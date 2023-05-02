/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.server.util;

import com.google.common.primitives.Floats;
import solutions.trsoftware.commons.shared.util.iterators.FloatIterator;

import java.util.*;

/**
 * A Java Collections adapter for a primitive {@code float[]} array, which uses significantly less memory
 * than an equivalent {@code ArrayList<Float>} and offers better performance for many of its operations.
 * <p>
 * This {@link List} implementation does not allow {@code null} elements.
 * <p>
 * <i>Note:</i> Unlike Guava's {@link Floats#asList(float...)} container, this class supports adding new elements to
 * the list using its {@link #add} methods and {@link #listIterator}.
 *
 * @author Alex
 * @see Floats#asList(float...)
 * @see <a href="https://commons.apache.org/dormant/commons-primitives/">Apache Commons Primitives (dormant)</a>
 * @see <a href="https://stackoverflow.com/questions/2504959/why-can-java-collections-not-directly-store-primitives-types">Other
 *     primitive collection libraries</a>
 * @since Oct 22, 2012
 */
public class PrimitiveFloatArrayList extends AbstractList<Float>
    implements List<Float>, RandomAccess, Cloneable, java.io.Serializable {

  // TODO(4/13/2023): rename class to FloatArrayList and move to solutions.trsoftware.commons.shared.util.collections

  private static final long serialVersionUID = 1L;

  private static final int DEFAULT_CAPACITY = 10;

  private float[] elementData;
  private int size;


  public PrimitiveFloatArrayList() {
    this(DEFAULT_CAPACITY);
  }

  public PrimitiveFloatArrayList(float... elements) {
    size = elements.length;
    elementData = Arrays.copyOf(elements, size);
  }

  public PrimitiveFloatArrayList(int initialCapacity) {
    elementData = new float[Math.max(DEFAULT_CAPACITY, initialCapacity)];
  }

  /**
   * Factory method.
   *
   * @return a new instance of {@link PrimitiveFloatArrayList} containing the given elements
   */
  public static PrimitiveFloatArrayList of(float... elements) {
    return new PrimitiveFloatArrayList(elements);
  }

  /**
   * Factory method.  Creates an instance containing the unboxed values from the given collection
   *
   * @return a new instance of {@link PrimitiveFloatArrayList} containing the primitive {@code float}
   * equivalents of the elements in the given collection
   * @throws NullPointerException if the specified collection contains any null elements,
   *   or if the specified collection is null
   */
  public static PrimitiveFloatArrayList copyOf(Collection<Float> elements) {
    PrimitiveFloatArrayList ret = new PrimitiveFloatArrayList(elements.size());
    ret.addAll(elements);
    return ret;
  }

  public Float get(int index) {
    return getFloat(index);
  }

  /**
   * Specialized version of {@link #get(int)} without auto-boxing.
   *
   * @param index index of the element to return
   * @return the element at the specified position in this list
   * @throws IndexOutOfBoundsException if the index is out of range
   *         ({@code index < 0 || index >= size()})
   */
  public float getFloat(int index) {
    rangeCheck(index);
    return elementData[index];
  }

  public int size() {
    return size;
  }

  /**
   * Appends the specified element to the end of this list.
   *
   * @param e element to be appended to this list
   * @return {@code true} (as specified by {@link Collection#add})
   * @throws NullPointerException if the specified element is {@code null}
   * @see #add(float)
   *
   */
  @Override
  public boolean add(Float e) {
    return add(e.floatValue());
  }

  /**
   * Appends the specified primitive value to the end of this list.
   * This is a specialized version of {@link #add(Float)} that avoids auto-boxing.
   *
   * @param e element to be appended to this list
   * @return {@code true} (as specified by {@link Collection#add})
   */
  public boolean add(float e) {
    ensureExplicitCapacity(size + 1);  // increments modCount (for fail-fast iterators)
    elementData[size++] = e;
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * @see #add(int, float)
   */
  @Override
  public void add(int index, Float element) {
    add(index, element.floatValue());
  }

  /**
   * Specialized version of {@link #add(int, Float)} without auto-boxing.
   *
   * @param index index at which the specified element is to be inserted
   * @param element element to be inserted
   */
  public void add(int index, float element) {
    rangeCheckForAdd(index);
    ensureExplicitCapacity(size + 1);
    System.arraycopy(elementData, index, elementData, index + 1,
        size - index);
    elementData[index] = element;
    size++;
  }

  /**
   * Specialized version of {@link #toArray()} without auto-boxing.
   *
   * @return a new array containing all of the elements in this list in proper sequence
   */
  public float[] toFloatArray() {
    return Arrays.copyOf(elementData, size);
  }

  /**
   * Replaces the element at the specified position in this list with
   * the specified element.
   *
   * @param index index of the element to replace
   * @param element element to be stored at the specified position
   * @return the element previously at the specified position
   * @see #setFloat(int, float)
   */
  public Float set(int index, Float element) {
    return setFloat(index, element);
  }

  /**
   * Specialized version of {@link #set(int, Float)} without auto-boxing.
   *
   * @param index index of the element to replace
   * @param element element to be stored at the specified position
   * @return the element previously at the specified position
   */
  public float setFloat(int index, float element) {
    float oldValue = elementData[index];
    elementData[index] = element;
    return oldValue;
  }

  /**
   * {@inheritDoc}
   *
   * @see #removeFloat(int)
   */
  @Override
  public Float remove(int index) {
    return removeFloat(index);
  }

  /**
   * Specialized version of {@link #remove(int)} without auto-boxing.
   *
   * @param index the index of the element to be removed
   * @return the element that was removed from the list
   * @throws IndexOutOfBoundsException if the index is out of range
   *                                   ({@code index < 0 || index >= size()})
   */
  public float removeFloat(int index) {
    rangeCheck(index);

    modCount++;
    float oldValue = elementData[index];

    int numMoved = size - index - 1;
    if (numMoved > 0)
      System.arraycopy(elementData, index + 1, elementData, index, numMoved);
    size--;
    return oldValue;
  }

  /**
   * Sorts this list into ascending numerical order using {@link Arrays#sort(float[])}.
   */
  public void sort() {
    Arrays.sort(elementData, 0, size);
  }

  /**
   * Returns a list iterator over the elements in this list (in proper
   * sequence), starting at the specified position in the list.
   * The specified index indicates the first element that would be
   * returned by an initial call to {@link ListIterator#next next}.
   * An initial call to {@link ListIterator#previous previous} would
   * return the element with the specified index minus one.
   *
   * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
   *
   * @throws IndexOutOfBoundsException {@inheritDoc}
   */
  public FloatListIterator listIterator(int index) {
    if (index < 0 || index > size)
      throw new IndexOutOfBoundsException("Index: " + index);
    return new ListItr(index);
  }

  /**
   * Returns a list iterator over the elements in this list (in proper
   * sequence).
   *
   * <p>The returned list iterator is <i>fail-fast</i>.
   *
   * @see #listIterator(int)
   */
  public FloatListIterator listIterator() {
    return new ListItr(0);
  }

  /**
   * Returns an iterator over the elements in this list in proper sequence.
   *
   * <p>The returned iterator is <i>fail-fast</i>.
   *
   * @return an iterator over the elements in this list in proper sequence
   */
  public FloatIterator iterator() {
    return new Itr();
  }

  /*
  TODO: might want to also override the following methods:
    - subList: to return a specialized subclass of PrimitiveFloatArrayList (similar to java.util.ArrayList.SubList)
    - forEach: to check for concurrent modification (see java.util.ArrayList.forEach)
    - addAll: to support primitive floats and directly access elementData if collection instanceof PrimitiveFloatArrayList
   */


  /**
   * Trims the capacity of this instance to be the list's current size.  An application can use this operation
   * to minimize the storage of a {@link PrimitiveFloatArrayList} instance.
   */
  public void trimToSize() {
    modCount++;  // NOTE: not sure why ArrayList increments modCount here, but we'll just do the same, to be safe
    if (size < elementData.length) {
      elementData = Arrays.copyOf(elementData, size);
    }
  }

  // NOTE: the following capacity-ensuring and range-checking code borrowed (verbatim) from ArrayList

  /**
   * Checks if the given index is in range.  If not, throws an appropriate
   * runtime exception.  This method does *not* check if the index is
   * negative: It is always used immediately prior to an array access,
   * which throws an ArrayIndexOutOfBoundsException if index is negative.
   */
  private void rangeCheck(int index) {
    if (index >= size)
      throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
  }

  /**
   * A version of rangeCheck used by add and addAll.
   */
  private void rangeCheckForAdd(int index) {
    if (index > size || index < 0)
      throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
  }

  /**
   * Constructs an IndexOutOfBoundsException detail message.
   * Of the many possible refactorings of the error handling code,
   * this "outlining" performs best with both server and client VMs.
   */
  private String outOfBoundsMsg(int index) {
    return "Index: " + index + ", Size: " + size;
  }

  private void ensureExplicitCapacity(int minCapacity) {
    modCount++;
    // overflow-conscious code
    if (minCapacity - elementData.length > 0)
      grow(minCapacity);
  }

  /**
   * The maximum size of array to allocate.
   * Some VMs reserve some header words in an array.
   * Attempts to allocate larger arrays may result in
   * OutOfMemoryError: Requested array size exceeds VM limit
   *
   * @see java.util.ArrayList#MAX_ARRAY_SIZE
   */
  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

  /**
   * Increases the capacity to ensure that it can hold at least the
   * number of elements specified by the minimum capacity argument.
   *
   * @param minCapacity the desired minimum capacity
   */
  private void grow(int minCapacity) {
    // overflow-conscious code
    int oldCapacity = elementData.length;
    int newCapacity = oldCapacity + (oldCapacity >> 1);
    if (newCapacity - minCapacity < 0)
      newCapacity = minCapacity;
    if (newCapacity - MAX_ARRAY_SIZE > 0)
      newCapacity = hugeCapacity(minCapacity);
    // minCapacity is usually close to size, so this is a win:
    elementData = Arrays.copyOf(elementData, newCapacity);
  }

  private static int hugeCapacity(int minCapacity) {
    if (minCapacity < 0) // overflow
      throw new OutOfMemoryError();
    return (minCapacity > MAX_ARRAY_SIZE) ?
        Integer.MAX_VALUE :
        MAX_ARRAY_SIZE;
  }

  /**
   * An optimized version of AbstractList.Itr
   */
  private class Itr implements FloatIterator {
    int cursor;       // index of next element to return
    int lastRet = -1; // index of last element returned; -1 if no such
    int expectedModCount = modCount;

    Itr() {
    }

    public boolean hasNext() {
      return cursor != size;
    }

    @Override
    public float nextFloat() {
      checkForComodification();
      int i = cursor;
      if (i >= size)
        throw new NoSuchElementException();
      float[] elementData = PrimitiveFloatArrayList.this.elementData;
      if (i >= elementData.length)
        throw new ConcurrentModificationException();
      cursor = i + 1;
      return elementData[lastRet = i];
    }

    public void remove() {
      if (lastRet < 0)
        throw new IllegalStateException();
      checkForComodification();

      try {
        PrimitiveFloatArrayList.this.remove(lastRet);
        cursor = lastRet;
        lastRet = -1;
        expectedModCount = modCount;
      }
      catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
      }
    }

    final void checkForComodification() {
      if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
    }

    // TODO: should we override forEachRemaining to check for concurrent modifications?  (see java.util.ArrayList.Itr.forEachRemaining)
  }

  interface FloatListIterator extends FloatIterator, ListIterator<Float> {

    @Override
    default Float next() {
      // have to override in order to resolve multiple inheritance from the 2 parent interfaces
      return nextFloat();
    }

    default Float previous() {
      return previousFloat();
    }

    /**
     * Primitive specialization of {@link #previous()} to avoid auto-boxing.
     */
    float previousFloat();

    default void set(Float e) {
      set(e.floatValue());
    }

    /**
     * Primitive specialization of {@link #set(Float)} to avoid auto-boxing.
     */
    void set(float e);

    default void add(Float e) {
      add(e.floatValue());
    }

    /**
     * Primitive specialization of {@link #add(Float)} to avoid auto-boxing.
     */
    void add(float e);
  }

  /**
   * An optimized version of AbstractList.ListItr
   */
  private class ListItr extends Itr implements FloatListIterator {
    ListItr(int index) {
      super();
      cursor = index;
    }

    public boolean hasPrevious() {
      return cursor != 0;
    }

    public int nextIndex() {
      return cursor;
    }

    public int previousIndex() {
      return cursor - 1;
    }

    @Override
    public float previousFloat() {
      checkForComodification();
      int i = cursor - 1;
      if (i < 0)
        throw new NoSuchElementException();
      float[] elementData = PrimitiveFloatArrayList.this.elementData;
      if (i >= elementData.length)
        throw new ConcurrentModificationException();
      cursor = i;
      return elementData[lastRet = i];
    }

    @Override
    public void set(float e) {
      if (lastRet < 0)
        throw new IllegalStateException();
      checkForComodification();

      try {
        PrimitiveFloatArrayList.this.set(lastRet, e);
      }
      catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
      }
    }

    @Override
    public void add(float e) {
      checkForComodification();

      try {
        int i = cursor;
        PrimitiveFloatArrayList.this.add(i, e);
        cursor = i + 1;
        lastRet = -1;
        expectedModCount = modCount;
      }
      catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
      }
    }

  }

}
