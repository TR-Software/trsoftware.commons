package solutions.trsoftware.commons.server.management.monitoring;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Longs;
import solutions.trsoftware.commons.server.io.StringPrintStream;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static solutions.trsoftware.commons.shared.util.CollectionUtils.isEmpty;
import static solutions.trsoftware.commons.shared.util.CollectionUtils.tryForEach;

/**
 * Runs a background thread to periodically check for deadlocks using {@link ThreadMXBean#findDeadlockedThreads()}.
 * If a deadlock is detected, a {@link DeadlockEvent} will be fired to any registered {@linkplain Handler handlers}.
 * The {@linkplain DefaultDeadlockHandler default handler implementation} logs an error message containing
 * the stack traces of the deadlocked threads and optionally {@linkplain Thread#interrupt() interrupts} those threads.
 * <p>
 * An alternate way of using this class is by creating a subclass that overrides the {@link #onDeadlockDetected(DeadlockEvent)}
 * method directly, thus bypassing the handler mechanism altogether.
 * Furthermore, a deadlock check can be manually triggered at any time by invoking {@link #detectDeadlock(boolean)},
 * even if this instance is not {@linkplain #isRunning() running} a background thread.
 * <p>
 * <strong>Note:</strong>
 * <p>
 * A deadlock <i>may</i> be resolved by interrupting the deadlocked threads only when using
 * {@link java.util.concurrent.locks} for synchronization (e.g. {@link Lock#lockInterruptibly()}
 * and {@link Lock#tryLock(long, TimeUnit)}).
 * <p>
 * Unfortunately, there is no way to resolve a monitor (i.e. {@code synchronized} block) deadlock
 * other than restarting the JVM, because threads waiting to enter such a block cannot respond to interrupts
 * and cannot be killed via {@link Thread#stop()}.
 * <p>
 * If the application consistently encounters such deadlocks, it could make sense to replace all the problematic
 * {@code synchronized} blocks and methods with equivalent logic based on {@link java.util.concurrent.locks}
 * (despite their more-cumbersome syntax).  The {@link Lock#lockInterruptibly()}
 * and {@link Lock#tryLock(long, TimeUnit)} methods allow aborting a lock acquisition by externally
 * {@linkplain Thread#interrupt() interrupting} the affected threads (which is what the {@link DefaultDeadlockHandler}
 * optimistically does by default).
 *
 * @see #start(long, long)
 * @see #addHandler(Handler)
 */
public class DeadlockDetector {
  // idea for this class borrowed from https://www.javaspecialists.eu/archive/Issue130-Deadlock-Detection-with-New-Locks.html

  private static final Logger LOGGER = Logger.getLogger(DeadlockDetector.class.getName());

  private volatile Collection<Handler> handlers;
  private volatile Timer timer;
  private volatile TimerTask task;

  public DeadlockDetector() {
    this(DeadlockDetector.class.getSimpleName());
  }

  /**
   * @param name the name of the associated thread
   */
  public DeadlockDetector(String name) {
    // TODO: don't init the timer until start is invoked,
    timer = new Timer(name, true);
  }

  /**
   * Starts the repeating deadlock detection task with a fixed delay between successive executions.
   *
   * @param period time in milliseconds between successive task executions.
   * @return self, for chaining
   * @throws IllegalStateException if the task has already been started or
   *   the timer has already been {@linkplain #shutdown() shut down}
   * @see #suspend()
   * @see #shutdown()
   */
  public DeadlockDetector start(long period) {
    return start(period, period);
  }

  /**
   * Starts the repeating deadlock detection task with a fixed delay between successive executions.
   *
   * @param initialDelay  delay in milliseconds before the task is to be executed.
   * @param period        time in milliseconds between successive task executions.
   * @return self, for chaining
   * @throws IllegalStateException if the task has already been started or
   *   the timer has already been {@linkplain #shutdown() shut down}
   * @see #suspend()
   * @see #shutdown()
   */
  public synchronized DeadlockDetector start(long initialDelay, long period) {
    if (timer == null)
      throw new IllegalStateException("This instance of " + getClass().getSimpleName()
          + " has already been shut down; please create a new instance");
    if (task != null)
      throw new IllegalStateException("Already running; must invoke suspend() before restarting");
    task = new TimerTask() {
      public void run() {
        synchronized (DeadlockDetector.this) {
          // make sure the task hasn't been cancelled
          if (task != null) {
            detectDeadlock(true);
          }
        }
      }
    };
    timer.schedule(task, initialDelay, period);
    return this;
  }

