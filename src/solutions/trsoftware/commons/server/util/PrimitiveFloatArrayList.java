package solutions.trsoftware.commons.server.util;

import solutions.trsoftware.commons.client.util.ArrayUtils;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

/**
 * A Java Collections adapter for a primitive {@code float[]} array, which uses 5x less space than {@code ArrayList<Float>}.
 * <p>
 * Some basic testing shows that a primitive {@code float[]}  of size 1000000 used up 3880.688 KB of memory, while the
 * equivalent wrapper {@code Float[]} of size 1000000 used up 19531.086 KB of memory.
 * </p>
 *
 * Oct 22, 2012
 *
 * @author Alex
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
