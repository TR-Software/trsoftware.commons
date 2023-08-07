package solutions.trsoftware.commons.server.management.monitoring;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.Longs;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.server.management.monitoring.DeadlockDetector.DeadlockEvent;
import solutions.trsoftware.commons.server.util.Duration;
import solutions.trsoftware.commons.server.util.RuntimeUtils;
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.util.stats.HashCounter;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static solutions.trsoftware.commons.shared.util.SetUtils.newSet;
import static solutions.trsoftware.commons.shared.util.StringUtils.idToString;

/**
 * @author Alex
 * @since 5/29/2023
 */
public class DeadlockDetectorTest extends BaseTestCase {

  /**
   * Creates 2 new threads which should become deadlocked after running the given code, and verifies
   * that the deadlock is detected by {@link DeadlockDetector} as well as the fired {@link DeadlockEvent}
   *
   * @return the threads created by this method
   */
  static ArrayList<Thread> testDeadlockDetection(DeadlockSimulator code) {
    DeadlockDetector detector = new DeadlockDetector(DeadlockDetector.class.getSimpleName());
    ArrayList<Thread> threads = new ArrayList<>();
    AtomicBoolean foundDeadlock = new AtomicBoolean();
    Duration testDuration = new Duration();

    int nThreads = 2;
    for (int i = 0; i < nThreads; i++) {
      Thread thread = new Thread(code);
      threads.add(thread);
      thread.start();
    }
    System.err.printf("testDeadlockDetection(%s(%s))%n", idToString(code), idToString(code.counter));

    DeadlockDetector.DefaultDeadlockHandler handler = new DeadlockDetector.DefaultDeadlockHandler() {
      @Override
      public void deadlockDetected(DeadlockEvent deadlockEvent) {
        super.deadlockDetected(deadlockEvent);
        assertEquals(newSet(threads), newSet(deadlockEvent.getThreads()));
        Set<Long> expectedThreadIds = threads.stream().map(Thread::getId).collect(Collectors.toSet());
        assertEquals(expectedThreadIds, newSet(Longs.asList(deadlockEvent.getThreadIds())));
        System.err.printf("deadlockDetected for code=%s, code.%s=%s%n", idToString(code), idToString(code.counter), code.counter);
        System.err.printf("code.%s = %s%n", idToString(code.counter), code.counter);
        synchronized (foundDeadlock) {
          foundDeadlock.set(true);
          foundDeadlock.notify();
        }
      }
    };
    detector.addHandler(handler);

    assertFalse(detector.isRunning());
    assertFalse(detector.isShutdown());
    detector.start(1, 10);
    assertTrue(detector.isRunning());

    try {
      if (!foundDeadlock.get()) {
        // if not found right away, wait long enough for the deadlock to occur and be detected
        synchronized (foundDeadlock) {
          int timeout = 10_000;
          System.out.printf("Waiting %,d ms for deadlock to be detected%n", timeout);
          try {
            foundDeadlock.wait(timeout);
          }
          catch (InterruptedException e) {
            throw new RuntimeException("Unexpectedly interrupted while waiting for a deadlock", e);
          }
          boolean found = foundDeadlock.get();
          System.out.printf("Deadlock %sdetected after %,f ms.%n", found ? "" : "not ", testDuration.elapsedMillis());
          assertTrue("Should have detected a deadlock", found);
        }
      }
    }
    finally {
      assertFalse(detector.isShutdown());
      detector.shutdown();
      assertFalse(detector.isRunning());
      assertTrue(detector.isShutdown());
    }
    return threads;
  }

  /**
   * Tests detection of a monitor (i.e. {@code synchronized} block) deadlock.
   * NOTE: This test is performed in a separate JVM to avoid ending up with deadlocked threads in the current test suite
   * at the end of this test (since monitor deadlocks cannot be recovered from).
   */
  @Slow
  public void testWithMonitorLocks() throws Exception {
    ProcessBuilder processBuilder = RuntimeUtils.buildNewJavaProcess();
    processBuilder.command().add(MonitorDeadlockTester.class.getName());
    String processName = MonitorDeadlockTester.class.getSimpleName();
    System.out.println("Starting new JVM process to run " + processName);
    Process subprocess = processBuilder.start();
    ServerIOUtils.pipeStreams(subprocess, processName);
    int exitStatus = subprocess.waitFor();
    assertEquals(processName + " exited abnormally",
        0, exitStatus);
  }

  /**
   * Helper for {@link #testWithMonitorLocks()}, intended to be run in a separate JVM process, so that the
   * permanently-deadlocked threads don't affect any other tests running in the suite
   */
  private static class MonitorDeadlockTester {

    public static void main(String[] args) {
      try {
        doTest();
        System.exit(0);  // must explicitly call exit because; otherwise will never finish with the deadlocked threads still running
      }
      catch (Throwable e) {
        e.printStackTrace();
        System.exit(1);
      }
    }

    private static void doTest() throws InterruptedException {
      testDeadlockDetection(new MonitorDeadlockSimulator());
    }

  }