  /**
   * @return {@code true} if the repeating deadlock detection task is currently scheduled for repeating execution
   *
   * @see #start(long)
   * @see #start(long, long)
   * @see #suspend()
   */
  public boolean isRunning() {
    return task != null;
  }

  /**
   * Suspends the deadlock detection task until a subsequent call to {@link #start}.
   * <b>Note:</b> in order to permanently cancel the the underlying {@linkplain Timer timer} thread and make it
   * eligible for GC, use the {@link #shutdown()} method instead.
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
  public synchronized void shutdown() {
    suspend();
    if (timer != null) {
      timer.cancel();
      timer = null;  // make eligible for GC
      task = null;
    }
  }

  /**
   * @return {@code true} if the {@link #shutdown()} method has been invoked on this instance.
   */
  public boolean isShutdown() {
    return timer == null;
  }

  /**
   * Invokes {@link ThreadMXBean#findDeadlockedThreads()} and, if a deadlock was found, returns a corresponding
   * {@link DeadlockEvent}, which provides more info than the plain {@code long[]} returned by the aforementioned method.
   * Otherwise, returns {@code null}.
   * <p>
   * This method can be used to manually trigger a deadlock check at any time, even if this instance is not
   * {@linkplain #isRunning() running} in the background.
   *
   * @param fireEvent if {@code true}, the event will be synchronously fired to any
   *   {@linkplain #addHandler(Handler) registered handlers} prior to being returned
   * @return information about the detected deadlock or {@code null} if didn't find any deadlocks
   * @see #detectDeadlock()
   */
  @Nullable
  public DeadlockEvent detectDeadlock(boolean fireEvent) {
    DeadlockEvent deadlockEvent = detectDeadlock();
    if (deadlockEvent == null)
      return null;
    if (fireEvent)
      onDeadlockDetected(deadlockEvent);
    return deadlockEvent;
  }

  /**
   * Invokes {@link ThreadMXBean#findDeadlockedThreads()} and, if a deadlock was found, returns a corresponding
   * {@link DeadlockEvent}, which provides more info than the plain {@code long[]} returned by the aforementioned method.
   * Otherwise, returns {@code null}.
   * <p>
   * This static method can be used to manually trigger a deadlock check at any time, without requiring
   * an instance of {@link DeadlockDetector} to be {@linkplain #isRunning() running} in the background.
   *
   * @return information about the detected deadlock or {@code null} if didn't find any deadlocks
   */
  @Nullable
  public static DeadlockEvent detectDeadlock() {
    ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    long[] ids = threadBean.findDeadlockedThreads();
    if (ids == null)
      return null;
    ThreadInfo[] threadInfos = threadBean.getThreadInfo(ids, true, true);
    return new DeadlockEvent(ids, threadInfos);
  }

  /**
   * Invoked when a deadlock is detected. Fires the event to any {@linkplain #addHandler(Handler) registered handlers},
   * or, in the absence of such handlers, logs the deadlock and prints a warning about no handlers being registered.
   * <p>
   * Subclasses can override this method to bypass the {@link Handler} mechanism.
   * @see DefaultDeadlockHandler
   */
  @SuppressWarnings("WeakerAccess")
  protected void onDeadlockDetected(DeadlockEvent event) {
    Collection<Handler> handlers = this.handlers;
    if (!isEmpty(handlers)) {
      tryForEach(handlers, handler -> handler.deadlockDetected(event));
    }
    else {
      // no deadlock handlers have been registered: just log the deadlock and warn that no handlers have been added
      LOGGER.severe(() -> printDeadlockInfoToString(event));
      LOGGER.warning("Deadlock detected but no deadlock handlers have been specified");
    }
  }

  public DeadlockDetector addHandler(@Nonnull Handler handler) {
    requireNonNull(handler, "handler");
    synchronized (this) {
      ensureHandlers().add(handler);
      return this;
    }
  }

  /**
   * On-demand lazy init of the {@link #handlers} collection.
   * @return the existing or newly-created collection
   */
  private synchronized Collection<Handler> ensureHandlers() {
    return handlers == null
        ? handlers = new LinkedHashSet<>()
        : handlers;
  }

  public synchronized boolean removeHandler(Handler handler) {
    return !isEmpty(handlers) && handlers.remove(handler);
  }

  public static String printDeadlockInfoToString(DeadlockEvent event) {
    StringPrintStream out = new StringPrintStream(1024);
    printDeadlockInfo(event, out);
    return out.toString();
  }

