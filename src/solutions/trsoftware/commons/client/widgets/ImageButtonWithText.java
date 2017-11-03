package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.event.MultiHandlerRegistration;

/**
 * Displays an image with a click listener and mouseover effects, as well
 * as a link that does the same thing.
 *
 * @author Alex
 */
public class ImageButtonWithText extends Composite implements HasClickHandlers {
  private ImageButton imgButton;
  private Anchor link;

  private ImageButtonWithText(AbstractImagePrototype img, boolean imgFirst, String linkText, boolean lnkTextAsHtml) {
    imgButton = new ImageButton(img);
    link = new Anchor(linkText, lnkTextAsHtml);
    HorizontalPanel panel = new HorizontalPanel();
    panel.setVerticalAlignment(HasAlignment.ALIGN_MIDDLE);
    if (imgFirst) {
      panel.add(imgButton);
      panel.add(link);
    } else {
      panel.add(link);
      panel.add(imgButton);
    }
    initWidget(panel);
    setStyleName("ImageButtonWithText");
  }

  /** Simplified constructor, for convenience.  The image will be left of text. */
  public ImageButtonWithText(AbstractImagePrototype img, String text, boolean textAsHtml) {
    this(img, true, text, textAsHtml);
  }


  /** Simplified constructor, for convenience.  The image will be left of text. */
  public ImageButtonWithText(AbstractImagePrototype img, String text) {
    this(img, text, false);
  }

  /** Simplified constructor, for convenience. The image will be to the right of text. */
  public ImageButtonWithText(String text, AbstractImagePrototype img, boolean textAsHtml) {
    this(img, false, text, textAsHtml);
  }

  /** Simplified constructor, for convenience. The image will be to the right of text. */
  public ImageButtonWithText(String text, AbstractImagePrototype img) {
    this(text, img, false);
  }

  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return new MultiHandlerRegistration(
        imgButton.addClickHandler(handler),
        link.addClickHandler(handler));
  }

  /**
   * Allows adding the click handler using method chaining after the constructor.  This allows creating the widget
   * with a single expression, when the {@link HandlerRegistration} returned by {@link #addClickHandler(ClickHandler)}
   * is not needed.
   */
  public ImageButtonWithText withClickHandler(ClickHandler handler) {
    addClickHandler(handler);
    return this;
  }
}