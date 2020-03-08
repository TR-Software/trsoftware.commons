package solutions.trsoftware.tools.swing;

import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/**
 * Base class providing utility methods for a swing GUI.
 *
 * @author Alex, 1/1/14
 */
public class BaseGUIFrame extends JFrame {

  public BaseGUIFrame(int width, int height) throws HeadlessException {
    this();
    setSize(width, height);
  }

  public BaseGUIFrame(String title) throws HeadlessException {
    super();
    setTitle(title);
  }

  public BaseGUIFrame() throws HeadlessException {
    super();
    setTitle(getClass().getSimpleName());
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  /** Facilitates launching a gui instance derived from this class */
  public static void start(final Class<? extends BaseGUIFrame> guiClass) {
    start(ReflectionUtils.newInstanceSupplier(guiClass));
  }

  /** Facilitates launching a gui instance derived from this class */
  public static void start(final Supplier<? extends BaseGUIFrame> instanceFactory) {
    // Must use invokeLater to avert a potential threading issue ( see http://java.sun.com/developer/technicalArticles/javase/swingworker/ )
    SwingUtilities.invokeLater(() -> {
      BaseGUIFrame gui = instanceFactory.get();
      gui.setVisible(true);
    });
  }

  protected void showErrorDialog(String title, String message) {
    JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Adds all of the given components (i.e. widgets) to the given container (such as a {@link JPanel}).
   *
   * This allows constructing a panel along with its child widgets in a single statement.
   *
   * @param container a container widget such as {@link JPanel}
   * @param components the child widgets to be added to the container
   * @param <C> the specific type of the container
   * @return the same container that was passed in (for call chaining)
   */
  public static <C extends Container> C addAll(C container, Component... components) {
    for (Component component : components) {
      container.add(component);
    }
    return container;
  }

}
