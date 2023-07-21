package solutions.trsoftware.commons.server.management.monitoring;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.Longs;
import solutions.trsoftware.commons.server.io.StringPrintStream;
import solutions.trsoftware.commons.shared.util.CollectionUtils;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

/**
 * Runs a background thread to periodically check for deadlocks using {@link ThreadMXBean#findDeadlockedThreads()}.
 * If a deadlock is detected, a {@link DeadlockEvent} will be fired to any registered {@linkplain Handler handlers}.
 * The default handler implementation logs an error message containing the stack traces of the deadlocked threads
 * and optionally {@linkplain Thread#interrupt() interrupts} those threads.
 * <p>
 * <b>Note</b>:
 * A deadlock <i>may</i> be resolved by interrupting the deadlocked threads only when using
 * {@link java.util.concurrent.locks} primitives for synchronization
 * (e.g. {@link Lock#lockInterruptibly()} and {@link Lock#tryLock(long, TimeUnit)}).
 * Unfortunately, there is no way to resolve a monitor (i.e. {@code synchronized} block) deadlock
 * other than restarting the JVM.
 *
 * @see #start(int, int)
 * @see #addHandler(Handler)
 */
public class DeadlockDetector {
  // idea for this class borrowed from https://www.javaspecialists.eu/archive/Issue130-Deadlock-Detection-with-New-Locks.html

  private static final Logger LOGGER = Logger.getLogger(DeadlockDetector.class.getName());

  private final ThreadMXBean mbean =
      ManagementFactory.getThreadMXBean();
  private final Collection<Handler> listeners =
      new CopyOnWriteArraySet<Handler>();
  private volatile Timer timer;
  private volatile TimerTask task;

  public DeadlockDetector() {
    this(DeadlockDetector.class.getSimpleName());
  }

  /**
   * @param name the name of the associated thread
   */
  public DeadlockDetector(String name) {
    timer = new Timer(name, true);
  }

  /**
   * Starts the repeating deadlock detection task with a fixed delay between successive executions.
   *
   * @param period time in milliseconds between successive task executions.
   *
   * @throws IllegalStateException if the task has already been started or
   *   the timer has already been {@linkplain #terminate() terminated}
   * @see #suspend()
   * @see #terminate()
   */
  public void start(int period) {
    start(period, period);
  }

  /**
   * Starts the repeating deadlock detection task with a fixed delay between successive executions.
   *
   * @param initialDelay  delay in milliseconds before the task is to be executed.
   * @param period        time in milliseconds between successive task executions.
   *
   * @throws IllegalStateException if the task has already been started or
   *   the timer has already been {@linkplain #terminate() terminated}
   * @see #suspend()
   * @see #terminate()
   */
  public synchronized void start(int initialDelay, int period) {
    if (timer == null)
      throw new IllegalStateException("This instance of " + getClass().getSimpleName()
          + " has been terminated; must create a new instance");
    if (task != null)
      throw new IllegalStateException("Already running; must invoke suspend() first");
    task = new TimerTask() {
      public void run() {
        checkForDeadlocks();
      }
    };
    timer.schedule(task, initialDelay, period);
  }

  /**
   * @return {@code true} if the repeating deadlock detection task is currently scheduled for repeating execution
   *
   * @see #start(int)
   * @see #start(int, int)
   * @see #suspend()
   */
  public boolean isRunning() {
    return task != null;
  }

  /**
   * Suspends the deadlock detection task until a subsequent call to {@link #start}.
   * <b>Note:</b> in order to permanently cancel the the underlying {@linkplain Timer timer} thread and make it
   * eligible for GC, use the {@link #terminate()} method instead.
   */
  public synchronized void suspend() {
    if (task != null) {
      task.cancel();
      task = null;
    }
  }

  /**
   * Cancels the deadlock detection task as well as the underlying {@linkplain Timer timer} thread,
   * making it eligible for GC.
   * Subsequent calls to {@link #start} will throw an {@link IllegalStateException}, and this instance can be considered
   * dead (will need to create a new instance to restart the detection task).
   * @see #suspend()
   */
  public synchronized void terminate() {
    if (timer != null) {
      timer.cancel();
      timer = null;  // make eligible for GC
      task = null;
    }
  }

  /**
   * @return {@code true} if the {@link #terminate()} method has been invoked on this instance.
   */
  public boolean isTerminated() {
    return timer == null;
  }

  private void checkForDeadlocks() {
    long[] ids = mbean.findDeadlockedThreads();
    if (ids == null)
      return;
    ThreadInfo[] threadInfos = mbean.getThreadInfo(ids, true, true);
    fireDeadlockDetected(new DeadlockEvent(ids, threadInfos));
  }

  private void fireDeadlockDetected(DeadlockEvent event) {
    if (!listeners.isEmpty()) {
      CollectionUtils.tryForEach(listeners, handler -> handler.deadlockDetected(event));
    }
    else {
      // no deadlock handlers have been registered: just log the deadlock and warn that no handlers have been added
      LOGGER.severe(() -> printDeadlockInfoToString(event));
      LOGGER.warning("Deadlock detected but no deadlock handlers have been specified");
    }
  }

