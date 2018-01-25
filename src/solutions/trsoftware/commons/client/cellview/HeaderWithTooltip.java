package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.cellview.client.Header;
import solutions.trsoftware.commons.shared.util.HtmlElementBuilder;

// TODO: move this class to TypeRacer Commons

/**
 * @author Alex, 10/16/2017
 */
public class HeaderWithTooltip extends Header<String> {

  private String text;

  /**
   * Construct a new TextHeader with tooltip.
   *
   * @param text the header text as a String
   */
  public HeaderWithTooltip(String text, String tooltip) {
    super(new CellWithTooltip(tooltip));
    this.text = text;
  }

  @Override
  public String getValue() {
    return text;
  }

  public static class CellWithTooltip extends TextCell {
    private String tooltip;
    private HtmlElementBuilder htmlBuilder;

    public CellWithTooltip(String tooltip) {
      super();
      this.tooltip = tooltip;
      initHtmlBuilder();
    }

    public CellWithTooltip(String tooltip, SafeHtmlRenderer<String> renderer) {
      super(renderer);
      this.tooltip = tooltip;
      initHtmlBuilder();
    }

    public void initHtmlBuilder() {
      this.htmlBuilder = new HtmlElementBuilder("span")
          .setAttribute("title", tooltip)
          .setAttribute("style", "cursor: help;");
    }

    @Override
    public void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
      if (value != null) {
        sb.appendHtmlConstant(htmlBuilder.openTag());
        sb.append(value);
        sb.appendHtmlConstant(htmlBuilder.closeTag());
      }
    }

  }
}
