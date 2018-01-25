package solutions.trsoftware.commons.client.bundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

/**
 * @author Alex
 * @since 12/27/2017
 */
public interface CommonsClientBundle extends ClientBundle {

  CommonsClientBundleFactory FACTORY = GWT.create(CommonsClientBundleFactory.class);
  CommonsClientBundle INSTANCE = FACTORY.getCommonsClientBundle();

  @Source("Commons.css")
  CommonsCss css();
}
