package solutions.trsoftware.commons.server.event;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static solutions.trsoftware.commons.server.util.reflect.ReflectionUtils.filterByType;

/**
 * Records events received via an {@link EventBus}.
 *
 * Subclasses should define methods marked with a {@link Subscribe} annotation that call {@link #addEvent(Event)}
 * and call {@link EventBus#register(Object)} with this instance.
 *
 * @author Alex
 * @since 9/30/2019
 */
public abstract class EventRecorder<T extends Event> {
  /*
  TODO: since this class takes a type parameter, it doesn't really make sense to have getters that take an event type:
  might want to extract that functionality into a separate class (perhaps a superclass of this)
   */

  /**
   * We store all the events as a sequential list.
   */
  private final ArrayList<T> events = new ArrayList<>();

  /*
   * TODO: for better query performance, can use either a
   * - {@code Multimap<Class, Event>} to segment the events by type
   * - NavigableMap by timestamp to improve querying by time range (NOTE: would have to avoid collisions in timestamp,
   *   perhaps by using a key type that's a composite of timestamp and System.nanoTime)
   */

  private int lastSize = 0;

  /**
   * @param startTime the lower bound in epoch millis (inclusive)
   * @param endTime the upper bound in epoch millis (exclusive)
   * @return all events with timestamp within the given bounds
   */
  public synchronized List<T> getEventsBetween(long startTime, long endTime) {
    return events.stream().filter(Event.timestampFilter(startTime, endTime))
        .collect(Collectors.toList());
  }

  /**
   * @param startTime the lower bound in epoch millis (inclusive)
   * @param endTime the upper bound in epoch millis (exclusive)
   * @return all events of the given type with timestamp within the given bounds
   */
  public synchronized <E extends Event> List<E> getEventsBetween(long startTime, long endTime, Class<E> eventType) {
    return filterByType(events.stream(), eventType).filter(Event.timestampFilter(startTime, endTime))
        .collect(Collectors.toList());
  }

  /**
   * Sets the pointer from which methods such as {@link #getLatestEvents} will derive their results.
   */
  public synchronized void mark() {
    lastSize = events.size();
  }

  /**
   * Sets the {@link #lastSize} pointer and returns the sub-list of events recorded since the last invocation
   * of any method that updates {@link #lastSize}.
   *
   * @return the sub-list of events recorded since the last invocation of any method that updates {@link #lastSize}.
   * NOTE: modifications of the returned list affect the internal state of this class, so a defensive copy should be
   * returned from any public method that uses this helper.
   */
  @Nonnull
  private List<T> newEventsSubList() {
    List<T> ret = events.subList(lastSize, events.size());
    mark();
    return ret;
  }

  /**
   * @return the events recorded since the last invocation of this method.
   * @see #mark()
   */
  public synchronized List<T> getLatestEvents() {
    return ImmutableList.copyOf(newEventsSubList());
  }

  /**
   * @return the events of the given type recorded since the last invocation of this method.
   * @see #mark()
   */
  public synchronized <E extends Event> List<E> getLatestEvents(Class<E> eventType) {
    // TODO: the newEventsSubList() method will mark all events as seen (we probably want to mark just events of the given type here)
    return filterByType(newEventsSubList().stream(), eventType)
        .collect(Collectors.toList());
  }

  /**
   * @return all the events recorded by this instance.
   */
  public synchronized List<T> getAllEvents() {
    return ImmutableList.copyOf(events);
  }

  /**
   * @return all the events of the given type recorded by this instance.
   */
  public synchronized <E extends Event> List<E> getAllEvents(Class<E> eventType) {
    return filterByType(events.stream(), eventType).collect(Collectors.toList());
  }

  /**
   * Subclasses should call this from their {@linkplain Subscribe listener} methods to add the received event.
   */
  public synchronized void addEvent(T event) {
    events.add(event);
  }

  public synchronized void printAllEvents(PrintStream out) {
    events.forEach(out::println);
  }

/*  @Subscribe void handleEvent(T event) {
    addEvent(event);
    System.out.println(event);
  }*/
}