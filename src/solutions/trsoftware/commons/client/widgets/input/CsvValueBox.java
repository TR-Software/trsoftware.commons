package solutions.trsoftware.commons.client.widgets.input;

import com.google.gwt.dom.client.Document;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBox;
import solutions.trsoftware.commons.shared.util.text.CsvParser;
import solutions.trsoftware.commons.shared.util.text.CsvRenderer;

import java.util.List;

/**
 * Allows entering a list of values as a comma-separated string.  The values will be parsed using a given
 * {@link CsvParser}.
 *
 * @author Alex
 * @since 12/22/2017
 */
public class CsvValueBox<T> extends ValueBox<List<T>> {

  /**
   * This constructor may be used by subclasses to explicitly use an existing
   * element. This element must be an &lt;input&gt; element whose type is
   * 'text'.
   *
   */
  protected CsvValueBox(Renderer<T> renderer, Parser<T> parser) {
    super(Document.get().createTextInputElement(), new CsvRenderer<T>(renderer), new CsvParser<T>(parser));
  }
}
