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

package solutions.trsoftware.commons.client.animations;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.HandlerRegistration;
import solutions.trsoftware.commons.client.widgets.LabeledWidget;
import solutions.trsoftware.commons.client.widgets.WidgetDecorator;
import solutions.trsoftware.commons.shared.util.MathUtils;

import static solutions.trsoftware.commons.client.widgets.Widgets.flowPanel;
import static solutions.trsoftware.commons.client.widgets.Widgets.inlineFlowPanel;

/**
 * @author Alex
 * @since 12/18/2017
 */
public abstract class OscillatingAnimation extends SmartAnimation {

  protected final double maxValue;
  protected final double minValue;
  /** Milliseconds to complete one full cycle (changing the value from max to min and back to max) */
  protected double wavelength;
  protected int duration;

  public OscillatingAnimation(double minValue, double maxValue, double wavelength) {
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.wavelength = wavelength;
  }

  public double getMaxValue() {
    return maxValue;
  }

  public double getMinValue() {
    return minValue;
  }

  public double getWavelength() {
    return wavelength;
  }

  // TODO: cont here: test modifying the wavelength (and maybe other fields) while the animation is running
  public void setWavelength(double wavelength) {
    this.wavelength = wavelength;
  }

  /** Called immediately before the animation starts. */
  @Override
  protected void onStart() {
    super.onStart();
    setValue(maxValue);
  }

  @Override
  protected void onUpdate(double progress) {
    super.onUpdate(progress);
    setValue(computeValue(progress));
  }

  /**
   * Computes the value to pass to {@link #setValue(double)} for the given progress percentage.
   * <p>
   *   Since we're modeling an oscillation whose max value occurs at {@code x = 0}, we use the cosine wave function,
   *   scaled and modulated to produce values in the range <nobr><code>[{@link #minValue}, {@link #maxValue}]</code></nobr>,
   *   at the frequency defined by {@link #duration} and {@link #wavelength}.
   * </p>
   * <p>
   *   Overriding this method is not recommended.
   * </p>
   * @param x the progress of the animation, in the range {@code [0,1]}
   * @return a value in the range <code>[{@link #minValue}, {@link #maxValue}]</code>
   * @see <a href="https://www.desmos.com/calculator">Online Graphing Calculator</a> (this was used to tune the cosine parameters)
   */
  protected double computeValue(double x) {
    double waveHeight = (maxValue - minValue) / 2;  // the height of the wave (the part above the x-axis)
    /*
       Explanation:
       * cos(2*N*PI*x) compresses the wave such that it has N oscillations between x = 0 and x = 1
       * multiplying this by waveHeight sets the height of the wave
       * adding minValue and waveHeight adjusts the wave such that its lowest y-value will be minValue
       (this formula was derived by trial & error using https://www.desmos.com/calculator)
    */
    return waveHeight * Math.cos(2 * getN() * Math.PI * x) + minValue + waveHeight;
  }

  /**
   * @return the number of oscillations that we expect to happen during this animation's {@link #duration},
   * based on {@link #wavelength}
   */
  private double getN() {
    return duration / wavelength;
  }

  /** Overriding this method in case animation terminates abnormally */
  @Override
  protected void onComplete() {
    super.onComplete();
    setValue(maxValue);
  }

  @Override
  public void run(int duration, double startTime, Element element) {
    this.duration = duration;
    super.run(duration, startTime, element);
  }

  /**
   * Apply the visual effect based on the given value.
   * @param value a value in the range [{@link #minValue}, {@link #maxValue}], which defines the visual effect for the current point in time
   */
  protected abstract void setValue(double value);

  /**
   * We override this method to simply return the given progress value, because we'll be doing our own interpolation
   * in {@link #computeValue(double)}
   */
  @Override
  protected final double interpolate(double progress) {
    return progress;
  }