  public static void printDeadlockInfo(DeadlockEvent event, PrintStream out) {
    out.println("Deadlock Detected:");
    out.println("--------------------------------------------------------------------------------");
    out.println(event.toDebugString());
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
    private Thread[] threads;  // lazy init (caches result of getThreads method)
    private ImmutableMap<Long, String> threadInfoStrings;

    DeadlockEvent(@Nonnull long[] threadIds, @Nonnull ThreadInfo[] threadInfos) {
      this.threadIds = requireNonNull(threadIds, "threadIds");
      this.threadInfos = requireNonNull(threadInfos, "threadInfos");
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

    /**
     * @return an immutable map containing the {@link ThreadInfo#toString()} representation of the {@link ThreadInfo}
     * object corresponding to each {@link #getThreadIds() threadId}
     */
    public synchronized ImmutableMap<Long, String> getThreadInfoStrings() {
      if (threadInfoStrings == null) {
        return threadInfoStrings = Arrays.stream(threadInfos)
            .collect(ImmutableMap.toImmutableMap(ThreadInfo::getThreadId, ThreadInfo::toString));
      }
      return threadInfoStrings;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("threadIds", threadIds)
          .add("threads", threads)
          .toString();
    }

    /**
     * @return a string containing the stack traces and locked synchronizer info of the deadlocked threads
     * @see #getThreadInfoStrings()
     */
    public String toDebugString() {
      return StringUtils.join("", getThreadInfoStrings().values());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      DeadlockEvent that = (DeadlockEvent)o;

      return getThreadInfoStrings().equals(that.getThreadInfoStrings());
    }

    @Override
    public int hashCode() {
      return getThreadInfoStrings().hashCode();
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
    protected final boolean interruptThreads;

    /**
     * Equivalent to {@link #DefaultDeadlockHandler(boolean) DefaultDeadlockHandler(true)}
     */
    public DefaultDeadlockHandler() {
      this(true);
    }

    /**
     *
     * @param interruptThreads whether to interrupt the deadlocked threads, to try to resolve the deadlock.
     *   <em>Note:</em> this doesn't help with a monitor (i.e. {@code synchronized} block) deadlock, but can help
     *   if deadlocked with {@link java.util.concurrent.locks} (using {@link Lock#lockInterruptibly()}
     *   and {@link Lock#tryLock(long, TimeUnit)})
     */
    public DefaultDeadlockHandler(boolean interruptThreads) {
      this.interruptThreads = interruptThreads;
    }

    /**
     * Logs the deadlocked threads with their stack traces and owned locks,
     * and optionally {@linkplain Thread#interrupt() interrupts} those threads
     * (if {@code true} was passed to the {@linkplain #DefaultDeadlockHandler(boolean) constructor}).
     * Subclasses can override this method to perform additional actions.
     */
    public void deadlockDetected(DeadlockEvent deadlockEvent) {
      LOGGER.severe(() -> printDeadlockInfoToString(deadlockEvent));
      if (interruptThreads) {
        Thread[] threads = deadlockEvent.getThreads();
        LOGGER.severe(() -> "Interrupting deadlocked threads: " + Arrays.toString(threads));
        for (Thread thread : threads) {
          thread.interrupt();
        }
        DeadlockEvent newEvent = detectDeadlock();
        LOGGER.severe(() -> format("The deadlock was %s by interrupting the threads %s",
            (newEvent == null) ? "resolved" : "not resolved",
            Arrays.toString(threads)));
      }
    }
  }

  /**
   * Logs an error message containing all available info about the deadlocked threads, including their stack
   * traces and owned locks.
   * <p>
   * This class can be used instead of {@link DefaultDeadlockHandler} to customize the {@link Logger} and
   * {@link Level}.
   */
  public static class DeadlockLogger implements Handler {

    private final Logger logger;
    private final Level level;

    public DeadlockLogger() {
      this(LOGGER);
    }

    public DeadlockLogger(Logger logger) {
      this(logger, Level.SEVERE);
    }

    public DeadlockLogger(Logger logger, Level level) {
      this.logger = logger;
      this.level = level;
    }

    /**
     * Logs the deadlocked threads with their stack traces and owned locks
     */
    @Override
    public void deadlockDetected(DeadlockEvent deadlockEvent) {
      logger.log(level, () -> printDeadlockInfoToString(deadlockEvent));
    }
  }

}