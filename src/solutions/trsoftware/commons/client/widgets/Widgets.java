/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.event.EventHandlers;
import solutions.trsoftware.commons.client.styles.CellPanelStyle;
import solutions.trsoftware.commons.client.styles.HtmlTableStyle;
import solutions.trsoftware.commons.client.styles.WidgetStyle;
import solutions.trsoftware.commons.shared.util.HtmlUtils;

/** A collections of convenience factory methods for creating widgets */
public class Widgets {

  public static Image image(String url, int width, int height, String style) {
    Image img = new Image(url);
    img.setPixelSize(width, height);
    if (style != null && style.length() > 0)
      img.addStyleName(style);
    return img;
  }

  public static Grid grid(Widget[][] widgets) {
    Grid grid = new Grid(widgets.length, widgets[0].length);
    // add the widgets
    for (int i = 0; i < widgets.length; i++) {
      for (int j = 0; j < widgets[i].length; j++) {
        Widget widget = widgets[i][j];
        if (widget != null)
          grid.setWidget(i, j, widget);
      }
    }
    return grid;
  }

  public static FlexTable flexTable(HtmlTableStyle style) {
    FlexTable flexTable = new FlexTable();
    style.apply(flexTable);
    return flexTable;
  }

  public static FlexTable flexTable(HtmlTableStyle style, Widget[][] widgets) {
    FlexTable flexTable = flexTable((Object[][])widgets);
    style.apply(flexTable);
    return flexTable;
  }

  public static FlexTable flexTable(Widget[][] widgets) {
    return flexTable((Object[][])widgets);
  }

  /**
   * Takes a square array of either Widget or String subclasses, and calls
   * setWidget or setText on the table for each one
   * @param cells
   * @return
   */
  public static FlexTable flexTable(Object[][] cells) {
    FlexTable table = new FlexTable();
    // add the widgets
    for (int i = 0; i < cells.length; i++) {
      for (int j = 0; j < cells[i].length; j++) {
        Object item = cells[i][j];
        if (item != null) {
          if (item instanceof Widget)
            table.setWidget(i, j, (Widget)item);
          else if (item instanceof String)
            table.setText(i, j, (String)item);
        }
      }
    }
    return table;
  }

  public static void setTableCellTooltip(HTMLTable table, int row, int col, String tooltip) {
    Element cellElt = table.getCellFormatter().getElement(row, col);
    cellElt.setTitle(tooltip);
    cellElt.getStyle().setCursor(Style.Cursor.HELP);
  }

  public static Grid grid(HtmlTableStyle style, Widget[][] widgets) {
    Grid grid = grid(widgets);
    style.apply(grid);
    return grid;
  }

  public static Grid grid(String[][] cellTexts) {
    Grid grid = new Grid(cellTexts.length, cellTexts[0].length);
    // add the cellTexts
    for (int i = 0; i < cellTexts.length; i++) {
      for (int j = 0; j < cellTexts[i].length; j++) {
        grid.setText(i, j, cellTexts[i][j]);
      }
    }
    return grid;
  }

  public static Grid grid(HtmlTableStyle style, String[][] cellTexts) {
    Grid grid = grid(cellTexts);
    style.apply(grid);
    return grid;
  }

  public static DisclosurePanel disclosurePanel(String headerText, String style, boolean startOpened, Widget content) {
    DisclosurePanel dp = disclosurePanel(headerText, startOpened, content);
    dp.addStyleName(style);
    return dp;
  }

  public static DisclosurePanel disclosurePanel(String headerText, boolean startOpened, Widget content) {
    DisclosurePanel dp = new DisclosurePanel(headerText);
    dp.setOpen(startOpened);
    dp.setContent(content);
    return dp;
  }

  public static DisclosurePanel disclosurePanel(final AbstractImagePrototype openImage,
                                                final AbstractImagePrototype closeImage,
                                                String headerText, boolean startOpened,
                                                Widget content) {
    DisclosurePanel dp = new DisclosurePanel(new DisclosurePanelImages() {
      public AbstractImagePrototype disclosurePanelOpen() {
        return openImage;
      }
      public AbstractImagePrototype disclosurePanelClosed() {
        return closeImage;
      }
    }, headerText, startOpened);
    dp.setContent(content);
    return dp;
  }

  public static DisclosurePanel disclosurePanel(Widget header, boolean startOpened, Widget content) {
    DisclosurePanel dp = new DisclosurePanel(header, startOpened);
    dp.setContent(content);
    return dp;
  }

  public static TextBox textBox(int visibleChars, int maxChars) {
    TextBox txt = new TextBox();
    txt.setVisibleLength(visibleChars);
    txt.setMaxLength(maxChars);
    return txt;
  }

  public static TextBox textBox(String text) {
    TextBox txt = new TextBox();
    txt.setText(text);
    return txt;
  }

