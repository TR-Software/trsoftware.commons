package solutions.trsoftware.commons.server.memquery.schema;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A fully-specified ColSpec based on a Method of some class.
 *
 * @author Alex, 1/5/14
 */
public class MethodAccessorColSpec<T> extends ReflectionAccessorColSpec<T> {

  private final Method method;

  public MethodAccessorColSpec(Method method) {
    super(method.getName(), (Class<T>)method.getReturnType());
    method.setAccessible(true);
    this.method = method;
  }

  public Method getMethod() {
    return method;
  }

  @Override
  protected T getValueByReflection(Object instance) throws InvocationTargetException, IllegalAccessException {
    return (T)method.invoke(instance);
  }

}
