package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import solutions.trsoftware.commons.client.util.StringUtils;

/**
 * Displays an image with a click listener and mouseover effects.
 *
 * @author Alex
 */
public class ImageButton extends Composite implements HasClickHandlers {

  private Image im;

  public ImageButton(AbstractImagePrototype img) {
    this(img.createImage());
  }

  public ImageButton(AbstractImagePrototype img, ClickHandler clickHandler) {
    this(img.createImage(), clickHandler);
  }

  public ImageButton(AbstractImagePrototype img, String title, ClickHandler clickHandler) {
    this(img, clickHandler);
    setTitle(title);
  }

  public ImageButton(Image img, ClickHandler clickHandler) {
    this(img);
    if (clickHandler != null)
      addClickHandler(clickHandler);
  }

  public ImageButton(AbstractImagePrototype img, String title) {
    this(img, title, null);
  }

  public ImageButton(Image img) {
    initWidget(im = img);
    
    // must explicitly set the size on the widget in order for its opacity style to work in IE6
    // hence we propagate whatever CSS width/height properties are present on the child image
    String imageStyleWidth = DOM.getStyleAttribute(img.getElement(), "width");
    String imageStyleHeight = DOM.getStyleAttribute(img.getElement(), "height");
    if (StringUtils.notBlank(imageStyleWidth) && StringUtils.notBlank(imageStyleHeight)) {
      setWidth(imageStyleWidth);
      setHeight(imageStyleHeight);
    }

    setStyleName("ImageButton");
  }

  public Image getImage() {
    return im;
  }

  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return im.addClickHandler(handler);
  }
}
