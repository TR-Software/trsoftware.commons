package solutions.trsoftware.grecaptcha.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Container for a reCAPTCHA widget that will be rendered using the
 * <a href="https://developers.google.com/recaptcha/docs/display#javascript_api">reCAPTCHA JavaScript API</a>.
 *
 * @see ReCaptcha
 */
public class ReCaptchaWidget extends Composite {

  private SimplePanel container = new SimplePanel();
  private Object reCaptchaWidgetId;


  public ReCaptchaWidget(ReCaptchaWidgetParams params) {
    initWidget(container);
    reCaptchaWidgetId = ReCaptcha.render(getElement(), params);
  }

  /**
   *
   * @return an internal ID of the newly created widget which can be passed to other grecaptcha API methods to reference
   * this widget; NOTE: this value is not an actual DOM element ID; although the actual return type of this method
   * is not specified in the reCAPTCHA API docs, in practice, the grecaptcha API returns sequential
   * positive integers (1, 2, 3, ...) as the ID.
   */
  public Object getReCaptchaWidgetId() {
    return reCaptchaWidgetId;
  }

  /**
   * Gets the response for the encapsulated reCAPTCHA widget.
   *
   * @return the response token, or an empty string if not available (i.e. not yet received or expired)
   * @see ReCaptcha#getResponse()
   */
  public String getResponse() {
    return ReCaptcha.getResponse(reCaptchaWidgetId);
  }

  /**
   * Resets the encapsulated reCAPTCHA widget.
   *
   * @see ReCaptcha#reset()
   */
  public void reset() {
    ReCaptcha.reset(reCaptchaWidgetId);
  }


}
