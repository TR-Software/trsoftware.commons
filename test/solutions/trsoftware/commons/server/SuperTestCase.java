package solutions.trsoftware.commons.server;

import solutions.trsoftware.commons.server.util.CanStopClock;
import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Since Java doesn't have multiple inheritance, it's not possible
 * to mix mulple superclasses each with a different setUp and and tearDown
 * behavior.  This class makes it easy to mix cusom setUp and tearDown
 * by letting subclasses specify them as command objects (instances of
 * SetUpTearDownDelegate).  These objects should be passed by the
 * constructor of the subclass using the addSetupTearDownDelegate method.
 *
 * @author Alex
 */
public abstract class SuperTestCase extends TestCase implements CanStopClock {

  private SetUpTearDownDelegateList delegates;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (delegates != null)
      delegates.setUpAll();
  }

  @Override
  protected void tearDown() throws Exception {
    try {
      if (delegates != null) {
        delegates.tearDownAll();
        delegates = null;
      }
      super.tearDown();
    }
    finally {
      checkForNonNullFields();
    }
  }

  /**
   * @return The same instance that was passed in, to allow method chaining.
   */
  public <T extends SetUpTearDownDelegate> T addSetupTearDownDelegate(T delegate) {
    if (delegates == null)
      delegates = new SetUpTearDownDelegateList();
    delegates.add(delegate);
    return delegate;
  }

  /**
   * Nulls out all reference fields of the subclass (and prints a warning for any fields that are found to be not null)
   * after the test case's execution.  In practice, this doesn't make much of a difference,
   * but according to the linked StackOverflow question, the instances will not be GC'd while a suite is running.
   * So if we have some {@link TestCase} subclass or a custom suite that defines a lot of test methods, it's probably
   * best to null out those fields.
   * @throws Exception
   * @see <a href="http://stackoverflow.com/questions/3653589/junit-should-i-assign-null-to-resources-in-teardown-that-were-instantiated-in">StackOverflow question</a>
   */
  private void checkForNonNullFields() throws Exception {
    // see http://stackoverflow.com/questions/3653589/junit-should-i-assign-null-to-resources-in-teardown-that-were-instantiated-in
    Class cls = getClass();
    while (!cls.equals(TestCase.class)) {
      for (Field field : cls.getDeclaredFields()) {
        Class<?> fieldType = field.getType();
        if (!Modifier.isStatic(field.getModifiers()) && !fieldType.isPrimitive() && !fieldType.isEnum()) {
          field.setAccessible(true);
          if (field.get(this) != null) {
            System.err.println("WARNING: field not null after tearDown: " + field.getDeclaringClass().getSimpleName() + "#" + field.getName() + " - it will be nulled by reflection in SuperTestCase.checkForNonNullFields");
            field.set(this, null);
          }
        }
      }
      cls = cls.getSuperclass();
    }
  }

}