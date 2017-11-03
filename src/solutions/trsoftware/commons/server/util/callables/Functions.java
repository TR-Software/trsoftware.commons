package solutions.trsoftware.commons.server.util.callables;

import solutions.trsoftware.commons.client.util.callables.Function1;

import java.lang.reflect.InvocationTargetException;

/**
 * Feb 15, 2010
 *
 * @author Alex
 */
public class Functions {

  public static <V,A> Function1<A, V> fromMethod1Arg(final Object instance, final String methodName, final Class<A> argType) {
    return new Function1<A, V>() {
      public V call(A arg) {
        try {
          return (V)instance.getClass().getMethod(methodName, argType).invoke(instance, arg);
        }
        catch (IllegalAccessException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
        catch (NoSuchMethodException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      }
    };
  }
}
