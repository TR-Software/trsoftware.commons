package solutions.trsoftware.grecaptcha.client;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.styles.CellPanelStyle;
import solutions.trsoftware.commons.shared.util.StringUtils;

import static solutions.trsoftware.commons.client.widgets.Widgets.flowPanel;
import static solutions.trsoftware.commons.client.widgets.Widgets.horizontalPanel;

/**
 * Debugging widget for manually testing the reCaptcha integration.
 */
public class ReCaptchaWidgetTester extends Composite {

  private ReCaptchaWidget reCaptchaWidget;

  // config parameters:
  private TextBox txtSiteKey = new TextBox();
  private TextBox txtTheme = new TextBox();
  private TextBox txtSize = new TextBox();
  
  private SimplePanel container = new SimplePanel();

  public ReCaptchaWidgetTester(String siteKey) {
    txtSiteKey.setText(siteKey);
    txtSiteKey.setVisibleLength(siteKey.length());
    initWidget(flowPanel(
        // config options:
        horizontalPanel(new CellPanelStyle().setSpacing(2),
            new InlineLabel("sitekey:"), txtSiteKey,
            new InlineLabel("theme:"), txtTheme,
            new InlineLabel("size:"), txtSize,
            new Button("Create new", (ClickHandler)event -> createNewWidget())
        ),
        // container for the widget itself
        container,
        horizontalPanel(
            new Button("getResponse()", (ClickHandler)event ->
                Window.alert(reCaptchaWidget.getResponse())),
            new Button("getReCaptchaWidgetId()", (ClickHandler)event ->
                Window.alert(reCaptchaWidget.getReCaptchaWidgetId().toString())),
            new Button("reset()", (ClickHandler)event ->
                reCaptchaWidget.reset()))
        )
    );
  }
  
  private ReCaptchaWidget createNewWidget() {
    ReCaptchaWidgetParams.Builder paramsBuilder = ReCaptchaWidgetParams.builder(txtSiteKey.getText())
        .setCallback(token -> Window.alert("reCAPTCHA token: " + token))
        .setExpiredCallback(() -> Window.alert("reCAPTCHA expired"))
        .setErrorCallback(() -> Window.alert("reCAPTCHA error"));
    String theme = txtTheme.getText();
    if (StringUtils.notBlank(theme))
      paramsBuilder.setTheme(theme);
    String size = txtSize.getText();
    if (StringUtils.notBlank(size))
      paramsBuilder.setSize(size);
    
    container.setWidget(reCaptchaWidget = new ReCaptchaWidget(paramsBuilder.build()));
    return reCaptchaWidget;
  }
}
