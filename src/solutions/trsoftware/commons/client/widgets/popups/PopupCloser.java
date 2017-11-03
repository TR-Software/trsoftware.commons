package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * A simple widget used to hide a popup.
 *
 * @author Alex
 */
public abstract class PopupCloser<W extends Widget & HasClickHandlers> extends Composite implements ClickHandler {
  private final PopupPanel popup;

  public PopupCloser(final PopupPanel popup, W closerWidget) {
    this.popup = popup;
    closerWidget.addClickHandler(this);
    initWidget(closerWidget);
    setTitle("close this popup");
  }

  @Override
  public W getWidget() {
    return (W)super.getWidget();
  }

  public void onClick(ClickEvent event) {
    popup.hide();
  }
}
