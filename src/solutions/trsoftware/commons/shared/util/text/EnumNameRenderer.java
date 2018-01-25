package solutions.trsoftware.commons.shared.util.text;

import com.google.gwt.text.shared.AbstractRenderer;

/** Simply renders the name of a given enum value */
public class EnumNameRenderer<E extends Enum<E>> extends AbstractRenderer<E> {
  @Override
  public String render(E value) {
    return value.name();
  }
}
