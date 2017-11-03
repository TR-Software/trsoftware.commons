package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.util.geometry.Alignment;
import solutions.trsoftware.commons.client.util.geometry.RelativePosition;

/**
 * Convenience class to save code when the popup already exists at construction time and that same instance is always
 * the one to show (cleaner than having to create an anonymous inner class).
 *
 * @author Alex, 2/17/2016
 */
public class ConcretePopupOpener<W extends Widget, P extends EnhancedPopup> extends PopupOpener<W, P> {

  // TODO: get rid of this class now that we have the following capability:
  {
    setReusePopup(true);
  }

  /**
   * Same as {@link PopupOpener#PopupOpener(Widget, int, RelativePosition)}.
   *
   * @param popup The singleton instance to always show.
   */
  public ConcretePopupOpener(W opener, P popup, int eventBits, RelativePosition position) {
    super(opener, eventBits, position);
    this.popup = popup;
  }

  /**
   * Same as {@link PopupOpener#PopupOpener(Widget, int, Alignment...)}.
   *
   * @param popup The singleton instance to always show.
   */
  public ConcretePopupOpener(W opener, P popup, int eventBits, Alignment... alignmentPrefs) {
    super(opener, eventBits, alignmentPrefs);
    this.popup = popup;
  }

  @Override
  protected P createPopup() {
    throw new IllegalStateException("ConcretePopupOpener.createPopup should never be called.");  // because popup != null
  }
}
