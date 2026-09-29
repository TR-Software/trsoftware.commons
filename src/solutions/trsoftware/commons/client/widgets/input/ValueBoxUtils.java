package solutions.trsoftware.commons.client.widgets.input;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.ValueBox;

import javax.annotation.Nonnull;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

/**
 * Allows binding up/down key handlers to numeric {@link ValueBox} instances, to allow incrementing/decrementing
 * the value using the arrow keys.
 * 
 * @since 9/11/2026
 */
public abstract class ValueBoxUtils {

  /**
   * Adds a {@link KeyDownHandler} to {@code valueBox} for incrementing/decrementing its value using the
   * up/down arrow keys.
   * 
   * @param valueBox an {@link Integer} value box (e.g. {@link IntegerBox})
   * @param delta will be added to the current value for the {@linkplain KeyCodes#KEY_UP up key} is pressed,
   *   or subtracted from the current value when the {@linkplain KeyCodes#KEY_DOWN down key} is pressed
   * @return the added handler registration
   */
  public static <T extends ValueBox<Integer>> HandlerRegistration setIncrement(T valueBox, Integer delta) {
    return valueBox.addKeyDownHandler(new IntegerBoxIncrementHandler(valueBox, delta));
  }

  /**
   * Adds a {@link KeyDownHandler} to {@code valueBox} for incrementing/decrementing its value using the
   * up/down arrow keys.
   * 
   * @param valueBox a {@link Double} value box (e.g. {@link DoubleBox})
   * @param delta will be added to the current value for the {@linkplain KeyCodes#KEY_UP up key} is pressed,
   *   or subtracted from the current value when the {@linkplain KeyCodes#KEY_DOWN down key} is pressed
   * @return the added handler registration
   */
  public static <T extends ValueBox<Double>> HandlerRegistration setIncrement(T valueBox, Double delta) {
    return valueBox.addKeyDownHandler(new DoubleBoxIncrementHandler(valueBox, delta));
  }

  /**
   * Adds a {@link KeyDownHandler} to {@code valueBox} for incrementing/decrementing its value using the
   * up/down arrow keys.
   * 
   * @param valueBox a {@link Long} value box (e.g. {@link LongBox})
   * @param delta will be added to the current value for the {@linkplain KeyCodes#KEY_UP up key} is pressed,
   *   or subtracted from the current value when the {@linkplain KeyCodes#KEY_DOWN down key} is pressed
   * @return the added handler registration
   */
  public static <T extends ValueBox<Long>> HandlerRegistration setIncrement(T valueBox, Long delta) {
    return valueBox.addKeyDownHandler(new LongBoxIncrementHandler(valueBox, delta));
  }


  abstract static class ValueBoxIncrementHandler<N extends Number> implements KeyDownHandler {
    protected final ValueBox<N> valueBox;
    protected final N increment;

    public ValueBoxIncrementHandler(ValueBox<N> valueBox, N increment) {
      this.valueBox = requireNonNull(valueBox, "valueBox");
      this.increment = requireNonNull(increment, "increment");
    }

    @Override
    public void onKeyDown(KeyDownEvent event) {
      // TODO: maybe add an option to predicate on modifier keys, e.g. if (event.isControlKeyDown())
      switch (event.getNativeKeyCode()) {
        case KeyCodes.KEY_UP:
          updateValue(this::increment);
          break;
        case KeyCodes.KEY_DOWN:
          updateValue(this::decrement);
          break;
      }
    }

    private void updateValue(UnaryOperator<N> operator) {
      N value = valueBox.getValue();
      if (value != null)
        valueBox.setValue(operator.apply(value), true);
    }

    /* Note: unable to provide generic implementations of the increment/decrement methods,
       each subclass has to implement the operations for its specific numeric type
       (see https://stackoverflow.com/questions/3873215/can-i-do-arithmetic-operations-on-the-number-baseclass)
       TODO: maybe create generic methods for this in NumberUtils (similar to NumberUtils.minValue / NumberUtils.maxValue)
     */

    /**
     * Adds the {@link #increment} to the specified value
     * @return the incremented value
     */
    protected abstract N increment(@Nonnull N value);

    /**
     * Subtracts the {@link #increment} from the specified value
     * @return the decremented value
     */
    protected abstract N decrement(@Nonnull N value);
  }

  static class IntegerBoxIncrementHandler extends ValueBoxIncrementHandler<Integer> {
    public IntegerBoxIncrementHandler(ValueBox<Integer> valueBox, Integer increment) {
      super(valueBox, increment);
    }
    @Override
    protected Integer increment(@Nonnull Integer value) {
      return value + increment;
    }
    @Override
    protected Integer decrement(@Nonnull Integer value) {
      return value - increment;
    }
  }

  static class DoubleBoxIncrementHandler extends ValueBoxIncrementHandler<Double> {
    public DoubleBoxIncrementHandler(ValueBox<Double> valueBox, Double increment) {
      super(valueBox, increment);
    }
    @Override
    protected Double increment(@Nonnull Double value) {
      return value + increment;
    }
    @Override
    protected Double decrement(@Nonnull Double value) {
      return value - increment;
    }
  }

  static class LongBoxIncrementHandler extends ValueBoxIncrementHandler<Long> {
    public LongBoxIncrementHandler(ValueBox<Long> valueBox, Long increment) {
      super(valueBox, increment);
    }
    @Override
    protected Long increment(@Nonnull Long value) {
      return value + increment;
    }
    @Override
    protected Long decrement(@Nonnull Long value) {
      return value - increment;
    }
  }
}
