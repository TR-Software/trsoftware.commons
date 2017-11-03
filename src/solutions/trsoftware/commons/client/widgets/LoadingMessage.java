package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import solutions.trsoftware.commons.client.styles.CellPanelStyle;

/**
 * Date: Dec 18, 2007
 * Time: 9:37:13 PM
 *
 * @author Alex
 */
public class LoadingMessage extends Composite {
  
  public LoadingMessage(String message, boolean startVisible) {
    initWidget(Widgets.horizontalPanel(new CellPanelStyle().setSpacing(5),
        new LoadingImage(),
        new Label(message)));
    setStyleName("loading-message");
    if (!startVisible)
      setVisible(false);
  }

  public LoadingMessage(boolean startVisible) {
    this("Loading...", startVisible);
  }

  public LoadingMessage() {
    this(true);
  }

  public LoadingMessage(String message) {
    this(message, true);
  }

}