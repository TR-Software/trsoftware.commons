package solutions.trsoftware.commons.shared.testutil;

import com.google.gwt.core.client.Scheduler;
import solutions.trsoftware.commons.client.util.SchedulerUtils;
import solutions.trsoftware.commons.shared.util.CollectionUtils;

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

  private Map<Class<?>, Replacement<?>> bindings = new LinkedHashMap<>();

  public Scheduler getScheduler() {
    return SchedulerUtils.getScheduler();
  }

  public Injections setScheduler(Scheduler scheduler) {
    bind(Scheduler.class, SchedulerUtils::getScheduler, SchedulerUtils::setScheduler)
        .replaceWith(scheduler);
    return this;
  }

  public void restoreAll() {
    CollectionUtils.tryForEach(bindings.values(), Replacement::restore);
  }

  @SuppressWarnings("unchecked")
  private <T> Replacement<T> bind(Class<T> cls, Supplier<T> getter, Consumer<T> setter) {
    return (Replacement<T>)bindings.computeIfAbsent(cls, aClass ->
        new Replacement<T>(getter, setter));
  }


  static class Replacement<T> {
    private T original;
    private T current;
    private Supplier<T> getter;
    private Consumer<T> setter;
    private boolean replaced;

    public Replacement(Supplier<T> getter, Consumer<T> setter) {
      this.getter = getter;
      this.setter = setter;
    }

    Replacement<T> replaceWith(T instance) {
      if (!replaced) {
        original = getter.get();  // set the backup only the first time it's replaced
      }
      setter.accept(instance);
      replaced = true;
      current = instance;
      return this;
    }

    void restore() {
      if (replaced) {
        setter.accept(original);
        original = null;
        replaced = false;
      }
    }

    public T getOriginal() {
      return original;
    }

    public T getCurrent() {
      return current;
    }

    public boolean isReplaced() {
      return replaced;
    }
  }
}
