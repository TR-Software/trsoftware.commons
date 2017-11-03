package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;

/**
 * All dialogs in the web app should extend this class.  It simply ensures a consistent
 * look & feel by providing a shared CSS class name.
 *
 * @see #STYLE_NAME
 * @see #getSecondaryStyleName()
 *
 * @author Alex, 6/29/2016
 */
public class PopupDialog extends PopupWithIcon {

  /** Defines a consistent CSS class name to use for all dialog popup within our app. */
  public static final String STYLE_NAME = "trPopupDialog";

  @Override
  protected String getSecondaryStyleName() {
    return STYLE_NAME;
  }

  public PopupDialog(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName) {
    super(autoHide, icon, headingText, styleName);
  }

  public PopupDialog(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName, String closeLinkText) {
    super(autoHide, icon, headingText, styleName, closeLinkText);
  }

  public PopupDialog(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName, Widget bodyWidget) {
    super(autoHide, icon, headingText, styleName, bodyWidget);
  }

  public PopupDialog(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName, Widget bodyWidget, String closeLinkText) {
    super(autoHide, icon, headingText, styleName, bodyWidget, closeLinkText);
  }

  public PopupDialog(boolean autoHide, AbstractImagePrototype icon, String headingText, String styleName, String bodyText, String closeLinkText) {
    super(autoHide, icon, headingText, styleName, bodyText, closeLinkText);
  }
}
