package solutions.trsoftware.commons.server.event;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gwt.dev.util.collect.IdentityHashSet;
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.util.StackTraceWrapper;
import solutions.trsoftware.commons.shared.annotations.Slow;

import java.util.IdentityHashMap;
import java.util.List;

/**
 * @author Alex
 * @since 10/2/2019
 */
public class EventRecorderTest extends TestCase {

  /**
   * Tests EventBus behavior with polymorphic events: demonstrates that if a listener subscribes to both an event
   * class and its superclass, it will receive the same event twice.
   */
  @Slow
  public void testEventPolymorphism() throws Exception {
    EventBus eventBus = new EventBus();
    Listener listener = new Listener();
    eventBus.register(listener);

    eventBus.post(new Foo());
    eventBus.post(new FooSub1());
    eventBus.post(new FooSub2());

    eventBus.post(new Bar());
    eventBus.post(new BarSub1());
    eventBus.post(new BarSub2());

    List<Event> allEvents = listener.getAllEvents();
    IdentityHashSet<Event> uniqueEvents = new IdentityHashSet<>(allEvents);

    System.out.printf("Listener recorded %d events (%d unique):%n", allEvents.size(), uniqueEvents.size());
    IdentityHashMap<Event, Integer> duplicateIndices = new IdentityHashMap<>();
    int i = 1;
    for (Event event : allEvents) {
      StringBuilder msg = new StringBuilder(event.toString());
      Integer dup = duplicateIndices.putIfAbsent(event, i);
      if (dup != null)
        msg.append(" (duplicate of #").append(dup).append(')');
      System.out.printf("  %2d. %s%n", i, msg);
      i++;
    }


  }


  private static class Foo extends AbstractEvent {}
  private static class FooSub1 extends Foo {}
  private static class FooSub2 extends Foo {}
  private static class Bar extends AbstractEvent {}
  private static class BarSub1 extends Bar {}
  private static class BarSub2 extends Bar {}


  private static class Listener extends EventRecorder<Event> {
    @Subscribe
    public void onEvent(Event event) {
//      System.out.println("Listener.onEvent(Event): " + event);
      addEvent(event, new StackTraceWrapper(eventToString(event)));
    }

    @Subscribe
    public void onEvent(Foo event) {
//      System.out.println("Listener.onEvent(Foo): " + event);
      addEvent(event, new StackTraceWrapper(eventToString(event)));
    }

    void addEvent(Event event, StackTraceWrapper stackTrace) {
      stackTrace.printStackTrace(System.out);
      System.out.println("--------------------------------------------------------------------------------");
      super.addEvent(event);
    }
  }

  static String eventToString(Event event) {
    return event.toString() + "@" + Integer.toHexString(System.identityHashCode(event));
  }

}