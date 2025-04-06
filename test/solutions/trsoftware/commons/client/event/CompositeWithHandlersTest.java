package solutions.trsoftware.commons.client.event;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.controller.ExecuteEvent;
import solutions.trsoftware.commons.client.controller.FinishedEvent;
import solutions.trsoftware.commons.client.controller.RpcEvent;

import java.util.ArrayList;
import java.util.List;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertEmpty;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.getOnlyElement;
import static solutions.trsoftware.commons.shared.util.ListUtils.last;

/**
 * @author Alex
 * @since 7/21/2024
 */
public class CompositeWithHandlersTest extends CommonsGwtTestCase {

  public void testRegisterEventHandlerOnLoad() throws Exception {
    DummyCompositeWithHandlers widget = new DummyCompositeWithHandlers();
    List<RpcEvent<?>> receivedEvents = widget.receivedEvents;

    CompositeWithHandlers.Remover executeRemover = widget.registerEventHandlerOnLoad(ExecuteEvent.TYPE, widget);
    CompositeWithHandlers.Remover finishedRemover = widget.registerEventHandlerOnLoad(FinishedEvent.TYPE, widget);

    assertEmpty(receivedEvents);
    RpcEvent<?> lastEvent;
    Events.BUS.fireEvent(lastEvent = new ExecuteEvent());
    // no events received until composite is added to DOM
    assertEmpty(receivedEvents);
    RootPanel.get().add(widget);
    assertEmpty(receivedEvents);
    // fire event again now that widget's onLoad method has added the event handler
    Events.BUS.fireEvent(lastEvent = new ExecuteEvent());
    assertSame(lastEvent, getOnlyElement(receivedEvents));

    // the handler should be removed when widget is removed from DOM (onUnload)
    RootPanel.get().remove(widget);
    Events.BUS.fireEvent(lastEvent = new ExecuteEvent());
    assertNotSame(lastEvent, getOnlyElement(receivedEvents));

    // test the Remover memento returned by registerEventHandlerOnLoad
    executeRemover.remove();
    // next time the widget is added, only the FinishedEvent handler will be registered
    RootPanel.get().add(widget);
    Events.BUS.fireEvent(lastEvent = new ExecuteEvent());
    // ExecuteEvent no longer being handled
    assertNotSame(lastEvent, getOnlyElement(receivedEvents));
    // but FinishedEvent still handled
    Events.BUS.fireEvent(lastEvent = new FinishedEvent());
    assertSame(lastEvent, last(receivedEvents));
  }


  static class DummyCompositeWithHandlers extends CompositeWithHandlers
      implements ExecuteEvent.Handler, FinishedEvent.Handler {

    private final List<RpcEvent<?>> receivedEvents = new ArrayList<>();

    public DummyCompositeWithHandlers() {
      initWidget(new Label());
    }

    @Override
    public void onExecute(ExecuteEvent event) {
      receivedEvents.add(event);
    }

    @Override
    public void onFinished(FinishedEvent event) {
      receivedEvents.add(event);
    }
  }

}