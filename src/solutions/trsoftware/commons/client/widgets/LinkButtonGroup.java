package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.util.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Displays several links horizontally.  One of the links may be disabled.
 *
 * Styles:
 * .LinkButtonGroup
 * .LinkButtonGroupHeading
 * .LinkButtonGroupLink
 * .LinkButtonGroupLink-selected
 *
 * @author Alex
 */
public class LinkButtonGroup<T extends Widget & HasText> extends Composite {

  /** Each link will be replaced with a label once it's "activated" */
  Map<T, Label> linkReplacements = new HashMap<T, Label>();

  /** The link that's disabled */
  private T activeLink;

  public LinkButtonGroup(String heading, T... links) {
    FlexTable tblMain = new FlexTable();
    int col = 0;
    if (heading != null) {
      tblMain.setText(0, col, heading);
      tblMain.getCellFormatter().setStyleName(0, col++, "LinkButtonGroupHeading");
    }
    for (T link : links) {
      tblMain.setWidget(0, col++, link);
      link.addStyleName("LinkButtonGroupLink");
      Label lblReplacement = Widgets.label("", "LinkButtonGroupLink");
      lblReplacement.addStyleDependentName("selected");
      lblReplacement.setVisible(false);
      tblMain.setWidget(0, col++, lblReplacement);
      linkReplacements.put(link, lblReplacement);
    }
    if (!ArrayUtils.isEmpty(links))
      setActiveLink(links[0]);
    initWidget(tblMain);
    setStyleName("LinkButtonGroup");
  }

  public T getActiveLink() {
    return activeLink;
  }

  public void setActiveLink(T link) {
    // unhide the current active link
    if (activeLink != null) {
      activeLink.setVisible(true);
      linkReplacements.get(activeLink).setVisible(false);
    }
    // hide the given link
    if (link != null) {
      link.setVisible(false);
      Label lblReplacement = linkReplacements.get(link);
      lblReplacement.setText(link.getText());
      lblReplacement.setVisible(true);
      activeLink = link;
    }
  }
}
