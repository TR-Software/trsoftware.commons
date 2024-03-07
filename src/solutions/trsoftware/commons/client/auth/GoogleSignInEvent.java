package solutions.trsoftware.commons.client.auth;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;

/**
 * Fired by {@link GoogleSignInClient} when user completes the Google account authentication precess
 * via the One Tap prompt or the popup-window.
 *
 * @see GoogleSignInClient#addSignInHandler(Handler)
 * @author Alex
 * @since 1/4/2024
 */
public class GoogleSignInEvent extends Event<GoogleSignInEvent.Handler> {

  public static final Type<Handler> TYPE = new Type<>();

  private final CredentialResponse credentialResponse;

  public GoogleSignInEvent(CredentialResponse credentialResponse) {
    this.credentialResponse = credentialResponse;
  }

  public CredentialResponse getCredentialResponse() {
    return credentialResponse;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onGoogleSignIn(this);
  }

  public interface Handler extends EventHandler {
    void onGoogleSignIn(GoogleSignInEvent event);
  }
}
