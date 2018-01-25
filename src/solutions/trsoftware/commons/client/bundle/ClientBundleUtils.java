package solutions.trsoftware.commons.client.bundle;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * Provides utility methods for working with resources defined in a {@link ClientBundle}
 *
 * @author Alex
 * @since 12/29/2017
 */
public class ClientBundleUtils {

  public static AbstractImagePrototype toImagePrototype(ImageResource imageResource) {
    return AbstractImagePrototype.create(imageResource);
  }

  public static SafeHtml toHTML(ImageResource imageResource) {
    return toImagePrototype(imageResource).getSafeHtml();
  }
}
