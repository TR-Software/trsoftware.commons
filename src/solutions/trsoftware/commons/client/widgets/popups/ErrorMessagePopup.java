package solutions.trsoftware.commons.client.widgets.popups;

import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.images.IconsBundle;

/**
 * @author Alex, 9/26/2017
 */
public class ErrorMessagePopup extends PopupDialog {

  public ErrorMessagePopup(boolean autoHide, String headingText, String styleName, Widget bodyWidget) {
    super(autoHide, IconsBundle.Instance.get().warn24(), headingText, styleName, bodyWidget);
  }

  @Override
  protected String getSecondaryStyleName() {
    return super.getSecondaryStyleName() + " ErrorMessagePopup";
  }
}
