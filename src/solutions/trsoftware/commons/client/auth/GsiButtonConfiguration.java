package solutions.trsoftware.commons.client.auth;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Command;

/**
 * Wrapper for the options object passed to the {@code google.accounts.id.renderButton} method.
 *
 * @author Alex
 * @since 1/3/2024
 * @see <a href="https://developers.google.com/identity/gsi/web/reference/js-reference#GsiButtonConfiguration">
 *   GsiButtonConfiguration Reference</a>
 * @see <a href="https://developers.google.com/identity/gsi/web/reference/js-reference#google.accounts.id.renderButton">
 *   google.accounts.id.renderButton Reference</a>
 */
public class GsiButtonConfiguration extends JavaScriptObject {

  protected GsiButtonConfiguration() {}

  public static GsiButtonConfiguration create() {
    return createObject().cast();
  }

  public final native GsiButtonConfiguration setType(String type) /*-{
    this.type = type;
    return this;
  }-*/;

  public final native GsiButtonConfiguration setTheme(String theme) /*-{
    this.theme = theme;
    return this;
  }-*/;

  public final native GsiButtonConfiguration setSize(String size) /*-{
    this.size = size;
    return this;
  }-*/;

  public final native GsiButtonConfiguration setText(String text) /*-{
    this.text = text;
    return this;
  }-*/;

  public final native GsiButtonConfiguration setShape(String shape) /*-{
    this.shape = shape;
    return this;
  }-*/;

  public final native GsiButtonConfiguration setLogoAlignment(String logo_alignment) /*-{
    this.logo_alignment = logo_alignment;
    return this;
  }-*/;

  public final native GsiButtonConfiguration setWidth(String width) /*-{
    this.width = width;
    return this;
  }-*/;

  public final native GsiButtonConfiguration setLocale(String locale) /*-{
    this.locale = locale;
    return this;
  }-*/;

  public final native GsiButtonConfiguration setClickListener(Command clickListener) /*-{
    this.click_listener = $entry(function () {
      clickListener.@com.google.gwt.user.client.Command::execute()();
    });
    return this;
  }-*/;

}
