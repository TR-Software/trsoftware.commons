package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * A simple "x" button used to hide a popup.
 *
 * @author Alex
 */
public class PopupCloserButton extends PopupCloser<HTML> {
  public PopupCloserButton(final PopupPanel popup) {
    /*
    We implement the "X" icon using an inline SVG tag and styled in CSS; we initially tried having just a plain text
    character (multiplication symbol), it turned out to be nearly impossible to get a char of text rendered exactly in
    the center of the button in a cross-platform way (since the exact position of a text char within a div depends on
    the specific font. However, we still include the multiplication symbol char inside the <svg> tag to support older
    browsers (IE8 and older) which should render that char if they don't support SVG.
     */
    super(popup, new HTML("<svg viewBox=\"0 0 20 20\">\n" +
        "&times;" +  // unicode multiplication symbol to support IE8 and older
        "<g class=\"xShape\">\n" +  // we group both strokes of the X under the style name "xShape" (its stroke color and width are defined in CSS)
        "  <line x1=\"5\" y1=\"5\" x2=\"15\" y2=\"15\"/>\n" +
        "  <line x1=\"5\" y1=\"15\" x2=\"15\" y2=\"5\"/>\n" +
        "</g>\n</svg>"));
    setStyleName("xButton");
  }
}
