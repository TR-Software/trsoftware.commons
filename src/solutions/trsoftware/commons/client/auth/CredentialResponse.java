package solutions.trsoftware.commons.client.auth;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * See https://developers.google.com/identity/gsi/web/reference/js-reference#CredentialResponse
 *
 * @author Alex
 * @since 1/3/2024
 */
public class CredentialResponse extends JavaScriptObject {

  protected CredentialResponse() {
  }

  public final native String getCredential() /*-{
    return this.credential;
  }-*/;

  public final native String getSelectBy() /*-{
    return this.select_by;
  }-*/;

  public final JavaScriptObject getCredentialPayload() {
    return parseJwtPayload(getCredential());
  }

  /**
   * Decodes a JWT token (extracts its JSON payload) without performing any security checks
   * (such as validating the signature).
   *
   * @param token
   * @return the JSON payload contained in the given token
   */
  public static native JavaScriptObject parseJwtPayload(String token) /*-{
    // code borrowed from https://stackoverflow.com/a/38552302
    var base64Url = token.split('.')[1];
    var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    var jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function (c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    return JSON.parse(jsonPayload);
  }-*/;

}
