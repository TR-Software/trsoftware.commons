package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Alex, 3/24/2015
 */
public class InlineSimplePanel extends GenericSimplePanel {

  public InlineSimplePanel() {
    super(DOM.createSpan());
  }

  public InlineSimplePanel(Widget child) {
    super(DOM.createSpan(), child);
  }

}
