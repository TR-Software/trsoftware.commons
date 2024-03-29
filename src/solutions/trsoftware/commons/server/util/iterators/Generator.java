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

package solutions.trsoftware.commons.server.util.iterators;

import com.google.common.collect.UnmodifiableIterator;
import solutions.trsoftware.commons.shared.util.Box;
import solutions.trsoftware.commons.shared.util.iterators.ResettableIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Mimics a Python generator function.
 *
 * TODO: document usage
 *
 * @author Alex, 10/27/2016
 */
public abstract class Generator<T> extends UnmodifiableIterator<T> implements ResettableIterator<T>, Iterable<T> {

  /**
   * Configurable value for the maximum size of {@link #buffer}. NOTE: The experiments performed by {@link
   * GeneratorTest#testPerformance()} clearly demonstrate that the larger the buffer, the faster the performance,
   * however, beyond {@code 1000}, it doesn't make much difference.
   */
  protected final int maxBufferSize;
  /** The buffer (a {@link BlockingQueue}) holding values generated by {@link #producerThread} */
  private final LinkedBlockingQueue<Box<T>> buffer;
  /** The thread which will run this {@link Runnable} (i.e. will call the {@link #generate()} method */
  private Thread producerThread;
  /** The producer thread will enqueue this token value onto {@link #buffer} to indicate that it's finished */
  private final Box<T> END_OF_STREAM = new Box<>();

  private static enum State {
    /** ready to begin a new generation cycle */
    READY,
    /** generation in progress, i.e. this iterator still has elements to return (can't begin a new cycle yet) */
    RUNNING,
    /** the last generation cycle is finished, {@link #hasNext()} should return {@code false} until {@code #reset()} is called */
    FINISHED
  }

  private volatile State state = State.READY;

  private volatile Box<T> next;

  private static volatile int threadCount = 0;

  public Generator(int maxBufferSize) {
    assert maxBufferSize > 0;
    this.maxBufferSize = maxBufferSize;
    buffer = new LinkedBlockingQueue<>(maxBufferSize);
  }

  /**
   * Inits {@link #maxBufferSize}{@code = 1000}.  This seems to be a good setting for it, because the experiments
   * performed by {@link GeneratorTest#testPerformance()} clearly demonstrate that the larger the buffer, the faster the
   * performance, however, beyond {@code 1000}, it doesn't make much difference.
   */
  public Generator() {
    this(1000);
  }

  private synchronized void doInit() {
    buffer.clear();
    producerThread = new Thread(() -> {
      generate();
      try {
        buffer.put(END_OF_STREAM);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }, "Generator Thread " + (++threadCount));
    producerThread.start();
  }

  @Override
  public synchronized boolean hasNext() {
    if (state == State.READY) {
      doInit();  // prepare for a new iteration over the generated sequence
      state = State.RUNNING;
    }
    if (state == State.RUNNING) {
      if (next == null) { // making sure that the previous value of "next" has been consumed by the next() method
        try {
          Box<T> nextInQueue = buffer.take();
          if (nextInQueue == END_OF_STREAM) {
            state = State.FINISHED;
            producerThread = null;  // GC the producer thread
            return false;
          }
          if (next == null)  // making sure that the previous value of "next" has been consumed by the next() method
            next = nextInQueue;
        }
        catch (InterruptedException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }
    }
    return state != State.FINISHED;
  }

  @Override
  public synchronized T next() {
    if (!hasNext())
      throw new NoSuchElementException();
    // the next value has already been extracted from the producer queue in the hasNext() method
    T ret = next.getValue();
    next = null;  // indicate that this value has been consumed
    return ret;
  }

  @Override
  public synchronized void reset() {
    if (state == State.RUNNING)
      throw new IllegalStateException("A Generator instance can't be reset while it's still running.");
    next = null;
    state = State.READY;
  }

  @Override
  public Iterator<T> iterator() {
    return this;
  }

  /**
   * Implement this method to generate a sequence by repeatedly calling {@link #yield(Object)}
   */
  protected abstract void generate();

  protected void yield(T value) {
    try {
      buffer.put(new Box<>(value));  // we're boxing the produced elements, because BlockingQueue doesn't allow null values
    }
    catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString() {
    return String.format("%s(bufferSize=%,d)", getClass().getSimpleName(), maxBufferSize);
  }
}
