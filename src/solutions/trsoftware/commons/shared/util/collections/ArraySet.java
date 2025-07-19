package solutions.trsoftware.commons.shared.util.collections;

import solutions.trsoftware.commons.shared.util.ArrayUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Alex
 * @since 4/24/2025
 */
public class ArraySet<E> extends AbstractSet<E> {
  // TODO: experimental Set implementation using a simple array (similar to ArrayList)

  /**
   * Default initial capacity.
   */
  private static final int DEFAULT_CAPACITY = 10;

  /**
   * Shared empty array instance used for empty instances.
   */
  private static final Object[] EMPTY_ELEMENTDATA = {};

  /**
   * Shared empty array instance used for default sized empty instances. We
   * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
   * first element is added.
   */
  private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

  /**
   * The array buffer into which the elements of the ArraySet are stored.
   * The capacity of the ArraySet is the length of this array buffer. Any
   * empty ArraySet with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
   * will be expanded to DEFAULT_CAPACITY when the first element is added.
   */
  transient Object[] elementData; // non-private to simplify nested class access

  /**
   * The size of the ArraySet (the number of elements it contains).
   *
   * @serial
   */
  private int size;

  protected transient int modCount = 0;

  /**
   * Constructs an empty set with the specified initial capacity.
   *
   * @param  initialCapacity  the initial capacity of the set
   * @throws IllegalArgumentException if the specified initial capacity
   *         is negative
   */
  public ArraySet(int initialCapacity) {
    if (initialCapacity > 0) {
      this.elementData = new Object[initialCapacity];
    } else if (initialCapacity == 0) {
      this.elementData = EMPTY_ELEMENTDATA;
    } else {
      throw new IllegalArgumentException("Illegal Capacity: "+
          initialCapacity);
    }
  }

  /**
   * Constructs an empty set with an initial capacity of ten.
   */
  public ArraySet() {
    this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
  }

  /**
   * Constructs a set containing the elements of the specified
   * collection, in the order they are returned by the collection's
   * iterator.
   *
   * @param c the collection whose elements are to be placed into this set
   * @throws NullPointerException if the specified collection is null
   */
  public ArraySet(Collection<? extends E> c) {
    int size = c.size();
    if (size != 0) {
      if (c.getClass() == ArraySet.class) {
        elementData = c.toArray();
        this.size = size;
      } else {
        elementData = new Object[size];
        addAll(c);
      }
    } else {
      // replace with empty array.
      elementData = EMPTY_ELEMENTDATA;
    }
  }


  @Override
  public boolean contains(Object o) {
    return indexOf(o) >= 0;
  }

  private int indexOf(Object o) {
    return ArrayUtils.indexOf(elementData, o, 0, size);
  }

  @Override
  public boolean add(E e) {
    if (!contains(e)) {
      ensureCapacityInternal(size + 1);  // Increments modCount!!
      elementData[size++] = e;
      return true;
    }
    return false;
  }

  @Override
  public Iterator<E> iterator() {
    return new Itr();
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean remove(Object o) {
    int i = indexOf(o);
    if (i >= 0) {
      remove(i);
      return true;
    }
    return false;
  }
  
  private void remove(int index) {
    modCount++;
    int numMoved = size - index - 1;
    if (numMoved > 0)
      System.arraycopy(elementData, index+1, elementData, index,
          numMoved);
    elementData[--size] = null; // clear to let GC do its work
  }

  @Override
  public Object[] toArray() {
    return Arrays.copyOf(elementData, size);
  }

  /**
   * Trims the capacity of this <tt>ArraySet</tt> instance to be the
   * set's current size.  An application can use this operation to minimize
   * the storage of an <tt>ArraySet</tt> instance.
   */
  public void trimToSize() {
    modCount++;  // Note: not sure why ArrayList increments modCount even if no actual modification occurs, but assuming there must've been a reason for that
    if (size < elementData.length) {
      elementData = (size == 0)
          ? EMPTY_ELEMENTDATA
          : Arrays.copyOf(elementData, size);
    }
  }

  // internal array size management methods copied from ArrayList
  
  private static int calculateCapacity(Object[] elementData, int minCapacity) {
    if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
      return Math.max(DEFAULT_CAPACITY, minCapacity);
    }
    return minCapacity;
  }

  private void ensureCapacityInternal(int minCapacity) {
    ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
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
      /*throw new OutOfMemoryError();*/  // OutOfMemoryError not GWT-compatible
      throw new IllegalStateException("Array capacity overflow");
    return (minCapacity > MAX_ARRAY_SIZE) ?
        Integer.MAX_VALUE :
        MAX_ARRAY_SIZE;
  }

  // copied from ArrayList.Itr:
  private class Itr implements Iterator<E> {
    int cursor;       // index of next element to return
    int lastRet = -1; // index of last element returned; -1 if no such
    int expectedModCount = modCount;

    Itr() {}

    public boolean hasNext() {
      return cursor != size;
    }

    @SuppressWarnings("unchecked")
    public E next() {
      checkForComodification();
      int i = cursor;
      if (i >= size)
        throw new NoSuchElementException();
      Object[] elementData = ArraySet.this.elementData;
      if (i >= elementData.length)
        throw new ConcurrentModificationException();
      cursor = i + 1;
      return (E) elementData[lastRet = i];
    }

    public void remove() {
      if (lastRet < 0)
        throw new IllegalStateException();
      checkForComodification();

      try {
        ArraySet.this.remove(lastRet);
        cursor = lastRet;
        lastRet = -1;
        expectedModCount = modCount;
      } catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEachRemaining(Consumer<? super E> consumer) {
      Objects.requireNonNull(consumer);
      final int size = ArraySet.this.size;
      int i = cursor;
      if (i >= size) {
        return;
      }
      final Object[] elementData = ArraySet.this.elementData;
      if (i >= elementData.length) {
        throw new ConcurrentModificationException();
      }
      while (i != size && modCount == expectedModCount) {
        consumer.accept((E) elementData[i++]);
      }
      // update once at end of iteration to reduce heap write traffic
      cursor = i;
      lastRet = i - 1;
      checkForComodification();
    }

    final void checkForComodification() {
      if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
    }
  }
}
