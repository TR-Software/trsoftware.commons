package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * A simple hyperlink used to hide a popup.
 *
 * @author Alex
 */
public class PopupCloserLink extends PopupCloser<Anchor> {
  public PopupCloserLink(String text, final PopupPanel popup) {
    super(popup, new Anchor(text));
  }
}
