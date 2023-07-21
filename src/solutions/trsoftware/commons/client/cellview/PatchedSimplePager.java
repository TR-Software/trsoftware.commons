package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.user.cellview.client.AbstractPager;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.Range;

/**
 * Fixes a bug in {@link AbstractPager#getPageStart()} which messes up the last page of the table.
 * @see <a href="https://stackoverflow.com/questions/6057141/simplepager-row-count-is-working-incorrectly">StackOverflow question</a>
 * @author Alex, 10/4/2017
 */
public class PatchedSimplePager extends SimplePager {

  @Override
  public void setPageStart(int index) {
    HasRows display = getDisplay();
    if (display != null) {
      Range range = display.getVisibleRange();
      int pageSize = range.getLength();
      // NOTE: the patch is to simply remove the following if-stmt:
//          if (!isRangeLimited() && getDisplay().isRowCountExact()) {
//            index = Math.min(index, getDisplay().getRowCount() - pageSize);
//          }
      index = Math.max(0, index);
      if (index != range.getStart()) {
        display.setVisibleRange(index, pageSize);
      }
    }
  }

  @Override
  public ComplexPanel getWidget() {
    // the SimplePager widget is a HorizontalPanel, but we'll assume it's any ComplexPanel, just to be safe from future changes
    return (ComplexPanel)super.getWidget();
  }
}
