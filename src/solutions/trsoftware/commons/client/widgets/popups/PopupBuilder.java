package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Allows configuring a popup by method chaining after the constructor.
 */
public class PopupBuilder<P extends PopupPanel> {
  // TODO: test, document, and clean up this code

  private P popup;

  public PopupBuilder(P popup) {
    this.popup = popup;
  }

  public PopupBuilder<P> setAutoHideEnabled(boolean autoHide) {
    popup.setAutoHideEnabled(autoHide);
    return this;
  }

  public P getPopup() {
    return popup;
  }

}
