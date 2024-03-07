package solutions.trsoftware.commons.client.auth;

import solutions.trsoftware.commons.client.jso.JsObject;

import javax.annotation.Nonnull;
import java.util.LinkedHashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Wrapper for the config object passed to the native {@code google.accounts.id.initialize} function.
 * This data object defines the application's client ID and other global parameters for the GSI client.
 *
 * @author Alex
 * @see <a href="https://developers.google.com/identity/gsi/web/reference/js-reference#IdConfiguration">
 *     <code>IdConfiguration</code> Reference</a>
 * @see <a href="https://developers.google.com/identity/gsi/web/reference/js-reference#google.accounts.id.initialize">
 *     <code>google.accounts.id.initialize</code> Reference</a>
 * @see GoogleSignInClient
 * @since 1/3/2024
 */
public class IdConfiguration extends JsObject {

  protected IdConfiguration() {
  }

  public static IdConfiguration create(String clientId) {
    IdConfiguration config = createObject().cast();
    config.set("client_id", clientId);
    return config;
  }

  // TODO: for now, probably not worth the effort to implement all the specific property setters methods individually; just using the generic setters provided by the JsObject superclass


  /**
   * Package-private so that only {@link GoogleSignInClient} can attach a singleton callback and fire {@link
   * GoogleSignInEvent}
   * to user code.
   *
   * @return this instance, for chaining setters
   * @see <a href="https://developers.google.com/identity/gsi/web/reference/js-reference#callback">
   *     <cdode>IdConfiguration.callback</cdode></a>
   */
  final native IdConfiguration setCallback(Consumer<CredentialResponse> callback) /*-{
    this.callback = $entry(function (credentialResponse) {
      callback.@java.util.function.Consumer::accept(*)(credentialResponse);
    });
    return this;
  }-*/;

  /**
   * Checks whether all properties of this object are strictly equal ({@code ===}) to those of the given object,
   * excluding the {@code "callback"} property (which isn't defined until the object gets passed to
   * the {@link GoogleSignInClient} {@linkplain GoogleSignInClient#GoogleSignInClient(IdConfiguration) constructor}).
   *
   * @return {@code true} if the given object represents the same settings as this object
   */
  public final boolean isEqualTo(IdConfiguration other) {
    if (other == null)
      return false;
    LinkedHashSet<String> keySet1 = getComparableKeys(this);
    LinkedHashSet<String> keySet2 = getComparableKeys(other);
    if (!keySet1.equals(keySet2))
      return false;
    for (String key : keySet1) {
      if (!this.propertyEquals(other, key))
        return false;
    }
    return true;
  }

  @Nonnull
  private LinkedHashSet<String> getComparableKeys(IdConfiguration obj) {
    LinkedHashSet<String> keySet = obj.streamKeys().collect(Collectors.toCollection(LinkedHashSet::new));
    keySet.remove("callback");
    return keySet;
  }

}
