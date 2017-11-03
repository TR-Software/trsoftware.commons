package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;


/**
 * @author Alex, 10/23/2014
 */
public class FlashingImage extends AbstractFlashingWidget<Image> {

  private AbstractImagePrototype imgOff;
  private AbstractImagePrototype imgOn;

  public FlashingImage(AbstractImagePrototype imgOff, AbstractImagePrototype imgOn, int flashingDelay) {
    super(imgOff.createImage(), flashingDelay);
    setFaces(imgOff, imgOn);
  }

  /**
   * Use this constructor to synchronize the flashing of this widget with another flashing widget so they both
   * flash at the same time.
   */
  public FlashingImage(AbstractImagePrototype imgOff, AbstractImagePrototype imgOn, PooledFlashingTimer flashingTimer) {
    super(imgOff.createImage(), flashingTimer);
    setFaces(imgOff, imgOn);
  }

  public void setFaces(AbstractImagePrototype imgOff, AbstractImagePrototype imgOn) {
    this.imgOff = imgOff;
    this.imgOn = imgOn;
  }

  @Override
  protected void setFlashOn(boolean flashOn) {
    if (flashOn)
      imgOn.applyTo(getImage());
    else
      imgOff.applyTo(getImage());
  }

  public Image getImage() {
    return (Image)getWidget();
  }
}
