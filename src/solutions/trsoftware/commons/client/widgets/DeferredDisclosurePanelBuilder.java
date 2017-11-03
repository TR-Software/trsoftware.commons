package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.util.callables.Function0;

/**
 * Creates a DiscolosurePanel which instantiates its body widget only when opened.
 *
 * Nov 5, 2009
 *
 * @author Alex
 */
public class DeferredDisclosurePanelBuilder {

  public static DisclosurePanel addDeferredBehavior(final DisclosurePanel dp, final Function0<Widget> contentWidgetFactory) {
    dp.addOpenHandler(new OpenHandler<DisclosurePanel>() {
      Widget contentWidget = null;
      public void onOpen(OpenEvent<DisclosurePanel> event) {
        if (contentWidget == null)
          dp.setContent(contentWidget = contentWidgetFactory.call());
      }
    });
    return dp;
  }

}