package solutions.trsoftware.commons.client.bundle;

import com.google.gwt.core.client.GWT;

/**
 * @author Alex
 * @since 12/28/2017
 */
public class CommonsClientBundleFactoryImpl extends CommonsClientBundleFactory {

  @Override
  protected CommonsClientBundle createCommonsClientBundle() {
    return GWT.create(CommonsClientBundle.class);
  }
}
