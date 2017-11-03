package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.jso.JsDocument;

/**
 * @author Alex, 9/22/2017
 */
public class FocusableComposite extends Composite {

  private FocusWidget focusWidget;

  public FocusWidget getFocusWidget() {
    return focusWidget;
  }

  public void setFocusWidget(FocusWidget focusWidget) {
    this.focusWidget = focusWidget;
  }

  public void setFocus(boolean focused) {
    focusWidget.setFocus(focused);
  }

  @Override
  protected void initWidget(Widget widget) {
    if (focusWidget == null)
      throw new IllegalStateException(getClass().getName() + " must call setFocusWidget before initWidget");
    super.initWidget(widget);
  }

  public boolean hasFocus() {
    Element activeElement = JsDocument.get().getActiveElement();
    return activeElement != null && activeElement == focusWidget.getElement();
  }
}
