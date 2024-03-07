package solutions.trsoftware.commons.client.auth;

import com.google.gwt.user.client.ui.Composite;
import com.google.web.bindery.event.shared.HandlerRegistration;
import solutions.trsoftware.commons.client.widgets.ElementWidget;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Widget that renders a Google Sign Button.
 *
 * @author Alex
 * @since 1/4/2024
 */
public class GoogleSignInButton extends Composite {

  private final GoogleSignInClient gsiClient;
  private final GsiButtonConfiguration buttonConfig;
  private final GoogleSignInEvent.Handler signInHandler;
  private HandlerRegistration handlerRegistration;

  public GoogleSignInButton(@Nonnull GoogleSignInClient gsiClient,
                            @Nonnull GsiButtonConfiguration buttonConfig,
                            @Nullable GoogleSignInEvent.Handler signInHandler) {
    this.gsiClient = requireNonNull(gsiClient, "gsiClient");
    this.buttonConfig = requireNonNull(buttonConfig, "buttonConfig");
    this.signInHandler = signInHandler;
    initWidget(
        /* the button container element */
        new ElementWidget("div"));
    gsiClient.renderButton(getElement(), buttonConfig);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if (handlerRegistration == null && signInHandler != null) {
      handlerRegistration = gsiClient.addSignInHandler(signInHandler);
    }
  }

  @Override
  protected void onUnload() {
    super.onUnload();
    if (handlerRegistration != null) {
      handlerRegistration.removeHandler();
      handlerRegistration = null;
    }
  }

  public GsiButtonConfiguration getButtonConfig() {
    return buttonConfig;
  }
}
