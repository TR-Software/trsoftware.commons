package solutions.trsoftware.commons.server.memquery.schema;

import solutions.trsoftware.commons.server.memquery.Row;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Alex, 1/5/14
 */
public abstract class ReflectionAccessorColSpec<T> extends NamedTypedColSpec<T> {

  protected ReflectionAccessorColSpec(String name, Class<T> type) {
    super(name, type);
  }

  @Override
  public T getValue(Row row) {
    Object rawData = row.getRawData();
    try {
      return getValueByReflection(rawData);
    }
    catch (Throwable e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected abstract T getValueByReflection(Object instance) throws IllegalAccessException, InvocationTargetException;

}
