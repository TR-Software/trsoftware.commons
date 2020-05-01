package solutions.trsoftware.grecaptcha.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import solutions.trsoftware.commons.client.jso.JsObject;

/**
 * Parameters for the {@link ReCaptcha#render grecaptcha.render} method.
 * <p>
 * Use the provided {@linkplain Builder builder} to construct an instance of this class.
 *
 * @see <a href="https://developers.google.com/recaptcha/docs/display#g-recaptcha_tag_attributes_and_grecaptcharender_parameters">
 *   grecaptcha.render parameters</a>
 * @see Builder
 * @see ReCaptcha#render(Element, ReCaptchaWidgetParams)
 * @see ReCaptchaWidget#ReCaptchaWidget(ReCaptchaWidgetParams)
 */
public class ReCaptchaWidgetParams extends JavaScriptObject {

  protected ReCaptchaWidgetParams() {
  }

  public static Builder builder(String siteKey) {
    return new Builder(siteKey);
  }

  /**
   * Builder for the JS object to be used as the {@code parameters} argument to the {@link ReCaptcha#render grecaptcha.render} method.
   */
  public static class Builder {

    private JsObject params = JsObject.create();

    Builder() {
    }

    Builder(String siteKey) {
      setSiteKey(siteKey);
    }

    public Builder setSiteKey(String siteKey) {
      params.set("sitekey", siteKey);
      return this;
    }

    /**
     * Optional. The color theme of the widget. Defaults to "light" if omitted.
     * @param theme "light" (default) or "dark"
     */
    public Builder setTheme(String theme) {
      params.set("theme", theme);
      return this;
    }

    /**
     * Optional. The size of the widget. Defaults to "normal".
     * @param size "normal" (default) or "compact"
     */
    public Builder setSize(String size) {
      params.set("size", size);
      return this;
    }

    /**
     * Optional. The tabindex of the widget and challenge.  Defaults to 0
     * If other elements in your page use tabindex, it should be set to make user navigation easier.
     */
    public Builder setTabIndex(int tabIndex) {
      params.set("tabindex", tabIndex);
      return this;
    }

    /**
     * Optional. The callback function executed when the user submits a successful response.
     * The {@code g-recaptcha-response} token is passed to this callback.
     */
    public Builder setCallback(ReCaptchaCallback callback) {
      params.set("callback", wrapCallback(callback));
      return this;
    }

    /**
     * Optional. The callback function executed when the reCAPTCHA response expires and the user needs to re-verify.
     */
    public Builder setExpiredCallback(Command expiredCallback) {
      params.set("expired-callback", wrapCallback(expiredCallback));
      return this;
    }

    /**
     * Optional. The callback function executed when reCAPTCHA encounters an error (usually network connectivity)
     * and cannot continue until connectivity is restored.
     * If you specify a function here, you are responsible for informing the user that they should retry.
     */
    public Builder setErrorCallback(Command errorCallback) {
      params.set("error-callback", wrapCallback(errorCallback));
      return this;
    }

    private native JavaScriptObject wrapCallback(ReCaptchaCallback callback) /*-{
      return $entry(function (token) {
        callback.@solutions.trsoftware.grecaptcha.client.ReCaptchaCallback::handleResponse(Ljava/lang/String;)(token);
      });
    }-*/;

    private native JavaScriptObject wrapCallback(Command callback) /*-{
      return $entry(function () {
        callback.@com.google.gwt.user.client.Command::execute()();
      });
    }-*/;

    public ReCaptchaWidgetParams build() {
      return params.cast();
    }

  }
}