  @Slow
  public void testWithInterruptibleLocks() throws Exception {
    // 1) make sure there are no pre-existing deadlocks in the system (which could throw off this test)
    assertNoDeadlock();

    // 2) perform the test by creating deadlocked threads and ensuring that they are detected
    ArrayList<Thread> threads = testDeadlockDetection(new InterruptibleDeadlockSimulator(true));

    // 3) make sure the deadlock was resolved by interrupting the threads
    assertNoDeadlock();

    // should be able to join the threads because the deadlock should've been resolved via interruption
    for (Thread thread : threads) {
      thread.join();
    }
  }

  /**
   * Asserts that no deadlock is currently present in the JVM, using {@link ThreadMXBean#findDeadlockedThreads()}
   */
  public static void assertNoDeadlock() {
    assertNull(ManagementFactory.getThreadMXBean().findDeadlockedThreads());
  }


  /**
   * Declares 2 abstract methods ({@link #f()} and {@link #g()}) that should cause a deadlock when invoked concurrently.
   * This abstraction allows simulating different types of deadlocks, e.g. with {@code synchronized} blocks or
   * {@link java.util.concurrent.locks}.
   * <p>
   * Provides a {@link #run()} method that infinitely calls {@link #f()} and {@link #g()} until the thread
   * is interrupted.  Subclasses just have to implement the 2 abstract methods in a way that causes a race condition
   * that leads to a deadlock of the desired type, and run the same instance of this {@link Runnable} in 2 or more threads.
   *
   * @see MonitorDeadlockSimulator
   * @see InterruptibleDeadlockSimulator
   */
  public abstract static class DeadlockSimulator implements Runnable {
    /**
     * Counts the number of times the critical section is reached by each thread
     */
    protected final HashCounter<String> counter = new HashCounter<>();

    public abstract void f();

    public abstract void g();

    @Override
    public void run() {
      while (!Thread.interrupted()) {
        f();
        g();
      }
    }
  }

  /**
   * Simulates a deadlock with {@code synchronized} blocks.
   * <p>
   * <strong>Warning:</strong>
   * <p>
   * There is no way to programmatically resolve this kind of deadlock without restarting the JVM process
   * (because threads waiting to enter such a block cannot respond to {@linkplain Thread#interrupt() interrupts}
   * and cannot be killed via {@link Thread#stop()}).
   * <p>
   * Any unit tests employing this code should probably <em>run in a separate JVM process</em>,
   * to avoid leaking system resources while running a large JUnit test suite.
   */
  public static class MonitorDeadlockSimulator extends DeadlockSimulator {
    private final Object lock = new Object();

    public void f() {
      synchronized (lock) {
        g();
      }
    }

    public synchronized void g() {
      synchronized (lock) {
        // do something
        counter.increment(Thread.currentThread().getName());
      }
    }
  }

  /**
   * Simulates a deadlock using {@link ReentrantLock#lockInterruptibly()}.  This type of deadlock can be resolved
   * by {@linkplain Thread#interrupt() interrupting} the affected threads.
   */
  public static class InterruptibleDeadlockSimulator extends DeadlockSimulator {
    private final ReentrantLock lock1 = new NamedReentrantLock("lock1");
    private final ReentrantLock lock2 = new NamedReentrantLock("lock2");
    private boolean verbose;

    public InterruptibleDeadlockSimulator() {
    }

    public InterruptibleDeadlockSimulator(boolean verbose) {
      this.verbose = verbose;
    }

    public void f() {
      try {
        lock2.lockInterruptibly();
        g();
      }
      catch (InterruptedException e) {
        throw new RuntimeException(formatInterruptedMsg(lock2, "f"), e);
      }
      finally {
        if (lock2.isHeldByCurrentThread())
          lock2.unlock();
      }
    }

    public void g() {
      ArrayList<Lock> heldLocks = new ArrayList<>();
      try {
        try {
          lock1.lockInterruptibly();
          heldLocks.add(lock1);
        }
        catch (InterruptedException e) {
          throw new RuntimeException(
              formatInterruptedMsg(lock1, "g"), e);
        }
        try {
          lock2.lockInterruptibly();
          heldLocks.add(lock2);
          // do the following if both locks acquired successfully:
          String currentThreadName = Thread.currentThread().getName();
          counter.increment(currentThreadName);
          if (verbose) {
            System.err.printf("%s.%s[%s]++ = %s%n", idToString(this), idToString(counter), currentThreadName, counter);
          }
        }
        catch (InterruptedException e) {
          throw new RuntimeException(formatInterruptedMsg(lock2, "g"), e);
        }
      }
      finally {
        heldLocks.forEach(Lock::unlock);
      }
    }

    private static String formatInterruptedMsg(ReentrantLock lock, String methodName) {
      return format("%s interrupted while trying to acquire %s in %s()",
          Thread.currentThread(), lock, methodName);
    }
  }

  /**
   * Extends {@link ReentrantLock} to provide a more-useful {@link #toString()} representation.
   */
  static class NamedReentrantLock extends ReentrantLock {
    private String name;

    public NamedReentrantLock(String name) {
      this.name = name;
    }

    public NamedReentrantLock(String name, boolean fair) {
      super(fair);
      this.name = name;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("name", name)
          .add("owner", getOwner())
          .toString();
    }
  }

}