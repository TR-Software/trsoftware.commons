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

package solutions.trsoftware.commons.client.util.stats;

// TODO: replace PlayerStats.recentScores with this data structure for extra efficiency

import java.util.ArrayList;

/**
 * Represents a fixed-size number sample of floats.  Adding a new value when the buffer is full forces out the oldest value.
 * Can compute the mean of the contained numbers on-demand (it doesn't make sense to keep track of the mean
 * all the time, since that would require expensive arithmetic like division every time a new element is added).
 *
 * The max size is specified by implementing an abstract method. This way it doesn't take up storage on the heap,
 * and it's always a good idea to define things shared between instances as class metadata. But that's not the only
 * advantage: perf testing shows it to be 1.6 times faster to have a value returned by a method rather than having it
 * as an instance field, even if that instance field is final!
 * 
 * (the perf test results are located in a comment in CyclicFloatBufferSize10Test).
 *
 * @author Alex, 1/2/14
 */
public abstract class CyclicFloatBuffer {

  private float[] buffer;
  /** The total number of elements that have been added since creation */
  private int count;
  /** The index where the first element resides */
  private int cursor;

  /** subclasses only need to implement this one method and to call initBuffer from their constructor */
  protected abstract int maxSize();

  protected void initBuffer() {
    buffer = new float[maxSize()];
  }

  public void add(float x) {
    int size = size();
    if (size == maxSize()) {
      // evict the oldest value by advancing the wrap-around cursor
      cursor = ordinalToIndex(1);
    }
    // insert the new value
    count++;
    buffer[ordinalToIndex(size() - 1)] = x;
  }

  public float get(int index) {
    checkRange(index);
    return buffer[ordinalToIndex(index)];
  }

  private int ordinalToIndex(int index) {
    return (cursor+index) % maxSize();
  }

  private void checkRange(int index) {
    int size = size();
    if(index < 0 || index >= size)
      throw new IndexOutOfBoundsException("Should be at least 0 and less than " + size + " but was " + index);
  }

  public int size() {
    return Math.min(count, maxSize());
  }

  public double mean() {
    int size = size();
    if (size == 0)
      return 0d;
    double sum = 0d;
    for (int i = 0; i < size; i++) {
      sum += get(i);
    }
    return sum / size;
  }

  public ArrayList<Float> asList() {
    int size = size();
    ArrayList<Float> ret = new ArrayList<Float>(size);
    for (int i = 0; i < size; i++) {
      ret.add(get(i));
    }
    return ret;
  }

  public float[] asArray() {
    int size = size();
    float[] ret = new float[size];
    // NOTE: we can't easily use System.arraycopy here because the array needs to wrap around
    for (int i = 0; i < size; i++) {
      ret[i] = get(i);
    }
    return ret;
  }
}
