/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.util.Assert;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Progress bar implemented as a {@link SimplePanel} (a {@code div} element) containing an avatar widget.  The avatar is moved
 * from left to right by calling {@link #setProgressPct(double)}, which adjusts the container's {@code padding-left} property
 * accordingly.
 * <p>
 * IMPORTANT: The width of the avatar widget given to {@link #ProgressBar(Widget)} must be less than the width of this
 * container (otherwise it can't move anywhere).  For this reason, the widget should either not be a block-rendered
 * element (such as a {@code div}) or it should have its style set to {@code display: inline-block;} (or {@code display: inline;}, etc.),
 * because block-level elements will always be stretched to fit the entire width of their parent block.
 * </p>
 * @param <W> Type of the avatar widget.
 *
 * @author aepshteyn
 */
public class ProgressBar<W extends Widget> extends SimplePanel {

  private double progressPct;

  public ProgressBar(W avatar) {
    setAvatar(avatar);
    setStyleName("progressBar");
  }

  public W getAvatar() {
    return (W)getWidget();
  }

  public void setAvatar(W avatar) {
    setWidget(avatar);
    refresh();
  }

  public double getProgressPct() {
    return progressPct;
  }

  public void setProgressPct(double percent) {
    // just in case, coerce the arg into the range [0,1]
    percent = min(max(0d, percent), 1d);
    if (progressPct != percent) {
      // only refresh if progress value has changed (previously this was to to avoid avatar flicker, but now that using padding instead of abs. position, probably not necessary, but still, saves cycles)
      progressPct = percent;
      refresh();
    }
  }

  /**
   * Adjusts the avatar position by setting this container's {@code padding-left} style property.
   * IMPORTANT: This method is called automatically every time the {@link #progressPct} changes, but you should also call it if
   * the width of the avatar widget changes.
   */
  public void refresh() {
    int containerWidth = getOffsetWidth();
    // make sure we have a width (otherwise this widget probably hasn't been attached to the DOM, in which case the onLoad method will call refresh)
    if (containerWidth > 0) {
      int avatarWidth = getAvatar().getOffsetWidth();
      Assert.assertTrue(avatarWidth < containerWidth); // probably using a block-rendered element for the avatar; see the doc comment for this class
      int availableSpace = containerWidth - avatarWidth;
      int progressPx = (int)(progressPct * availableSpace)-1;  // NOTE: if we don't subtract 1, the avatar's layout will be messed up in FF (either way, it's safer to have a little less padding than too much)
      getStyleElement().getStyle().setPaddingLeft(progressPx, Style.Unit.PX);
    }
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    refresh();
  }
}
