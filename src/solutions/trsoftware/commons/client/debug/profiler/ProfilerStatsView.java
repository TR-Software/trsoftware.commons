package solutions.trsoftware.commons.client.debug.profiler;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import solutions.trsoftware.commons.client.cellview.PatchedSimplePager;
import solutions.trsoftware.commons.client.images.CommonsImages;
import solutions.trsoftware.commons.client.widgets.ImageButton;

import static solutions.trsoftware.commons.client.widgets.Widgets.disclosurePanel;
import static solutions.trsoftware.commons.client.widgets.Widgets.flowPanel;

/**
 * @author Alex
 * @since 5/8/2023
 */
public class ProfilerStatsView extends Composite {
  private static final int PAGE_SIZE = 10;

  private final ProfilerStatsTable table;
  private final DisclosurePanel disclosurePanel;


  public ProfilerStatsView() {
//    disclosurePanel = new DisclosurePanel("Profiler Stats");
    table = new ProfilerStatsTable();
    table.setPageSize(PAGE_SIZE);
    PatchedSimplePager pager = new PatchedSimplePager();
    pager.setDisplay(table);
    pager.getWidget().add(new ImageButton(CommonsImages.INSTANCE.reloadIconSingleArrow().createImage(),
        clickEvent -> refresh()));

    initWidget(disclosurePanel = disclosurePanel("Profiler Stats",
        flowPanel(table, pager))
    );

    disclosurePanel.addOpenHandler(event -> refresh());
  }

  public void refresh() {
    table.refresh();
  }
}
