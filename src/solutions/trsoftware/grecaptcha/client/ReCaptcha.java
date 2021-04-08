/*
 * Copyright 2021 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.grecaptcha.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Element;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Facade for the <a href="https://developers.google.com/recaptcha/docs/display">reCAPTCHA v2</a> JavaScript API.
 *
 * @author Alex
 * @since 4/19/2020
 * @see ReCaptchaWidget
 */
public class ReCaptcha {
  public static final String RECAPTCHA_SCRIPT_URL = "https://www.google.com/recaptcha/api.js";

  private static final Logger LOG = Logger.getLogger(ReCaptcha.class.getName());

  private static final AtomicBoolean injected = new AtomicBoolean();

  private static final AtomicBoolean loaded = new AtomicBoolean();

  public static void init() {
    if (injected.compareAndSet(false, true)) {
      String scriptUrl = RECAPTCHA_SCRIPT_URL + "?render=explicit";
      // TODO: allow passing custom URL params for constructing the above script URL?  (see https://developers.google.com/recaptcha/docs/display#javascript_resource_apijs_parameters)
      ScriptInjector.fromUrl(scriptUrl)
          .setWindow(ScriptInjector.TOP_WINDOW)
          .setCallback(new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {
              LOG.log(Level.SEVERE, "Error injecting '" + scriptUrl + "' using ScriptInjector", reason);
            }

            @Override
            public void onSuccess(Void result) {
              loaded.set(true);
            }
          })
          .inject();
    }
  }

  /**
   * Renders a new reCAPTCHA widget inside the given container element and returns the ID of the newly created widget.
   * <p>
   * <b>Notes:</b>
   * <ul>
   *   <li>
   *     the ID returned by this method is not an actual DOM element ID, and therefore should be treated
   *     simply as an opaque handle for referencing this widget in other calls to the reCAPTCHA API.
   *   </li>
   *   <li>
   *     Although the actual return type of this method is not specified in the reCAPTCHA API docs,
   *     in practice it appears to always return sequential positive integers (0, 1, 2, ...).
   *   </li>
   * </ul>
   *
   * @param container The DOM element inside which to render the reCAPTCHA widget
   * @param parameters An object containing parameters as key=value pairs,
   *     for example: {@code {"sitekey": "your_site_key", "theme": "light"}};
   *     use {@link ReCaptchaWidgetParams.Builder} to construct this argument
   * @return an internal ID of the newly created widget which can be passed to other grecaptcha API methods to reference
   * this widget; NOTE: this value is not an actual DOM element ID; although the actual return type of this method
   * is not specified in the reCAPTCHA API docs, in practice, the grecaptcha API returns sequential
   * positive integers (0, 1, 2, ...) as the ID.
   *
   * @see <a href="https://developers.google.com/recaptcha/docs/display#javascript_api">reCAPTCHA JavaScript API docs</a>
   * @see ReCaptchaWidgetParams
   * @see #render(String, ReCaptchaWidgetParams)
   */
  public static Object render(Element container, ReCaptchaWidgetParams parameters) {
    return renderNative(container, parameters);
  }

  /**
   * Renders a new reCAPTCHA widget inside the given container element and returns the ID of the newly created widget.
   * <p>
   * <b>Notes:</b>
   * <ul>
   *   <li>
   *     the ID returned by this method is not an actual DOM element ID, and therefore should be treated
   *     simply as an opaque "handle" for referencing this widget in other calls to the reCAPTCHA API.
   *   </li>
   *   <li>
   *     Although the actual return type of this method is not specified in the reCAPTCHA API docs,
   *     in practice it appears to always return sequential positive integers (0, 1, 2, ...).
   *   </li>
   * </ul>
   *
   * @param containerId DOM ID of the element inside which to render the reCAPTCHA widget
   * @param parameters An object containing parameters as key=value pairs,
   *     for example: {@code {"sitekey": "your_site_key", "theme": "light"}};
   *     use {@link ReCaptchaWidgetParams.Builder} to construct this argument
   * @return an internal ID of the newly created widget which can be passed to other grecaptcha API methods to reference
   * this widget; NOTE: this value is not an actual DOM element ID; although the actual return type of this method
   * is not specified in the reCAPTCHA API docs, in practice, the grecaptcha API returns sequential
   * positive integers (0, 1, 2, ...) as the ID.
   *
   * @see <a href="https://developers.google.com/recaptcha/docs/display#javascript_api">reCAPTCHA JavaScript API docs</a>
   * @see ReCaptchaWidgetParams
   * @see #render(Element, ReCaptchaWidgetParams)
   */
  public static Object render(String containerId, ReCaptchaWidgetParams parameters) {
    return renderNative(containerId, parameters);
  }

  private static native int renderNative(Object container, JavaScriptObject parameters) /*-{
    return $wnd.grecaptcha.render(container, parameters);
  }-*/;

  /**
   * Resets the reCAPTCHA widget with the given ID.
   *
   * @param widgetId a widget ID returned by the {@link #render} method
   *
   * @see <a href="https://developers.google.com/recaptcha/docs/display#javascript_api">reCAPTCHA JavaScript API docs</a>
   */
  public static native void reset(Object widgetId) /*-{
    $wnd.grecaptcha.reset(widgetId);
  }-*/;

  /**
   * Resets the first reCAPTCHA widget created in the page.
   * If there is more than one, pass the widget ID to {@link #reset(Object)}.
   *
   * @see <a href="https://developers.google.com/recaptcha/docs/display#javascript_api">reCAPTCHA JavaScript API docs</a>
   */
  public static native void reset() /*-{
    $wnd.grecaptcha.reset();
  }-*/;

  /**
   * Gets the response for the reCAPTCHA widget with the given ID.
   *
   * @param widgetId a widget ID returned by the {@link #render} method
   * @return the response token, or an empty string if not available (i.e. not yet received or expired)
   *
   * @see <a href="https://developers.google.com/recaptcha/docs/display#javascript_api">reCAPTCHA JavaScript API docs</a>
   */
  public static native String getResponse(Object widgetId) /*-{
    return $wnd.grecaptcha.getResponse(widgetId);
  }-*/;

  /**
   * Gets the response for the first reCAPTCHA widget created in the page.
   * If there is more than one, pass the widget ID to {@link #getResponse(Object)}.
   *
   * @return the response token, or an empty string if not available (i.e. not yet received or expired)
   * @see <a href="https://developers.google.com/recaptcha/docs/display#javascript_api">reCAPTCHA JavaScript API docs</a>
   */
  public static native String getResponse() /*-{
    return $wnd.grecaptcha.getResponse();
  }-*/;


  /*
  // NOTE: it might be possible to implement this API using JsInterop (http://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJsInterop.html)
  // instead of JSNI:

  @JsType(isNative = true, name = "grecaptcha", namespace = JsPackage.GLOBAL)
  public static class GRecaptcha {

    public static native Object render(Element container, Object parameters);

    public static native void reset(Object widgetId);

    public static native String getResponse(Object widgetId);
  }

  @JsType
  public static class ReCaptchaParams {
    @JsProperty(name = "sitekey")
    public String siteKey;
    public String theme;
    public String size;
    public int tabindex;
    public ReCaptchaCallback callback;
  }*/


}
