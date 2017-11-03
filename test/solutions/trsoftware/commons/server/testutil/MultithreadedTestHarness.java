package solutions.trsoftware.commons.server.testutil;

import java.util.Collection;
import java.util.concurrent.*;

/**
 * Feb 21, 2009
 *
 * @author Alex
 */
public class MultithreadedTestHarness {
  private Runnable task;

  public MultithreadedTestHarness(Runnable task) {
    this.task = task;
  }

  /**
   * Executes the task the given number of times in the given number of threads
   * @return All the unchecked exceptions thrown by the task during execution, or
   * an empty collection; never null.
   */
  public Collection<Throwable> run(int nThreads, final int iterationsPerThread) throws BrokenBarrierException, InterruptedException {
    // each thread will await upon the start barrier, then run the task, then await at the finish barrier (where the main thread will be waiting)
    final CyclicBarrier startBarrier = new CyclicBarrier(nThreads);
    final CyclicBarrier finishBarrier = new CyclicBarrier(nThreads + 1);  // +1 for the main thread
    // exceptions raised by threads will be logged and returned
    final Collection<Throwable> exceptions = new ConcurrentLinkedQueue<Throwable>();

    for (int i = 0; i < nThreads; i++) {
      new Thread("Thread " + i) {
        public void run() {
          awaitOnBarrier(startBarrier, 5);
          try {
            for (int j = 0; j < iterationsPerThread; j++) {
              task.run();
            }
          }
          catch (Throwable e) {
            e.printStackTrace();
            exceptions.add(e);
          }
          finally {
            awaitOnBarrier(finishBarrier, 60);
          }
        }
      }.start();
    }
    finishBarrier.await();
    return exceptions;
  }


  /** Calls barrier.await and supresses all its checked exceptions */
  public static void awaitOnBarrier(CyclicBarrier barrier, int timeoutSeconds) {
    try {
      barrier.await(timeoutSeconds, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    catch (BrokenBarrierException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    catch (TimeoutException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}