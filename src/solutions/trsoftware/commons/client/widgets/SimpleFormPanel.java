package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * GWT's FormPanel widgets submits as an AJAX request.  This widget, on
 * the other hand, creates a plain HTML form.
 *
 * Dec 21, 2009
 *
 * @author Alex
 */
public class SimpleFormPanel extends SimplePanel {

  /** Creates an empty panel that uses a FORM for its contents. */
  public SimpleFormPanel() {
    super(Document.get().createFormElement());
  }

  public FormElement getFormElement() {
    return FormElement.as(getElement());
  }
}
