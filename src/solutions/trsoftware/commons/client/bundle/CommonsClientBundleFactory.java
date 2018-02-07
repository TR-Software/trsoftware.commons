package solutions.trsoftware.commons.client.bundle;

import com.google.gwt.core.client.GWT;

/**
 * Allows overriding the default resources in {@link CommonsClientBundle} (like the default CSS styles for widgets).
 * Inheriting modules can define a subclass and add the following declaration to their module {@code .gwt.xml} file:
 *  <pre>
 *  {@code <replace-with class="my.package.MyCommonsClientBundleFactorySubclass">}
 *    {@code <when-type-is class="solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory"/>}
 *  {@code </replace-with>}
 * </pre>
 * @see <a href="https://stackoverflow.com/questions/5943895/how-exactly-would-one-override-a-cssresource">How to override CssResource</a>
 * @author Alex
 * @since 12/28/2017
 */
public abstract class CommonsClientBundleFactory {

  public static final CommonsClientBundleFactory INSTANCE = GWT.create(CommonsClientBundleFactory.class);

  private static CommonsClientBundle commonsClientBundle;

  protected abstract CommonsClientBundle createCommonsClientBundle();

  public CommonsClientBundle getCommonsClientBundle() {
    if (commonsClientBundle == null) {
      commonsClientBundle = createCommonsClientBundle();
      commonsClientBundle.css().ensureInjected();
    }
    return commonsClientBundle;
  }

  public CommonsCss getCss() {
    return getCommonsClientBundle().css();
  }
}
