package solutions.trsoftware.commons.client.widgets.input;

import com.google.gwt.dom.client.Document;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.ValueBox;
import solutions.trsoftware.commons.shared.util.text.AbstractRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Allows inputting a collection of string values using a single text input field.
 * The collection is rendered and parsed using a string delimiter passed to the constructor.
 *
 * @author Alex
 * @since 8/6/2024
 */
public class StringCollectionTextBox extends ValueBox<Collection<String>> {

  // TODO: can extract superclass that extends ValueBox<Collection<T>> and takes an element parser and renderer, to return a typed collection

  /**
   * @param delim delimiter for joining a collection to render it in the text field
   */
  public StringCollectionTextBox(String delim) {
    this(delim, ArrayList::new);
  }

  /**
   * @param delim delimiter for joining a collection to render it in the text field
   * @param supplier creates a new collection of the desired type to be returned from {@link #getValue()}
   */
  public StringCollectionTextBox(String delim, Supplier<Collection<String>> supplier) {
    super(Document.get().createTextInputElement(),
        (AbstractRenderer<Collection<String>>)collection -> String.join(delim, collection),
        text -> Arrays.stream(text.toString().split(RegExp.quote(delim)))
            .collect(Collectors.toCollection(supplier)));
  }
}
