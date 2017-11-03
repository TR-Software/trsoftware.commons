package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FocusWidget;

/**
 * A virtually invisible "honeypot" widget that can receive keyboard focus, upon the receipt of which, it returns
 * focus to its master widget.  This allows forcibly maintaining keyboard focus on the master widget at all times
 * by surrounding it with two of these trap widgets. This will prevent the ability of the user to move keyboard focus
 * somewhere else when pressing the Tab and Shift+Tab keys. Therefore this widget should only be used in modal dialogs.
 *
 * Tested in Chrome, FF, and IE8/9/10/11 on Windows.
 */
public class FocusTrap extends Anchor implements FocusHandler {
  private FocusWidget master;

  public FocusTrap(FocusWidget master) {
    super("", true);
    // NOTE: if we hide this widget using CSS (visibility:hidden or display:none), it will nto receive keyboard focus,
    // so instead we make it "invisible" by making the anchor text empty and removing the underline
    getElement().getStyle().setTextDecoration(Style.TextDecoration.NONE);
    this.master = master;
    addFocusHandler(this);
  }

  public FocusWidget getMaster() {
    return master;
  }

  public void setMaster(FocusWidget master) {
    this.master = master;
  }

  @Override
  public void onFocus(FocusEvent event) {
    master.setFocus(true);
  }

}
