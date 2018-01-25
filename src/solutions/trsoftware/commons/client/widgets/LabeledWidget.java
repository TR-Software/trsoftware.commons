package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LabelBase;
import com.google.gwt.user.client.ui.Widget;

/**
 * An {@link InlineFlowPanel} containing a label and a widget.
 *
 * @param <T> the type of the child widget
 * @author Alex
 * @since 12/23/2017
 */
public class LabeledWidget<T extends Widget> extends InlineFlowPanel {
  private LabelBase label;
  private T widget;

  public LabeledWidget(LabelBase label, T widget) {
    add(this.label = label);
    add(this.widget = widget);
  }

  public LabeledWidget(String label, T widget) {
    this(new InlineLabel(label), widget);
  }


}