  public static TextBox textBox(String text, String styleName) {
    TextBox txt = textBox(text);
    maybeSetStyleName(txt, styleName);
    return txt;
  }

  public static TextBox textBox(int visibleChars, int maxChars, String text, String styleName) {
    TextBox txt = textBox(visibleChars, maxChars);
    txt.setText(text);
    maybeSetStyleName(txt, styleName);
    return txt;
  }

  public static TextBox textBox(int visibleChars, int maxChars, String text, String styleName, Command onEnterKey, Command onEscapeKey) {
    TextBox txt = textBox(visibleChars, maxChars, text, styleName);
    EventHandlers.addEnterAndEscapeKeyHandlers(txt, onEnterKey, onEscapeKey);
    return txt;
  }

  public static PasswordTextBox passwordBox(Integer visibleChars, Integer maxChars) {
    PasswordTextBox txt = new PasswordTextBox();
    if (visibleChars != null)
      txt.setVisibleLength(visibleChars);
    if (maxChars != null)
      txt.setMaxLength(maxChars);
    return txt;
  }

  public static PasswordTextBox passwordBox(Integer visibleChars, Integer maxChars, String styleName) {
    PasswordTextBox txt = passwordBox(visibleChars, maxChars);
    maybeSetStyleName(txt, styleName);
    return txt;
  }

  public static PasswordTextBox passwordBox(Integer visibleChars, Integer maxChars, String styleName, Command onEnterKey, Command onEscapeKey) {
    PasswordTextBox txt = passwordBox(visibleChars, maxChars, styleName);
    EventHandlers.addEnterAndEscapeKeyHandlers(txt, onEnterKey, onEscapeKey);
    return txt;
  }

  public static CheckBox checkBox(String label, boolean checked) {
    return checkBox(label, false, checked);
  }

  public static CheckBox checkBox(String label, boolean asHTML, boolean checked) {
    CheckBox chk = new CheckBox(label, asHTML);
    chk.setChecked(checked);
    return chk;
  }

  /**
   * Creates a simple panel initially containing the given widget
   */
  public static SimplePanel simplePanel(Widget widget) {
    SimplePanel sp = new SimplePanel();
    sp.setWidget(widget);
    return sp;
  }

  public static Label label(String text, String styleName) {
    return maybeSetStyleName(new Label(text), styleName);
  }

  public static Label label(String text, String styleName, boolean wordWrap) {
    return maybeSetStyleName(new Label(text, wordWrap), styleName);
  }

  private static <W extends Widget> W maybeSetStyleName(W w, String styleName) {
    if (styleName != null && !styleName.equals(""))
      w.setStyleName(styleName);
    return w;
  }

  public static HTML html(String html, String styleName) {
    return html(html, styleName, true);
  }

  public static HTML html(String html, String styleName, boolean wordWrap) {
    HTML h = new HTML(html, wordWrap);
    maybeSetStyleName(h, styleName);
    return h;
  }

  public static SimplePanel simplePanel(WidgetStyle style, Widget widget) {
    SimplePanel sp = simplePanel(widget);
    style.apply(sp);
    return sp;
  }

  public static VerticalPanel verticalPanel(Widget... widgets) {
    return initPanel(new VerticalPanel(), null, widgets);
  }

  public static VerticalPanel verticalPanel(CellPanelStyle style, Widget... widgets) {
    return initPanel(new VerticalPanel(), style, widgets);
  }

  public static HorizontalPanel horizontalPanel(Widget... widgets) {
    return initPanel(new HorizontalPanel(), null, widgets);
  }

  public static HorizontalPanel horizontalPanel(CellPanelStyle style, Widget... widgets) {
    return initPanel(new HorizontalPanel(), style, widgets);
  }

  public static FlowPanel flowPanel(Widget... widgets) {
    return initPanel(new FlowPanel(), null, widgets);
  }

  public static FlowPanel flowPanel(WidgetStyle style, Widget... widgets) {
    return initPanel(new FlowPanel(), style, widgets);
  }

  public static InlineFlowPanel inlineFlowPanel(Widget... widgets) {
    return initPanel(new InlineFlowPanel(), null, widgets);
  }

  public static InlineFlowPanel inlineFlowPanel(WidgetStyle style, Widget... widgets) {
    return initPanel(new InlineFlowPanel(), style, widgets);
  }
  
  public static DeckPanel deckPanel(Widget... widgets) {
    return deckPanel(null, widgets);
  }

  public static DeckPanel deckPanel(WidgetStyle style, Widget... widgets) {
    DeckPanel deckPanel = initPanel(new DeckPanel(), style, widgets);
    // DeckPanel doesn't show any widgets by default, but we show the first widget for convenience
    if (widgets != null && widgets.length > 0)
      deckPanel.showWidget(0);
    return deckPanel;
  }

