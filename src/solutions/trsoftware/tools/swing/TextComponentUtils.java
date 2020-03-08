package solutions.trsoftware.tools.swing;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author Alex
 * @since 1/11/2020
 */
public class TextComponentUtils {

  /**
   * Attaches a {@link FocusListener} that calls {@link JTextComponent#selectAll()} on the given
   * component when it {@linkplain FocusListener#focusGained(FocusEvent) gains focus}.
   *
   * @return the given component, for call chaining
   */
  public static JTextComponent selectAllWhenFocused(JTextComponent field) {
    field.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        if (!e.isTemporary()) {
//          field.selectAll();
          SwingUtilities.invokeLater(field::selectAll);
        }
      }
    });
    return field;
  }
}