  public boolean addHandler(Handler handler) {
    return listeners.add(handler);
  }

  public boolean removeHandler(Handler handler) {
    return listeners.remove(handler);
  }

  public static String printDeadlockInfoToString(DeadlockEvent event) {
    StringPrintStream out = new StringPrintStream(1024);
    printDeadlockInfo(event, out);
    return out.toString();
  }

  public static void printDeadlockInfo(DeadlockEvent event, PrintStream out) {
    out.println("Deadlock Detected:");
    out.println("--------------------------------------------------------------------------------");
    for (ThreadInfo threadInfo : event.getThreadInfos()) {
      out.println(threadInfo);
    }
    out.println("--------------------------------------------------------------------------------");
  }


  /**
   * Event fired to the {@linkplain #addHandler(Handler) registered handlers} when a deadlock is detected.
   * Provides info about the deadlocked threads, including the thread IDs, the threads themselves, and
   * the corresponding {@link ThreadInfo} objects, which contain the stack trace and locked synchronizer info.
   *
   * @see #getThreadIds()
   * @see #getThreads()
   * @see #getThreadInfos()
   * @see Handler#deadlockDetected(DeadlockEvent)
   */
  public static class DeadlockEvent {
    private final long[] threadIds;
    private final ThreadInfo[] threadInfos;
    private Thread[] threads;  // lazy init

    DeadlockEvent(long[] threadIds, ThreadInfo[] threadInfos) {
      this.threadIds = threadIds;
      this.threadInfos = threadInfos;
    }

    /**
     * @return the IDs of the deadlocked threads
     */
    public long[] getThreadIds() {
      return threadIds;
    }

    /**
     * @return an array of objects containing the stack trace and locked synchronizer info of the deadlocked threads
     *   (array indices corresponding to the {@link #getThreadIds() threadIds})
     */
    public ThreadInfo[] getThreadInfos() {
      return threadInfos;
    }

    /**
     * @return the deadlocked threads (array indices corresponding to the {@link #getThreadIds() threadIds}
     *   and {@link #getThreadInfos() threadInfos} arrays)
     */
    public synchronized Thread[] getThreads() {
      if (threads == null) {
        // find the threads corresponding to the threadIds
        // Note: there doesn't seem to be an easier way to get a Thread by id
        return threads = Thread.getAllStackTraces().keySet().stream()
            .filter(thread -> Longs.contains(threadIds, thread.getId()))
            .sorted(Comparator.comparingInt(thread -> Longs.indexOf(threadIds, thread.getId())))
            .toArray(Thread[]::new);
      }
      return threads;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("threadIds", threadIds)
          .add("threadInfos", threadInfos)
          .add("threads", threads)
          .toString();
    }

    public String toDebugString() {
      return printDeadlockInfoToString(this);
    }
  }

  /**
   * Invoked whenever a deadlock is detected.
   */
  public interface Handler {
    void deadlockDetected(DeadlockEvent deadlockEvent);
  }

  /**
   * Default {@link Handler} implementation: logs the deadlocked threads with their stack traces and owned locks,
   * and optionally {@linkplain Thread#interrupt() interrupts} those threads.
   * Can be subclassed for additional functionality.
   *
   * @see #DefaultDeadlockHandler(boolean)
   * @see DeadlockDetector
   */
  public static class DefaultDeadlockHandler implements Handler {
    private final boolean interruptThreads;

    /**
     * Equivalent to {@link #DefaultDeadlockHandler(boolean) DefaultDeadlockHandler(true)}
     */
    public DefaultDeadlockHandler() {
      this(true);
    }

    /**
     *
     * @param interruptThreads whether to interrupt the deadlocked threads, to try to resolve the deadlock.
     *   <em>Note:</em> this doesn't help with a monitor (i.e. {@code synchronized} block) deadlock, but can help resolve
     *   a deadlock on {@link java.util.concurrent.locks} primitives (e.g. {@link Lock#lockInterruptibly()}
     *   and {@link Lock#tryLock(long, TimeUnit)})
     */
    public DefaultDeadlockHandler(boolean interruptThreads) {
      this.interruptThreads = interruptThreads;
    }

    /**
     * Logs the deadlocked threads with their stack traces and owned locks,
     * and optionally {@linkplain Thread#interrupt() interrupts} those threads, if {@code true} was passed to
     * the {@linkplain #DefaultDeadlockHandler(boolean) constructor}.
     * Subclasses can override for additional actions.
     *
     */
    public void deadlockDetected(DeadlockEvent event) {
      LOGGER.severe(() -> printDeadlockInfoToString(event));
      if (interruptThreads) {
        Thread[] threads = event.getThreads();
        LOGGER.severe(() -> "Interrupting deadlocked threads: " + Arrays.toString(threads));
        for (Thread thread : threads) {
          thread.interrupt();
        }
      }
    }
  }

}