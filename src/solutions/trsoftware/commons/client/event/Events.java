package solutions.trsoftware.commons.client.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.web.bindery.event.shared.EventBus;
import solutions.trsoftware.commons.client.util.StringUtils;

import static solutions.trsoftware.commons.client.util.StringUtils.methodCallToString;

/**
 * Event-handling utils.
 *
 * @author Alex, 10/8/2015
 */
public abstract class Events {

  /** Singleton event bus to be used across the application. */
  public static final EventBus BUS = GWT.create(EventBus.class);

  private Events() {
  }

  /**
   * Stops event propagation (bubbling) and prevents browser from executing its default action for this event.
   *
   * @see <a href="http://www.quirksmode.org/js/events_order.html">Browser event model explaination</a>
   * @see <a href="http://stackoverflow.com/questions/5963669/whats-the-difference-between-event-stoppropagation-and-event-preventdefault">stopPropagation() vs. preventDefault()</a>
   * @see MenuBar#eatEvent(Event)
   * */
  public static void eatEvent(NativeEvent event) {
    event.stopPropagation();
    event.preventDefault();
  }


  /** Debugging method */
  public static String toString(MouseEvent event) {
    return event.getAssociatedType().getName() + "[" + StringUtils.join(",",
        methodCallToString("coords", event.getX(), event.getY()),
        methodCallToString("screen=", event.getScreenX(), event.getScreenY()),
        methodCallToString("client=", event.getClientX(), event.getClientY())
    ) + "]";
  }

}
