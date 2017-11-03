package solutions.trsoftware.typinglog.client.replay.icons;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 * @author Alex, 9/17/2014
 */
public interface PlayerIconsBundle extends ImageBundle {

  public AbstractImagePrototype play();
  public AbstractImagePrototype pause();

  public AbstractImagePrototype step_back();
  public AbstractImagePrototype step_forward();
  
  public AbstractImagePrototype ff();
  public AbstractImagePrototype ff_full();
  
  public AbstractImagePrototype rew();
  public AbstractImagePrototype rew_full();
}
