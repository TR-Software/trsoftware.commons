package solutions.trsoftware.commons.client.debug;

/**
 * A {@link DebugPanel} that contains an {@link ExceptionSimulator} widget.
 *
 * @author Alex
 * @since 3/6/2018
 */
public class ExceptionSimulatorDebugPanel extends DebugPanel {
  public ExceptionSimulatorDebugPanel() {
    initWidget(new ExceptionSimulator());
  }
}
