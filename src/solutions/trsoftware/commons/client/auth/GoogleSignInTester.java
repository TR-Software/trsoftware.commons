package solutions.trsoftware.commons.client.auth;

import com.google.common.base.Strings;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.debug.DebugPanel;
import solutions.trsoftware.commons.client.images.CommonsImages;
import solutions.trsoftware.commons.client.jso.JsConsole;
import solutions.trsoftware.commons.client.jso.JsMixedArray;
import solutions.trsoftware.commons.client.widgets.popups.PopupDialog;

import static solutions.trsoftware.commons.client.widgets.Widgets.flowPanel;

/**
 * A widget that can be used to test the various config options for a Google Sign In button.
 *
 * @see DebugPanel
 * @author Alex
 * @since 1/4/2024
 */
public class GoogleSignInTester extends Composite {


  // defaults for idConfig & buttonConfig used to pre-populate the text areas with JSON values
  private IdConfiguration idConfig = IdConfiguration.create("1002655364388-lq828f67sol8up8raich6hco8k255ou8.apps.googleusercontent.com");
  private GsiButtonConfiguration buttonConfig = GsiButtonConfiguration.create()
      .setTheme("outline")
      .setSize("large");

  private GoogleSignInClient gsiClient;
  private GoogleSignInButton gsiButton;

  private final TextArea txtIdConfig;
  private final TextArea txtButtonConfig;
  private final SimplePanel gsiButtonContainer;
  private final FlowPanel pnlMain;

  public GoogleSignInTester() {
    txtIdConfig = new TextArea();
    txtIdConfig.setVisibleLines(4);
    txtIdConfig.setWidth("800px");
    txtIdConfig.setText(JsonUtils.stringify(idConfig, "  "));

    txtButtonConfig = new TextArea();
    txtButtonConfig.setVisibleLines(4);
    txtButtonConfig.setWidth("400px");
    txtButtonConfig.setText(JsonUtils.stringify(buttonConfig, "  "));

    gsiButtonContainer = new SimplePanel();
    pnlMain = flowPanel(
        new Label("IdConfiguration:"),
        txtIdConfig,
        new Label("GsiButtonConfiguration:"),
        txtButtonConfig,
        flowPanel(new Button("renderButton", (ClickHandler)event -> renderButton())),
        gsiButtonContainer
    );
    initWidget(pnlMain);
  }

  private void renderButton() {
    idConfig = JsonUtils.safeEval(txtIdConfig.getText());
    buttonConfig = JsonUtils.safeEval(txtButtonConfig.getText());
    GoogleSignInClient.getInstance(idConfig, new Callback<GoogleSignInClient, Exception>() {
      @Override
      public void onSuccess(GoogleSignInClient result) {
        gsiClient = result;
        gsiButtonContainer.setWidget(gsiButton = new GoogleSignInButton(gsiClient, buttonConfig, event -> {
          CredentialResponse response = event.getCredentialResponse();
          JsConsole.get().logVarArgs(JsConsole.Level.INFO, JsMixedArray.create()
              .add("GSI response:")
              .add(response)
              .add("JWT:")
              .add(response.getCredentialPayload())
            );
        }
        ));
      }

      @Override
      public void onFailure(Exception reason) {
        gsiButtonContainer.setWidget(new Label(
            Strings.lenientFormat("Failed to load %: %s", GoogleSignInClient.class.getSimpleName(), reason)));
      }
    });
  }
  
  public static class Popup extends PopupDialog {
    private static Popup instance;
    private final GoogleSignInTester googleSignInTester;

    public Popup() {
      super(false, AbstractImagePrototype.create(CommonsImages.INSTANCE.user_arrow24()),
          "GSI Tester", null);
      googleSignInTester = new GoogleSignInTester();
//      googleSignInTester.setWidth("80vw");
//      googleSignInTester.setHeight("80vh");

      setBodyWidget(googleSignInTester);
      setGlassEnabled(true);
    }

    public static Popup getInstance() {
      if (instance == null)
        instance = new Popup();
      return instance;
    }
  }


}
