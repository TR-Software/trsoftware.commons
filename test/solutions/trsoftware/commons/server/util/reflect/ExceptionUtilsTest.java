package solutions.trsoftware.commons.server.util.reflect;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.ListUtils;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.function.Function;

import static solutions.trsoftware.commons.server.util.reflect.ExceptionUtils.getFirstByType;

/**
 * @author Alex
 * @since 7/30/2019
 */
public class ExceptionUtilsTest extends TestCase {

  /**
   * List of functions that instantiate a subclass of {@link Throwable} given a cause.
   */
  private static final ImmutableList<Function<Throwable, Throwable>> randomExceptionSuppliers = ImmutableList.of(
      // some checked exceptions:
      Exception::new,
      IOException::new,
      GeneralSecurityException::new,
      // and some unchecked exceptions:
      RuntimeException::new,
      IllegalArgumentException::new,
      IllegalStateException::new
  );

  public void testGetFirstByType() throws Exception {

    DummyException dummyException = new DummyException(getName() + "_" + 1);
    DummyException dummyException2 = new DummyException(getName() + "_" + 2);
    for (int i = 0; i < 5; i++) {
      // 1) should return null if arg is null (rather than throwing NPE)
      assertNull(getFirstByType(null, DummyException.class, i));
      // 2) search target is at the top
      assertSame(dummyException, getFirstByType(dummyException, DummyException.class, i));
      // 3) search target is buried somewhere in the cause chain
      for (int j = 0; j < i; j++) {
        assertSame(dummyException, getFirstByType(buryException(dummyException, j), DummyException.class, i));
      }
    }


    // TODO: test the following cases:
    // - multiple instances of the target class in chain
    // - instances of subclasses of the target class in chain
  }

  public void testBuryException() throws Exception {
    for (int depth = 0; depth < 5; depth++) {
      DummyException rootCause = new DummyException(getName());
      assertNull(rootCause.getCause());
      Throwable result = buryException(rootCause, depth);
      result.printStackTrace(System.out);
      // make sure that rootCause still doesn't have a cause itself
      assertNull(rootCause.getCause());
      List<Throwable> causalChain = Throwables.getCausalChain(result);
      assertEquals(depth + 1, causalChain.size());
      assertSame(rootCause, ListUtils.last(causalChain));
    }
  }

  /**
   * Creates a causal chain of random exceptions, with the given throwable as the root cause (at the lowest level).
   *
   * @param depth the index of how deep to bury the throwable (where 0 indicates the top level):
   *   the number of layers in the generated causal chain will be {@code depth + 1}
   */
  public static Throwable buryException(Throwable rootCause, int depth) {
    Throwable ret = rootCause;
    for (int j = 0; j < depth; j++) {
      ret = RandomUtils.randomElement(randomExceptionSuppliers).apply(ret);
    }
    return ret;
  }

  public static class DummyException extends Exception {
    public DummyException() { }

    public DummyException(String message) {
      super(message);
    }

    public DummyException(String message, Throwable cause) {
      super(message, cause);
    }

    public DummyException(Throwable cause) {
      super(cause);
    }
  }
}