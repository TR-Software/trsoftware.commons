package solutions.trsoftware.commons.client.logging;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import solutions.trsoftware.commons.client.styles.CellPanelStyle;

import static solutions.trsoftware.commons.client.widgets.Widgets.horizontalPanel;
import static solutions.trsoftware.commons.client.widgets.Widgets.verticalPanel;

/**
 * Creates a text area to show logging output for browsers that don't have a window.console method
 * (e.g. IE6&7, or IE8 when the developer tools panel is not open).
 *
 * Date: Nov 18, 2007
 * Time: 4:10:25 PM
 *
 * @author Alex
 */
public class DebugPanel extends Composite {

  private TextArea txtMessages = new TextArea();
  private TextArea txtScript = new TextArea();

  public DebugPanel() {
    initWidget(verticalPanel(
        horizontalPanel(new CellPanelStyle().setSpacing(5),
            new Label("Debugging Console"),
            new Button("Clear", new ClickHandler() {
              public void onClick(ClickEvent event) {
                txtMessages.setText("");
              }
            }),
            new Button("Show/Hide", new ClickHandler() {
              public void onClick(ClickEvent event) {
                txtMessages.setVisible(!txtMessages.isVisible());
              }
            })),
        txtMessages,
        horizontalPanel(
            txtScript,
            new Button("Eval", new ClickHandler() {
              public void onClick(ClickEvent event) {
                eval(txtScript.getText());

              }
            }))
    ));

    txtMessages.setCharacterWidth(80);
    txtMessages.setVisibleLines(10);

    txtScript.setCharacterWidth(80);
    txtScript.setVisibleLines(4);
  }

  public void writeMessage(String msg) {
    txtMessages.setText(msg + "\n" + txtMessages.getText());
  }

  private native void eval(String code) /*-{
    eval(code);
  }-*/;

}
