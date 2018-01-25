package solutions.trsoftware.commons.client.widgets.input;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.view.client.ProvidesKey;
import solutions.trsoftware.commons.shared.util.text.StringRenderer;

import java.util.Collection;
import java.util.EnumSet;

/**
 * @author Alex
 * @since 12/22/2017
 */
public class EnumValueListBox<E extends Enum<E>> extends ValueListBox<E> {

  /**
   * An empty {@link ValueListBox} using the default {@link StringRenderer}.
   * Should call {@link #setAcceptableValues(Collection)} or {@link #addValue(Object)} to add the selectable options.
   */
  public EnumValueListBox() {
    this(new StringRenderer<E>());
  }

  /**
   * An empty {@link ValueListBox} with the given {@link Renderer} for displaying values.
   * Should call {@link #setAcceptableValues(Collection)} or {@link #addValue(Object)} to add the selectable options.
   */
  public EnumValueListBox(Renderer<E> renderer) {
    super(renderer);
  }

  /**
   * An empty {@link ValueListBox} with the given {@link Renderer} for displaying values.
   * Should call {@link #setAcceptableValues(Collection)} or {@link #addValue(Object)} to add the selectable options.
   */
  public EnumValueListBox(Renderer<E> renderer, ProvidesKey<E> keyProvider) {
    super(renderer, keyProvider);
  }

  /**
   * {@link ValueListBox} containing all of the enum constants in the given enum class.
   * @param elementType the enum class
   * @param renderer a custom renderer for the enum values
   */
  public EnumValueListBox(Class<E> elementType, Renderer<E> renderer) {
    this(EnumSet.allOf(elementType), renderer);
  }

  /**
   * {@link ValueListBox} containing the given set of enum constants,
   * to be rendered using the given renderer.
   * @param values the initial set of values to be displayed
   * @param renderer a custom renderer for the enum values
     */
  public EnumValueListBox(EnumSet<E> values, Renderer<E> renderer) {
    super(renderer);
    setAcceptableValues(values);
  }

  /**
   * {@link ValueListBox} containing all of the enum constants in the given enum class,
   * to be rendered using the default {@link StringRenderer}
   * @param elementType the enum class
   */
  public EnumValueListBox(Class<E> elementType) {
    this(EnumSet.allOf(elementType), new StringRenderer<E>());
  }

  /**
   * {@link ValueListBox} containing the given set of enum constants,
   * to be rendered using the default {@link StringRenderer}.
   * @param values the initial set of values to be displayed
     */
  public EnumValueListBox(EnumSet<E> values) {
    this(values, new StringRenderer<E>());
  }

}
