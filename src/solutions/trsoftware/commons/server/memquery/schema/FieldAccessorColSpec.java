package solutions.trsoftware.commons.server.memquery.schema;

import java.lang.reflect.Field;

/**
 * A fully-specified ColSpec based on a Field of some class.
 *
 * @author Alex, 1/5/14
 */
public class FieldAccessorColSpec<T> extends ReflectionAccessorColSpec<T> {

  private final Field field;

  public FieldAccessorColSpec(Field field) {
    super(field.getName(), (Class<T>)field.getType());
    field.setAccessible(true);
    this.field = field;
  }

  public Field getField() {
    return field;
  }


  @Override
  protected T getValueByReflection(Object instance) throws IllegalAccessException {
    return (T)field.get(instance);
  }


}
