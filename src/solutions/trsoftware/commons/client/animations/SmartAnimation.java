package solutions.trsoftware.commons.client.animations;

import com.google.gwt.animation.client.Animation;

/**
 * This class extends {@link Animation} to expose whether it's running, since the {@link Animation#isRunning} field
 * is private.
 *
 * @author Alex
 */
public abstract class SmartAnimation extends Animation {
  private boolean running;

  @Override
  protected void onStart() {
    super.onStart();
    running = true;
  }

  @Override
  protected void onComplete() {
    super.onComplete();
    running = false;
  }

  @Override
  protected void onCancel() {
    super.onCancel();
    running = false;
  }

  public boolean isRunning() {
    return running;
  }
}
