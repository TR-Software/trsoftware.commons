package solutions.trsoftware.commons.shared.util.text;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Uses {@link Object#toString()} to render values.  Renders {@code null} values as {@code ""}.
 *
 * @author Alex
 * @since 12/2/2017
 */
public class StringRenderer<T> extends AbstractRenderer<T> {

  @Override
  public String render(T object) {
    return object != null ? object.toString() : "";
  }
}
