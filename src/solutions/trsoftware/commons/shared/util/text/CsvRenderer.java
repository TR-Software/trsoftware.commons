package solutions.trsoftware.commons.shared.util.text;

import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Renderer;

import java.util.Iterator;
import java.util.List;

/**
 * Renders a list of values as a comma-separated string. Renders {@code null} and empty lists as {@code ""}.
 *
 * @author Alex
 * @since 12/2/2017
 */
public class CsvRenderer<T> extends AbstractRenderer<List<T>> {

  private Renderer<T> valueRenderer;

  public CsvRenderer(Renderer<T> valueRenderer) {
    this.valueRenderer = valueRenderer;
  }

  public CsvRenderer() {
    this(new StringRenderer<T>());
  }

  @Override
  public String render(List<T> values) {
    if (values == null || values.isEmpty())
      return "";
    StringBuilder out = new StringBuilder();
    for (Iterator<T> it = values.iterator(); it.hasNext(); ) {
      T value = it.next();
      out.append(valueRenderer.render(value));
      if (it.hasNext())
        out.append(',');
    }
    return out.toString();
  }
}
