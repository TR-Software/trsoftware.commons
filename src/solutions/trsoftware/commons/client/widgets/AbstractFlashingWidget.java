/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * Can be used to decorate any widget with a flashing effect. Subclasses just need to implement the {@link #setFlashOn(boolean)}
 * to define how to apply and remove the flash effect.
 *
 * @author Alex
 */
public abstract class AbstractFlashingWidget<T extends Widget> extends Composite {

  private final PooledFlashingTimer flashingTimer;
  private boolean flashing = false;

  public AbstractFlashingWidget(T widget, int flashPeriodMs) {
    this(widget, new PooledFlashingTimer(flashPeriodMs));
  }

  /**
   * Use this constructor to synchronize the flashing of this widget with another flashing widget so they both
   * flash at the same time.
   */
  public AbstractFlashingWidget(T widget, PooledFlashingTimer flashingTimer) {
    initWidget(widget);
    this.flashingTimer = flashingTimer;
  }

  public void startFlashing() {
    setFlashing(true);
  }

  public void stopFlashing() {
    setFlashing(false);
  }

  public void setFlashing(boolean flashing) {
    if (flashing && !this.flashing)
      flashingTimer.add(this);
    else if (!flashing && this.flashing) {
      flashingTimer.remove(this);
      setFlashOn(false);
    }
    this.flashing = flashing;
  }

  public boolean isFlashing() {
    return flashing;
  }

  @Override
  protected void onUnload() {
    super.onUnload();
    // avoid memory leaks and make sure the timer will not keep firing when this widget has been removed from the DOM
    stopFlashing();
  }

  /** Applies or removes the flash effect */
  protected abstract void setFlashOn(boolean flashOn);


  /**
   * This timer can be shared among multiple {@link AbstractFlashingWidget} instances so that their flashes
   * can be synchronized.
   */
  public static class PooledFlashingTimer extends Timer {
    /** Whether the current state of the flash is "on" */
    private boolean flashOn = false;
    /** The flashing widgets managed by this timer */
    private List<AbstractFlashingWidget> flashingWidgets = new ArrayList<AbstractFlashingWidget>();
    private int periodMs = 0;

    public PooledFlashingTimer(int periodMs) {
      this.periodMs = periodMs;
    }

    public void run() {
      for (AbstractFlashingWidget widget : flashingWidgets) {
        if (widget.isFlashing())
          widget.setFlashOn(flashOn);
      }
      flashOn = !flashOn;
    }

    public boolean add(AbstractFlashingWidget widget) {
      boolean needsToStart = flashingWidgets.isEmpty();
      boolean ret = flashingWidgets.add(widget);
      if (needsToStart)
        scheduleRepeating(periodMs);
      return ret;
    }

    public boolean remove(AbstractFlashingWidget widget) {
      boolean ret = flashingWidgets.remove(widget);
      if (flashingWidgets.isEmpty())
        cancel();
      return ret;
    }
  }

}
