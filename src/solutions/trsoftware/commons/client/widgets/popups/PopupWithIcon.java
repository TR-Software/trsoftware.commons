package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.widgets.Widgets;

/**
 * Date: Apr 8, 2008 Time: 4:33:12 PM
 *
 * @author Alex
 */
public class PopupWithIcon extends DialogBox {

  private FlowPanel pnlMain;
  private SimplePanel bodyWidgetHolder = new SimplePanel();
  {
    bodyWidgetHolder.setStyleName("bodyWidgetHolder");
  }

  private PopupCloserLink popupCloserLink;
  public static final String CLOSE_LINK_TEXT_DEFAULT = "close";
  public static final String CLOSE_LINK_TEXT_CANCEL = "Cancel";
  public static final String CLOSE_LINK_TEXT_OK = "OK";

  public PopupWithIcon(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName) {
    this(autoHide, icon, headingText, styleName, CLOSE_LINK_TEXT_DEFAULT);
  }

  public PopupWithIcon(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName, String closeLinkText) {
    this(autoHide, icon, headingText, styleName, (Widget)null, closeLinkText);
  }

  public PopupWithIcon(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName, Widget bodyWidget) {
    this(autoHide, icon, headingText, styleName, bodyWidget, CLOSE_LINK_TEXT_DEFAULT);
  }

  private static class CaptionWithIcon extends CompositeCaption {
    private CaptionWithIcon(AbstractImagePrototype icon, String text) {
      initWidget(Widgets.flowPanel(icon.createImage(), Widgets.inlineLabel(text, "captionText")));
    }
  }

  /** Creates a popup with the given body widget and a link that closes the popup */
  public PopupWithIcon(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName, Widget bodyWidget, String closeLinkText) {
    super(autoHide, false, new CaptionWithIcon(icon, headingText));
    if (bodyWidget != null)
      bodyWidgetHolder.setWidget(bodyWidget);
    pnlMain = Widgets.flowPanel(bodyWidgetHolder);
    if (closeLinkText != null) {
      insertCloserButton(new PopupCloserButton(this));
      if (!closeLinkText.equals(CLOSE_LINK_TEXT_DEFAULT)) {
        // omit the close link altogether if just says "close", since we've introduced the closer button (created with the above call to insertCloserButton)
        setCloserLinkText(closeLinkText);
      }
    }
    setWidget(pnlMain);
    addStyleName(getSecondaryStyleName());
    if (styleName != null)
      addStyleName(styleName);
  }

  /** Creates a popup with the given body widget and a link that closes the popup */
  public PopupWithIcon(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName, String bodyText, String closeLinkText) {
    this(autoHide, icon, headingText, styleName, new Label(bodyText), closeLinkText);
  }

  /** Subclasses may override this method to use a different name for the secondary style (CSS class attribute value) */
  protected String getSecondaryStyleName() {
    return "PopupWithIcon";
  }

  public Widget getBodyWidget() {
    return bodyWidgetHolder.getWidget();
  }

  public void setBodyWidget(Widget bodyWidget) {
    this.bodyWidgetHolder.setWidget(bodyWidget);
  }

  public void setCloserLinkText(String closerLinkText) {
    if (popupCloserLink != null)
      popupCloserLink.getWidget().setText(closerLinkText);
    else {
      // no closer link has been added to this popup yet, so we add it now
      popupCloserLink = new PopupCloserLink(closerLinkText, this);
      SimplePanel containerDiv = new SimplePanel(popupCloserLink);
      // we wrap the closer link in a container div to have it displayed on the right
      containerDiv.getElement().getStyle().setTextAlign(Style.TextAlign.RIGHT);
      pnlMain.add(containerDiv);
    }
  }

  /**
   * Controls the visibility of the hyperlink that was created by {@link #setCloserLinkText(String)}
   */
  public void setCloserLinkVisible(boolean visible) {
    if (popupCloserLink != null)
      popupCloserLink.setVisible(visible);
  }

  @Override
  public void setCloserControlsEnabled(boolean enabled) {
    setCloserLinkVisible(enabled);
    super.setCloserControlsEnabled(enabled);
  }
}