  /**
   * A scaffolding widget that can be used to test the animation with various parameters.
   *
   * @param <T> the subclass of {@link OscillatingAnimation}
   */
  public static abstract class Tester<T extends OscillatingAnimation> extends Composite implements UpdateEvent.Handler {
    private static final double STEP = .01;
    FlowPanel targetWidget;
    Element targetElement;
    final DoubleBox minValueInput = new DoubleBox();
    final DoubleBox maxValueInput = new DoubleBox();
    final IntegerBox wavelengthInput = new IntegerBox();
    final IntegerBox durationInput = new IntegerBox();
    T animation;
    FlowPanel pnlControls = new FlowPanel();
    NumberLabel<Double> lblProgress = new NumberLabel<Double>(NumberFormat.getFormat("#.##%"));
    NumberLabel<Double> lblValue = new NumberLabel<Double>(NumberFormat.getFormat("#.###"));
    private HandlerRegistration updateHandlerReg;

    Tester() {
      targetWidget = flowPanel(
          new InlineLabel(getClass().getName()),
          WidgetDecorator.setStyleProperty(
              inlineFlowPanel(
                  new InlineLabel("progress: "), lblProgress,
                  new InlineLabel("; value: "), lblValue
              ),
              "float", "right"));
      lblProgress.setValue(0d);
      targetElement = targetWidget.getElement();
      minValueInput.setValue(.5);
      maxValueInput.setValue(1.0);
      wavelengthInput.setValue(1000);
      durationInput.setValue(5000);
      pnlControls.add(registerInputWidget(minValueInput, "Min"));
      pnlControls.add(registerInputWidget(maxValueInput, "Max"));
      pnlControls.add(registerInputWidget(wavelengthInput, "Period (ms)"));
      pnlControls.add(registerInputWidget(durationInput, "Duration (ms)"));
      pnlControls.add(new Button("Start", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          getAnimation().run(durationInput.getValue());
        }
      }));
      pnlControls.add(new Button("Step &raquo;", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          advance(STEP);
        }
      }));
      pnlControls.add(new Button("&laquo; Step", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          advance(-STEP);
        }
      }));
      pnlControls.add(new Button("Reset", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          refresh();
        }
      }));
      initWidget(flowPanel(targetWidget, pnlControls));
    }

    /**
     * Advances the animation (changes its progress) by the given percentage.
     */
    protected void advance(double delta) {
      T anim = getAnimation();
      double progress = anim.getProgress() + delta;
      anim.onUpdate(MathUtils.restrict(progress, 0, 1));
    }

    public Double getMaxValue() {
      return maxValueInput.getValue();
    }

    public Double getMinValue() {
      return minValueInput.getValue();
    }

    public Integer getWavelength() {
      return wavelengthInput.getValue();
    }

    public Integer getDuration() {
      return durationInput.getValue();
    }

    public T getAnimation() {
      if (animation == null)
        animation = create();
      return animation;
    }

    private T create() {
      T animation = createAnimation();
      animation.duration = durationInput.getValue();
      if (updateHandlerReg != null)
        updateHandlerReg.removeHandler();  // remove handler from the prior instance of the animation
      updateHandlerReg = animation.addUpdateHandler(this);
      lblProgress.setValue(0d);
      return animation;
    }

    /**
     * @return a new instance of the {@link OscillatingAnimation} being tested by the subclass
     */
    protected abstract T createAnimation();

    public void refresh() {
      if (animation != null)
        animation.cancel();
      animation = create();
    }

    /**
     * Adds a {@link ValueChangeHandler} to the given widget in order to re-create the animation
     * (by invoking {@link #refresh()}) when its value changes.
     * @return {@link LabeledWidget} containing the label and the input widget
     */
    protected <V, W extends Widget & HasValue<V>> LabeledWidget<W> registerInputWidget(W inputWidget, String label) {
      inputWidget.addValueChangeHandler(new ValueChangeHandler<V>() {
        @Override
        public void onValueChange(ValueChangeEvent<V> event) {
          refresh();
        }
      });
      if (inputWidget instanceof ValueBox) {
        ValueBox valueBox = (ValueBox)inputWidget;
        valueBox.setVisibleLength(valueBox.getText().length());
      }
      return new LabeledWidget<W>(label + ":", inputWidget);
    }

    @Override
    public void onUpdate(UpdateEvent event) {
      double progress = event.getProgress();
      lblProgress.setValue(progress);
      lblValue.setValue(getAnimation().computeValue(progress));
    }
  }
}
