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

package solutions.trsoftware.commons.client.animations;

import com.google.gwt.core.client.Duration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.util.SchedulerUtils;
import solutions.trsoftware.commons.shared.util.Point;
import solutions.trsoftware.commons.shared.util.mutable.MutableBoolean;
import solutions.trsoftware.commons.shared.util.stats.FunctionStats;
import solutions.trsoftware.commons.shared.util.text.SharedNumberFormat;

import java.util.ArrayList;

/**
 * @author Alex, 9/23/2017
 */
public class FlashingOpacityAnimationTest extends CommonsGwtTestCase {
  private final SharedNumberFormat fmt = new SharedNumberFormat(3);

  public void testOnUpdate() throws Exception {
    Label testWidget = new Label("FlashingOpacityAnimation Test Widget");
    RootPanel.get().add(testWidget);
    final AnimationRecorder animation = new AnimationRecorder(testWidget.getElement(), .5, 1, 1000);
    animation.run(5000);
    delayTestFinish(10000);
    SchedulerUtils.checkAndWait(animation.complete, 500, new Command() {
      @Override
      public void execute() {
        animation.printLog();
        FunctionStats<OpacityChange> fs = animation.computeStats();
        // print the inflection points
        System.out.println("Inflections:");
        for (OpacityChange pt : fs.getInflections()) {
          System.out.println("" + (int)pt.timestamp + ": " + fmt.format(pt.opacity));
        }
        assertEquals(5, fs.getMinimums().size());
        assertEquals(1, fs.getMax(), .05);
        assertEquals(.5, fs.getMin(), .05);
        assertEquals(1000, fs.getPeriod(), 50);
        finishTest();
      }
    });
  }

  /** Logs the opacity updates over time */
  private class AnimationRecorder extends OscillatingOpacityAnimation {

    private ArrayList<OpacityChange> log = new ArrayList<OpacityChange>();
    private MutableBoolean complete = new MutableBoolean();
    private double startTime;

    public AnimationRecorder(Element element, double minOpacity, double maxOpacity, double wavelength) {
      super(element, minOpacity, maxOpacity, wavelength);
    }

    @Override
    protected void setValue(double value) {
      super.setValue(value);
      log.add(new OpacityChange(Duration.currentTimeMillis() - startTime, value));

    }

    @Override
    protected void onComplete() {
      super.onComplete();
      complete.set(true);
    }

    @Override
    protected void onStart() {
      complete.set(false);
      startTime = Duration.currentTimeMillis();
      super.onStart();
    }

    void printLog() {
      for (OpacityChange entry : log) {
        int t = (int)entry.timestamp;
        System.out.println("" + t + ": " + fmt.format(entry.opacity));
      }
    }

    FunctionStats<OpacityChange> computeStats() {
      return new FunctionStats<OpacityChange>(log);
    }

  }

  private static class OpacityChange implements Point {
    private double timestamp;
    private double opacity;

    public OpacityChange(double timestamp, double opacity) {
      this.timestamp = timestamp;
      this.opacity = opacity;
    }

    public OpacityChange(double opacity) {
      this(Duration.currentTimeMillis(), opacity);
    }

    @Override
    public double getX() {
      return timestamp;
    }

    @Override
    public double getY() {
      return opacity;
    }
  }

}