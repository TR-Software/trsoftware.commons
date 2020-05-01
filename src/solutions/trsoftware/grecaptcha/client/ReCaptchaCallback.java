package solutions.trsoftware.grecaptcha.client;

/**
 * Handles a response from a reCAPTCHA widget.
 *
 * @author Alex
 * @since 4/20/2020
 * @see ReCaptchaWidgetParams.Builder#setCallback(ReCaptchaCallback)
 */
public interface ReCaptchaCallback {

  /**
   * Executed when the user submits a successful response.
   *
   * @param token the {@code g-recaptcha-response} token that should be validated using the server-side API.
   */
  void handleResponse(String token);

}
