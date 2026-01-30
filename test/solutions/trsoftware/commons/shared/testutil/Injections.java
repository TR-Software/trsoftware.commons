package solutions.trsoftware.commons.shared.testutil;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GwtIncompatible;
import solutions.trsoftware.commons.client.util.SchedulerUtils;
import solutions.trsoftware.commons.shared.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A lightweight dependency injection facility.
 *
 * @author Alex
 * @since 9/23/2023
 */
public class Injections {
  // TODO: experimental

  private final Map<Object, Replacement<?>> bindings = new LinkedHashMap<>();
  // TODO(1/14/2026): why do we need a map with Class as key? probably to ensure that no duplicate added for Scheduler.class, but is that really necessary?

  private final ArrayList<Replacement<?>> replacements = new ArrayList<>();

  public Scheduler getScheduler() {
    return SchedulerUtils.getScheduler();
  }

  public Injections setScheduler(Scheduler scheduler) {
    bind(Scheduler.class, SchedulerUtils::getScheduler, SchedulerUtils::setScheduler)
        .replaceWith(scheduler);
    return this;
  }

  /**
   * Uses reflection to change the value of a field.
   *
   * @param field the field to be modified
   * @param obj the object on which the field will be accessed
   *   (i.e. the {@code obj} argument for {@link Field#get(Object)} and {@link Field#set(Object, Object)});
   *   can be null for a static field
   * @see #setFieldValue(Field, Object)
   */
  @GwtIncompatible("Reflection")
  @SuppressWarnings({"NonJREEmulationClassesInClientCode", "unchecked"})
  public <T> Replacement<T> setFieldValue(@Nonnull Field field, @Nullable Object obj, T newValue) {
    Replacement<T> replacement = (Replacement<T>)bindings.computeIfAbsent(field, key -> new FieldValueReplacement<>(field, obj));
    return replacement.replaceWith(newValue);
  }

  /**
   * Uses reflection to change the value of a static field.
   *
   * @param field the field to be modified
   * @param newValue the new value to be assigned to the field
   */
  @GwtIncompatible("Reflection")
  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  public <T> Replacement<T> setFieldValue(@Nonnull Field field, T newValue) {
    // TODO: rename to setStaticFieldValue?
    // Note: FieldValueReplacement constructor asserts that the field is indeed static if obj = null
    return setFieldValue(field, null, newValue);
  }

  public void restoreAll() {
    CollectionUtils.tryForEach(bindings.values(), Replacement::restore);
  }

  @SuppressWarnings("unchecked")
  private <T> Replacement<T> bind(Class<T> cls, Supplier<T> getter, Consumer<T> setter) {
    return (Replacement<T>)bindings.computeIfAbsent(cls, aClass ->
        new ReplacementImpl<T>(getter, setter));
  }

  // TODO: unit test

  /**
   * @param <V> the type of value being replaced
   * @author Alex
   * @since 1/14/2026
   */
  public static abstract class Replacement<V> implements AutoCloseable {  // TODO: implement AutoCloseable for use in try-with-resources block?

    protected V originalValue;
    protected V newValue;

    protected Runnable remover;  // TODO: use this field to remove this from bindings map after restore is invoked

    public abstract Replacement<V> replaceWith(V newValue);

    public abstract void restore();

    public V getOriginalValue() {
      return originalValue;
    }

    public V getNewValue() {
      return newValue;
    }

    public V getCurrentValue() {
      return isReplaced() ? newValue : originalValue;
    }

    // TODO: pull up shared fields and logic from subclasses

    abstract V getOriginal();

    abstract V getCurrent();

    abstract boolean isReplaced();

    @Override
    public void close() {
      // AutoCloseable implementation for use in try-with-resources block
      restore();
    }
  }

  public static class ReplacementImpl<V> extends Replacement<V> {
    private V original;
    private V current;
    private Supplier<V> getter;
    private Consumer<V> setter;
    private boolean replaced;

    public ReplacementImpl(Supplier<V> getter, Consumer<V> setter) {
      this.getter = getter;
      this.setter = setter;
    }

    @Override
    public Replacement<V> replaceWith(V newValue) {
      if (!replaced) {  // TODO(1/14/2026): maybe use AtomicBoolean.compareAndSet(false, true), or make method synchronized
        original = getter.get();  // set the backup only the first time it's replaced
      }
      setter.accept(newValue);
      replaced = true;
      current = newValue;
      return this;
    }

    @Override
    public void restore() {
      if (replaced) {
        // TODO(1/14/2026): what if value is modified externally between call to replaceWith and restore?
        //  - make sure the getter still returns the same value as this.current before invoking setter.accept(original)
        setter.accept(original);
        original = null;
        replaced = false;
      }
      // TODO(1/14/2026): should this remove itself from the bindings Map? maybe return an intermediate remover fcn from top-level Injections methods
    }

    @Override
    public V getOriginal() {
      return original;
    }

    @Override
    public V getCurrent() {
      return current;
    }

    @Override
    public boolean isReplaced() {
      return replaced;
    }
  }

}
