package solutions.trsoftware.commons.client.images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * Provides icons used by this module.
 *
 * @author Alex
 * @since 12/29/2017
 */
public interface CommonsImages extends ClientBundle {

  CommonsImages INSTANCE = GWT.create(CommonsImages.class);

  /*
    NOTE: don't rely on GWT's ClientBundle generator to scale the images
    (e.g. with @ImageResource.ImageOptions(width = 22, height = 30))
    because the result (generated using Java's ImageIO) will probably look much worse than what you can get using a
    real image editor (like GIMP)
   */

  ImageResource user24();
  ImageResource user_arrow24();
  ImageResource user_edit24();
  ImageResource info24();
  ImageResource clipboard24();
  ImageResource warn24();
}
