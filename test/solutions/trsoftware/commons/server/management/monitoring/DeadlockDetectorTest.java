package solutions.trsoftware.commons.server.management.monitoring;

import com.google.common.primitives.Longs;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.server.util.RuntimeUtils;
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.util.stats.HashCounter;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static solutions.trsoftware.commons.shared.util.SetUtils.newSet;

/**
 * @author Alex
 * @since 5/29/2023
 */
public class DeadlockDetectorTest extends BaseTestCase {


  /*@Slow
  public void testWithMonitorLocks() throws Exception {
    DeadlockDetector detector = new DeadlockDetector();
    int nThreads = 2;
    ArrayList<Thread> threads = new ArrayList<>();
    AtomicBoolean foundDeadlock = new AtomicBoolean();
    DeadlockingMonitorCode code = new DeadlockingMonitorCode();

    for (int i = 0; i < nThreads; i++) {
      Thread thread = new Thread(new DeadlockingRunnable(code));
      threads.add(thread);
      thread.start();
    }
    detector.addHandler(new DeadlockDetector.DefaultDeadlockHandler() {
      @Override
      public void deadlockDetected(DeadlockDetector.DeadlockEvent event) {
        super.deadlockDetected(event);
        assertEquals(newSet(threads), newSet(event.getThreads()));
        Set<Long> expectedThreadIds = threads.stream().map(Thread::getId).collect(Collectors.toSet());
        assertEquals(expectedThreadIds, newSet(Longs.asList(event.getThreadIds())));
        System.err.println("code.counter = " + code.counter);
        synchronized (foundDeadlock) {
          foundDeadlock.set(true);
          foundDeadlock.notify();
        }
      }
    });

    detector.start(1, 10);
//
//    thread1.join();
//    thread2.join();
    synchronized (foundDeadlock) {
      foundDeadlock.wait(10_000);
      assertTrue(foundDeadlock.get());
    }
  }*/

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
   * Helper for {@link #testWithMonitorLocks()}, intended to be run in a separate JVM process.
   */
  private static class MonitorDeadlockTester {
    private static final Logger LOGGER = Logger.getLogger(MonitorDeadlockTester.class.getName());

    public static void main(String[] args) {
      try {
        doTest();
      }
      catch (Throwable e) {
        e.printStackTrace();
        System.exit(1);
      }
    }

    private static void doTest() throws InterruptedException {
      DeadlockDetector detector = new DeadlockDetector(DeadlockDetector.class.getSimpleName());
      int nThreads = 2;
      ArrayList<Thread> threads = new ArrayList<>();
      AtomicBoolean foundDeadlock = new AtomicBoolean();
      DeadlockingMonitorCode code = new DeadlockingMonitorCode();

      for (int i = 0; i < nThreads; i++) {
        Thread thread = new Thread(new DeadlockingRunnable(code));
        threads.add(thread);
        thread.start();
      }
      detector.addHandler(new DeadlockDetector.DefaultDeadlockHandler() {
        @Override
        public void deadlockDetected(DeadlockDetector.DeadlockEvent event) {
          super.deadlockDetected(event);
          assertEquals(newSet(threads), newSet(event.getThreads()));
          Set<Long> expectedThreadIds = threads.stream().map(Thread::getId).collect(Collectors.toSet());
          assertEquals(expectedThreadIds, newSet(Longs.asList(event.getThreadIds())));
          System.err.println("code.counter = " + code.counter);
          synchronized (foundDeadlock) {
            foundDeadlock.set(true);
            foundDeadlock.notify();
          }
        }
      });

      detector.start(1, 10);
      synchronized (foundDeadlock) {
        System.out.println("Waiting for foundDeadlock");
        foundDeadlock.wait(1_000);
        System.out.println("Notified for foundDeadlock");
        assertTrue("Should have detected a deadlock", foundDeadlock.get());
        detector.terminate();
        System.exit(foundDeadlock.get() ? 0 : 1);
      }
    }
  }

  @Slow
  public void testWithReentrantLocks() throws Exception {
    DeadlockDetector detector = new DeadlockDetector(DeadlockDetector.class.getSimpleName());
    int nThreads = 2;
    ArrayList<Thread> threads = new ArrayList<>();
    AtomicBoolean foundDeadlock = new AtomicBoolean();
    InterruptibleReentrantLockCode code = new InterruptibleReentrantLockCode();

    for (int i = 0; i < nThreads; i++) {
      Thread thread = new Thread(new DeadlockingRunnable(code));
      threads.add(thread);
      thread.start();
    }
    detector.addHandler(new DeadlockDetector.DefaultDeadlockHandler() {
      @Override
      public void deadlockDetected(DeadlockDetector.DeadlockEvent event) {
        super.deadlockDetected(event);
        assertEquals(newSet(threads), newSet(event.getThreads()));
        Set<Long> expectedThreadIds = threads.stream().map(Thread::getId).collect(Collectors.toSet());
        assertEquals(expectedThreadIds, newSet(Longs.asList(event.getThreadIds())));
        System.err.println("code.counter = " + code.counter);
        synchronized (foundDeadlock) {
          foundDeadlock.set(true);
          foundDeadlock.notify();
        }
      }
    });
    detector.start(1, 10);
    // should be able to join the threads because the deadlock should've been resolved via interruption
    for (Thread thread : threads) {
      thread.join();
    }
    synchronized (foundDeadlock) {
      foundDeadlock.wait(10_000);
      assertTrue(foundDeadlock.get());
    }
  }


  public interface DeadlockingCode {
    void f();

    void g();
  }

  static class DeadlockingMonitorCode implements DeadlockingCode {
    private final Object lock = new Object();
    private HashCounter<Thread> counter = new HashCounter<>();

    public synchronized void f() {
      synchronized (lock) {
        // do something
        counter.increment(Thread.currentThread());
      }
    }

    public void g() {
      synchronized (lock) {
        f();
      }
    }
  }

  static class InterruptibleReentrantLockCode implements DeadlockingCode {
    private final ReentrantLock lock1 = new ReentrantLock();
    private final ReentrantLock lock2 = new ReentrantLock();
    private final HashCounter<Thread> counter = new HashCounter<>();

    public void f() {
      try {
        lock1.lockInterruptibly();
        lock2.lockInterruptibly();
        // do something
        counter.increment(Thread.currentThread());
      }
      catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      finally {
        lock2.unlock();
        lock1.unlock();
      }
    }

    public void g() {
      try {
        lock2.lockInterruptibly();
        f();
      }
      catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      finally {
        lock2.unlock();
      }
    }
  }

  static class DeadlockingRunnable implements Runnable {
    private final DeadlockingCode code;

    public DeadlockingRunnable(DeadlockingCode code) {
      this.code = code;
    }

    @Override
    public void run() {
      while (!Thread.interrupted()) {
        code.g();
        code.f();
      }

    }
  }

}