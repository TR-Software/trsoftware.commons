package solutions.trsoftware.commons.server.testutil;

/**
 * @author Alex
 * @since 4/10/2023
 */
public abstract class NamedRunnable implements Runnable {
  private final String name;

  protected NamedRunnable(String name) {
    this.name = name;
  }

  public final String getName() {
    return name;
  }

  public static NamedRunnable fromRunnable(String name, Runnable runnable) {
    return new NamedRunnable(name) {
      @Override
      public void run() {
        runnable.run();
      }
    };
  }
}
