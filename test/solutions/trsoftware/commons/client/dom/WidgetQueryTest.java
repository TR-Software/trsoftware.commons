package solutions.trsoftware.commons.client.dom;

import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.widgets.popups.DialogBox;

import static solutions.trsoftware.commons.client.dom.WidgetQuery.ancestorOf;
import static solutions.trsoftware.commons.client.dom.WidgetQuery.hideParentPopupPanel;
import static solutions.trsoftware.commons.client.widgets.Widgets.*;

/**
 * @author Alex
 * @since 2/21/2024
 */
public class WidgetQueryTest extends CommonsGwtTestCase {

  /**
   * Widgets in the widget tree to test against
   */
  private Widget[] w;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    // create a widget tree to test against
    w = new Widget[20];
    w[0] = flowPanel(
        w[1] = horizontalPanel(
            w[2] = new Label(),
            w[3] = flowPanel(
                w[4] = new Image(),
                w[5] = verticalPanel(
                    w[6] = simplePanel(
                        w[7] = new Label()
                    ),
                    w[8] = flowPanel(
                        w[9] = new HTML(),
                        w[10] = new TextBox()
                    )
                )
            )
        )
    );
  }

  public void testAncestorOf() throws Exception {
    assertSame(w[8], ancestorOf(w[9], FlowPanel.class));
    assertSame(w[8], ancestorOf(w[9], widget -> widget instanceof FlowPanel));
    
    assertSame(w[1], ancestorOf(w[9], HorizontalPanel.class));
    assertSame(w[1], ancestorOf(w[9], widget -> widget instanceof HorizontalPanel));

    // test a more complex predicate
    assertSame(w[5], ancestorOf(w[9], widget ->
        widget instanceof ComplexPanel && ((ComplexPanel)widget).getWidgetIndex(w[6]) >= 0));
  }

  public void testHideParentPopupPanel() throws Exception {
    DialogBox popup = new DialogBox();
    popup.setWidget(w[0]);
    assertFalse(popup.isShowing());
    popup.show();
    assertTrue(popup.isShowing());
    hideParentPopupPanel(w[10]);
    assertFalse(popup.isShowing());
  }

}