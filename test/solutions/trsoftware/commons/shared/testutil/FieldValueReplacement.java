package solutions.trsoftware.commons.shared.testutil;

import com.google.gwt.core.shared.GwtIncompatible;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Temporarily replaces a field value for testing
 *
 * @param <V> the field's value type
 *
 * @see solutions.trsoftware.commons.shared.testutil.Injections.Replacement
 * @see <a href="https://docs.google.com/document/d/1zXm6Htm5qFgbitSTI5l-1rbPJiNMSdcwjAgoOhQVl2M/edit?tab=t.0">
 *   Google AI answer for query "java use reflection to modify static final field"</a>
 * @author Alex
 * @since 1/14/2026
 */
@GwtIncompatible("Reflection")
@SuppressWarnings("NonJREEmulationClassesInClientCode")
public class FieldValueReplacement<V> extends Injections.Replacement<V> implements AutoCloseable {
  // TODO: unit test

  /**
   * The field to be modified
   */
  private final Field field;
  /**
   * The object on which the field will be accessed
   *   (i.e. the {@code obj} argument for {@link Field#get(Object)} and {@link Field#set(Object, Object)});
   *   can be null for a static field
   */
  @Nullable
  private final Object obj;

  /**
   * True when the field has been assigned a new value at least once
   */
  private final AtomicBoolean replaced = new AtomicBoolean();
  private V originalValue;
  private V newValue;

  /**
   * @param field the field to be modified
   * @param obj the object on which the field will be accessed
   *   (i.e. the {@code obj} argument for {@link Field#get(Object)} and {@link Field#set(Object, Object)});
   *   can be null for a static field
   * @throws AssertionError if {@code obj} is null but the field is not {@code static}
   */
  public FieldValueReplacement(@Nonnull Field field, @Nullable Object obj) {
    this.field = requireNonNull(field, "field");
    assert obj != null || Modifier.isStatic(field.getModifiers()): "Must provide non-null object for field access";
    this.obj = obj;
  }

  /**
   * Replacement for a static field value
   * (which doesn't require an object for for {@link Field#get(Object)} and {@link Field#set(Object, Object)}).
   *
   * @param field the static field to be modified
   * @throws AssertionError if {@code obj} is null but the field is not {@code static}
   */
  public FieldValueReplacement(@Nonnull Field field) {
    this(field, null);
  }

  /**
   * Uses reflection to temporarily change the value of a field.
   *
   * @param field the field to be modified
   * @param obj the object on which the field will be accessed
   *   (i.e. the {@code obj} argument for {@link Field#get(Object)} and {@link Field#set(Object, Object)});
   *   can be null for a static field
   * @param newValue the new value to be assigned to the field
   * @return an instance of this class which can be used in a try-with-resources block to {@linkplain #restore() restore}
   *   the original value
   * @see #setFieldValue(Field, Object)
   * @throws AssertionError if {@code obj} is null but the field is not {@code static}
   */
  public static <V> FieldValueReplacement<V> setFieldValue(@Nonnull Field field, @Nullable Object obj, V newValue) {
    return new FieldValueReplacement<V>(field, obj).replaceWith(newValue);
  }

  /**
   * Uses reflection to temporarily change the value of a static field
   * (which doesn't require an object for for {@link Field#get(Object)} and {@link Field#set(Object, Object)}).
   *
   * @param field the field to be modified
   * @param newValue the new value to be assigned to the field
   * @throws AssertionError if the field is not {@code static}
   */
  @GwtIncompatible("Reflection")
  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  public static <V> FieldValueReplacement<V> setFieldValue(@Nonnull Field field, V newValue) {
    // TODO: rename to setStaticFieldValue?
    // Note: FieldValueReplacement constructor asserts that the field is indeed static if obj = null
    return setFieldValue(field, null, newValue);
  }

  /**
   * Assigns the given value to the field, overriding the field's {@code private} and {@code final} modifiers if needed
   * @param newValue the value to assign to the field.
   * @return self, for chaining
   */
  @Override
  public FieldValueReplacement<V> replaceWith(V newValue) {
    try {
      replaceWithImpl(newValue);
    }
    catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   * Reverts the field to its original value if it hasn't been modified externally since the invocation of {@link #replaceWith(Object)}
   * @throws IllegalStateException if the field has been modified externally
   */
  @Override
  public void restore() {
    try {
      restoreImpl();
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public V getOriginal() {
    return originalValue;
  }

  @Override
  public V getCurrent() {
    return newValue;
  }

  @Override
  public boolean isReplaced() {
    return replaced.get();
  }

  private synchronized void replaceWithImpl(V newValue) throws ReflectiveOperationException {
    // TODO: maybe don't need AtomicBoolean since method is now synchronized?
    if (replaced.compareAndSet(false, true)) {
      ensureModifiable(field);
      originalValue = getValue();  // set the backup only the first time it's replaced
    }
    setValue(newValue);
    this.newValue = newValue;
  }

  private synchronized void restoreImpl() throws IllegalAccessException {
    // TODO: maybe don't AtomicBoolean since method is now synchronized?
    if (replaced.compareAndSet(true, false)) {
      // make sure the field value wasn't modified externally between call to replaceWith and restore
      Object currentValue = getValue();
      if (fieldValueEquals(currentValue))
        setValue(originalValue);  // field still has the same value that was provided to replaceWith
      else
        // TODO: maybe ignore this instead of throwing an exception?
        throw new IllegalStateException(
            format("Current value of field %s (%s) doesn't match the value passed to replaceWith (%s)",
                field, currentValue, newValue));
      originalValue = null;
      newValue = null;
      // TODO: do we need to revert the modifier changes made by ensureModifiable?
    }
  }

  private boolean fieldValueEquals(Object currentValue) {
    // TODO: explain this
    if (field.getType().isPrimitive())
      return Objects.equals(currentValue, newValue);
    else
      return currentValue == newValue;
  }

  private V getValue() throws IllegalAccessException {
    return (V)field.get(obj);
  }

  private void setValue(V newValue) throws IllegalAccessException {
    field.set(obj, newValue);
  }

  private static void ensureModifiable(Field field) throws ReflectiveOperationException {
    // Note: this code is based on Google AI answer for query "java use reflection to modify static final field" (https://docs.google.com/document/d/1zXm6Htm5qFgbitSTI5l-1rbPJiNMSdcwjAgoOhQVl2M/edit?tab=t.0)
    ReflectionUtils.ensureAccessible(field);
    int modifiers = field.getModifiers();
    if (Modifier.isFinal(modifiers)) {
      // remove the final modifier by changing the value of Field.modifiers
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
      assert !Modifier.isFinal(field.getModifiers());  // mission accomplished
    }
    // Note: the above overrides are temporary: future invocations of getDeclaredField will have the original modifiers and accessibility
  }


}
