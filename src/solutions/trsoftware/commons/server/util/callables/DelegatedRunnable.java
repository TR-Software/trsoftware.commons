package solutions.trsoftware.commons.server.util.callables;

/**
 * Wraps a {@link Runnable}, allowing execution of some additional code before (override {@link #doBefore()})
 * and after (override {@link #doAfter(Exception)}) the delegate's {@link Runnable#run() run()} method is invoked.
 *
 * @see #doRun()
 * @author Alex
 * @since 11/29/2017
 */
public class DelegatedRunnable implements Runnable {

  private final Runnable delegate;

  /**
   * @param delegate will be invoked by {@link #doRun()}; could pass {@code null}, but in that case should also
   * override {@link #doRun()} to avoid a {@link NullPointerException}
   */
  public DelegatedRunnable(Runnable delegate) {
    this.delegate = delegate;
  }

  /**
   * Subclasses can override if they wish to do something before {@link #doRun()}.
   */
  protected void doBefore() {}
  
  /**
   * Subclasses can override if they wish to do something after {@link #doRun()}.
   * This method is guaranteed to execute even if {@link #doRun()} threw an exception.
   *
   * @param ex any exception that was thrown by {@link #doRun()}, or {@code null} if no exception was thrown.
   */
  protected void doAfter(Exception ex) {}

  @Override
  public final void run() {
    doBefore();
    try {
      doRun();
    }
    catch (RuntimeException e) {
      doAfter(e);
      throw e;
    }
    doAfter(null);
  }

  /**
   * Invokes {@link #delegate}{@code .}{@link Runnable#run() run()}. Subclasses should override if they don't
   * pass a delegate instance to the constructor, otherwise will throw a {@link NullPointerException}.
   *
   * @see #DelegatedRunnable(Runnable)
   * @throws NullPointerException if a delegate instance wasn't passed to the constructor; subclasses can override
   * this method to avoid this exception
   */
  protected void doRun() {
    delegate.run();
  }
}