  /**
   * Adds all the given widgets to the given panel.
   * @return the given panel, to allow chaining
   */
  public static <T extends Widget & HasWidgets> T addAll(T panel, Widget... children) {
    for (Widget child : children)
      if (child != null)
        addChild(panel, child);
    return panel;
  }

  private static <T extends Widget & HasWidgets> void addChild(T panel, Widget child) {
    if (child instanceof CellPanelEntry)
      ((CellPanelEntry)child).addTo((CellPanel)panel);
    else
      panel.add(child);
  }

  /**
   * Adds all the given widgets to the given panel.
   * @return the given cellPanel, to allow chaining
   */
  public static <T extends Widget & HasWidgets> T addAll(T panel, Iterable<? extends Widget> children) {
    for (Widget child : children)
      addChild(panel, child);
    return panel;
  }

  private static <T extends Widget & HasWidgets> T initPanel(T panel, WidgetStyle style, Widget... children) {
    addAll(panel, children);
    if (style != null)
      style.apply(panel);
    return panel;
  }

  public static ScrollPanel scrollPanel(WidgetStyle style, Widget child) {
    ScrollPanel sp = new ScrollPanel(child);
    style.apply(sp);
    return sp;
  }

  public static TabPanel tabPanel(Widget[] widgets, String[] captions) {
    TabPanel tp = new TabPanel();
    for (int i = 0; i < widgets.length; i++) {
      tp.add(widgets[i], captions[i]);
    }
    return tp;
  }

  public static TabPanel tabPanel(IsWidget[] widgets, String[] captions) {
    TabPanel tp = new TabPanel();
    for (int i = 0; i < widgets.length; i++) {
      tp.add(widgets[i], captions[i]);
    }
    return tp;
  }

  public static TabPanel tabPanel(Widget[] widgets, String[] captions, int selectedTabIndex) {
    TabPanel tp = tabPanel(widgets, captions);
    tp.selectTab(selectedTabIndex);
    return tp;
  }

  public static TabPanel tabPanel(WidgetStyle style, Widget[] widgets, String[] captions, int selectedTabIndex) {
    TabPanel tp = tabPanel(widgets, captions, selectedTabIndex);
    style.apply(tp);
    return tp;
  }

  /**
   * @param items An array of 2-item string arrays representing name-value
   * pairs for the entries in the combo box.
   */
  public static ListBox comboBox(String styleName, String[][] items) {
    ListBox lb = new ListBox();
    lb.setVisibleItemCount(1);
    for (String[] item : items) {
      lb.addItem(item[0], item[1]);
    }
    maybeSetStyleName(lb, styleName);
    return lb;
  }

  /** Returns a new HTML("&nbsp") */
  public static HTML spacer() {
    return new InlineHTML(HtmlUtils.nbsp);
  }

  public static InlineLabel inlineLabel(String txt, String styleName) {
    return maybeSetStyleName(new InlineLabel(txt), styleName);
  }
  
  public static InlineLabel inlineLabel(String txt, String styleName, boolean wordWrap) {
    InlineLabel lbl = inlineLabel(txt, styleName);
    lbl.setWordWrap(wordWrap);
    return lbl;
  }

  public static InlineHTML inlineHTML(String txt, String styleName) {
    return maybeSetStyleName(new InlineHTML(txt), styleName);
  }

  public static InlineHTML inlineHTML(String txt, WidgetStyle style) {
    return style.apply(new InlineHTML(txt));
  }

  public static InlineHTML inlineHTML(String txt, String styleName, boolean wordWrap) {
    InlineHTML lbl = inlineHTML(txt, styleName);
    lbl.setWordWrap(wordWrap);
    return lbl;
  }

  /** A hyperlink with a click handler */
  public static Anchor anchor(String text, ClickHandler clickHandler) {
    Anchor anchor = new Anchor(text);
    anchor.addClickHandler(clickHandler);
    return anchor;
  }

  /** A hyperlink with a click handler and help (title) text */
  public static Anchor anchor(String text, String title, ClickHandler clickHandler) {
    Anchor anchor = anchor(text, clickHandler);
    anchor.setTitle(title);
    return anchor;
  }

  /** A hyperlink rendered as HTML with a click handler */
  public static Anchor anchorHTML(String text, ClickHandler clickHandler) {
    Anchor anchor = new Anchor(text, true);
    anchor.addClickHandler(clickHandler);
    return anchor;
  }


  /** A hyperlink with text rendered as HTML, with a click handler and help (title) text */
  public static Anchor anchorHTML(String text, String title, ClickHandler clickHandler) {
    Anchor anchor = anchorHTML(text, clickHandler);
    anchor.setTitle(title);
    return anchor;
  }
}
