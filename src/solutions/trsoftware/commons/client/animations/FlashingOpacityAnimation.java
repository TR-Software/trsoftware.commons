package solutions.trsoftware.commons.client.animations;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.bridge.text.AbstractNumberFormatter;
import solutions.trsoftware.commons.client.bridge.text.NumberFormatter;
import solutions.trsoftware.commons.client.widgets.Widgets;

/**
 * Fades the given element in & out at the given rate.
 *
 * @author Alex
 */
public class FlashingOpacityAnimation extends Animation {

  private final Element element;
  private final double maxOpacity;
  private final double minOpacity;
  private final double opacityDelta;
  private final int totalFlashes;

  /** Used to render values like "opacity: .7" */
  private static final NumberFormatter FRACTION_FORMATTER = AbstractNumberFormatter.getInstance(0, 1, 2, false, false);

  /** Used to render values like "filter: alpha(opacity=70)" */
  private static final NumberFormatter PERCENT_FORMATTER = AbstractNumberFormatter.getInstance(1, 0, 0, false, false);

  public FlashingOpacityAnimation(Element element, double maxOpacity, double minOpacity, int totalFlashes) {
    this.element = element;
    this.maxOpacity = maxOpacity;
    this.minOpacity = minOpacity;
    this.totalFlashes = totalFlashes;
    opacityDelta = maxOpacity - minOpacity;
  }

  /** Called immediately before the animation starts. */
  @Override
  protected void onStart() {
    super.onStart();
    setOpacity(maxOpacity);
  }

  protected void onUpdate(double progress) {
    // we use the cosine function to model an oscillation from 100% to 0 and back to 100%
    double x = Math.PI * progress * totalFlashes;
    double cosX = Math.abs(Math.cos(x));
    double delta = cosX * opacityDelta;
    double opacity = minOpacity + delta;
    setOpacity(opacity);
  }

  /** Overriding this method in case animation terminates abnormally */
  @Override
  protected void onComplete() {
    super.onComplete();
    setOpacity(maxOpacity);
  }

  private void setOpacity(double newOpacity) {
    String fractionString = FRACTION_FORMATTER.format(newOpacity);
    element.getStyle().setProperty("opacity", fractionString);
    element.getStyle().setProperty("filter", "alpha(opacity=" + PERCENT_FORMATTER.format(newOpacity*100) + ")");
    element.getStyle().setProperty("MozOpacity", fractionString);
  }

  // TODO: temp
  public static void insertTestWidget() {
    Label label = new Label("FlashingOpacityAnimation Test Widget");
    final Element element = label.getElement();
    element.getStyle().setBackgroundColor("red");
    final DoubleBox maxOpacity = new DoubleBox();
    maxOpacity.setValue(1.);
    final DoubleBox minOpacity = new DoubleBox();
    minOpacity.setValue(.5);
    final IntegerBox flashCount = new IntegerBox();
    flashCount.setValue(5);
    final IntegerBox durationSeconds = new IntegerBox();
    durationSeconds.setValue(5);
    RootPanel.get().insert(Widgets.flowPanel(
        label,
        minOpacity, maxOpacity, flashCount, durationSeconds,
        new Button("Start FlashingOpacityAnimation", new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            FlashingOpacityAnimation animation =
                new FlashingOpacityAnimation(element, maxOpacity.getValue(), minOpacity.getValue(), flashCount.getValue());
            animation.run(durationSeconds.getValue() * 1000);
          }
        }),
        new Button("Step &raquo;", new ClickHandler() {
          private FlashingOpacityAnimation animation = new FlashingOpacityAnimation(element, maxOpacity.getValue(), minOpacity.getValue(), flashCount.getValue());
          private double progress = 0;
          @Override
          public void onClick(ClickEvent event) {
            animation.onUpdate(progress += .01);
          }
        })
    ), 0);
  }
}