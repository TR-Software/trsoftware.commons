/*
 * Copyright 2022 TR Software Inc.
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
 */

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.event.EventHandlers;
import solutions.trsoftware.commons.client.styles.CellPanelStyle;
import solutions.trsoftware.commons.client.styles.HtmlTableStyle;
import solutions.trsoftware.commons.client.styles.WidgetStyle;
import solutions.trsoftware.commons.shared.util.HtmlUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    DisclosurePanel dp = disclosurePanel(headerText, content);
    dp.setOpen(startOpened);
    return dp;
  }

  @Nonnull
  public static DisclosurePanel disclosurePanel(String headerText, Widget content) {
    DisclosurePanel dp = new DisclosurePanel(headerText);
    dp.setContent(content);
    return dp;
  }

  /**
   * @deprecated {@link DisclosurePanel#DisclosurePanel(DisclosurePanelImages, String, boolean)} is deprecated
   */
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

  public static DisclosurePanel disclosurePanel(ImageResource openImage,
                                                ImageResource closeImage,
                                                String headerText, boolean startOpened,
                                                Widget content) {
    DisclosurePanel dp = new DisclosurePanel(openImage, closeImage, headerText);
    dp.setOpen(startOpened);
    dp.setContent(content);
    return dp;
  }

  public static DisclosurePanel disclosurePanel(Widget header, boolean startOpened, Widget content) {
    DisclosurePanel dp = new DisclosurePanel(header, startOpened);
    dp.setContent(content);
    return dp;
  }

  public static TextBox textBox(int visibleLength) {
    TextBox txt = new TextBox();
    txt.setVisibleLength(visibleLength);
    return txt;
  }

  public static TextBox textBox(int visibleLength, int maxLength) {
    TextBox txt = textBox(visibleLength);
    txt.setMaxLength(maxLength);
    return txt;
  }

  public static TextBox textBox(String text) {
    TextBox txt = new TextBox();
    txt.setText(text);
    return txt;
  }

  public static TextBox textBox(String text, @Nullable String styleName) {
    TextBox txt = textBox(text);
    applyStyleName(txt, styleName);
    return txt;
  }

  public static TextBox textBox(int visibleLength, int maxLength, String text, @Nullable String styleName) {
    TextBox txt = textBox(visibleLength, maxLength);
    txt.setText(text);
    applyStyleName(txt, styleName);
    return txt;
  }

  public static TextBox textBox(int visibleLength, int maxLength, String text, @Nullable String styleName, @Nullable Command onEnterKey, @Nullable Command onEscapeKey) {
    TextBox txt = textBox(visibleLength, maxLength, text, styleName);
    EventHandlers.addEnterAndEscapeKeyHandlers(txt, onEnterKey, onEscapeKey);
    return txt;
  }

  public static TextArea textArea(int visibleLines) {
    TextArea txt = new TextArea();
    txt.setVisibleLines(visibleLines);
    return txt;
  }

  public static TextArea textArea(int visibleLines, int characterWidth) {
    TextArea txt = textArea(visibleLines);
    txt.setCharacterWidth(characterWidth);
    return txt;
  }
  
  // TODO(1/21/2025): why do the passwordBox methods take Integer rather than int?

  public static PasswordTextBox passwordBox(@Nullable Integer visibleLength, @Nullable Integer maxLength) {
    PasswordTextBox txt = new PasswordTextBox();
    if (visibleLength != null)
      txt.setVisibleLength(visibleLength);
    if (maxLength != null)
      txt.setMaxLength(maxLength);
    return txt;
  }

  public static PasswordTextBox passwordBox(@Nullable Integer visibleLength, @Nullable Integer maxLength, String styleName) {
    PasswordTextBox txt = passwordBox(visibleLength, maxLength);
    applyStyleName(txt, styleName);
    return txt;
  }

  public static PasswordTextBox passwordBox(@Nullable Integer visibleLength, @Nullable Integer maxLength, String styleName,
                                            Command onEnterKey, Command onEscapeKey) {
    PasswordTextBox txt = passwordBox(visibleLength, maxLength, styleName);
    EventHandlers.addEnterAndEscapeKeyHandlers(txt, onEnterKey, onEscapeKey);
    return txt;
  }

  public static CheckBox checkBox(String label, boolean checked) {
    return checkBox(label, false, checked);
  }

  public static CheckBox checkBox(String label, boolean asHTML, boolean checked) {
    CheckBox chk = new CheckBox(label, asHTML);
    chk.setValue(checked);
    return chk;
  }

  /**
   * Creates a simple panel initially containing the given widget
   */
  public static SimplePanel simplePanel(Widget widget) {
    return new SimplePanel(widget);
  }

  /**
   * Creates an empty label without a style name
   * (i.e. without the default {@code "gwt-Label"} style name imposed by its {@linkplain Label#Label() constructor})
   */
  public static Label label() {
    return clearStyleName(new Label());
  }

  /**
   * Creates a label containing the given text, without a style name
   * (i.e. without the default {@code "gwt-Label"} style name imposed by its {@linkplain Label#Label() constructor})
   */
  public static Label label(String text) {
    return clearStyleName(new Label(text));
  }

  public static Label label(String text, String styleName) {
    return applyStyleName(new Label(text), styleName);
  }

  public static Label label(String text, String styleName, boolean wordWrap) {
    return applyStyleName(new Label(text, wordWrap), styleName);
  }

  /**
   * Sets the widget's {@linkplain Widget#setStyleName(String) style name} and returns the widget (for chaining).
   */
  public static <W extends Widget> W applyStyleName(W w, String styleName) {
    /* Update(11/18/2024): now allows null/empty styleName to facilitate removal of default GWT styles (e.g. gwt-Label)
       Originally had: `if (StringUtils.notEmpty(styleName))`
     */
    w.setStyleName(styleName);
    return w;
  }

  /**
   * Facilitates removing the default style names imposed by various GWT widget constructors
   * (e.g. {@code gwt-Label}, {@code gwt-InlineLabel}, {@code gwt-Anchor}, etc.)
   *
   * @return the given widget after invoking {@code widget}.{@link Widget#setStyleName(String) setStyleName}{@code ("")}
   */
  public static <W extends Widget> W clearStyleName(W widget) {
    widget.setStyleName("");
    return widget;
  }

  /**
   * Creates an empty {@link HTML} without the default {@code "gwt-HTML"} style name imposed by its
   * {@linkplain HTML#HTML() constructor}
   */
  public static HTML HTML() {
    return clearStyleName(new HTML());
  }
  
  /**
   * Creates an {@link HTML} with empty style name instead of {@code "gwt-HTML"}.
   */
  public static HTML html(String html) {
    return html(html, "");
  }

  public static HTML html(String html, String styleName) {
    return applyStyleName(new HTML(html), styleName);
  }

  public static HTML html(String html, String styleName, boolean wordWrap) {
    return applyStyleName(new HTML(html, wordWrap), styleName);
  }

  public static SimplePanel simplePanel(WidgetStyle style, Widget widget) {
    return style.apply(new SimplePanel(widget));
  }

  public static SimplePanel simplePanel(String styleName, Widget widget) {
    return applyStyleName(new SimplePanel(widget), styleName);
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

  public static FlowPanel flowPanel(Iterable<? extends Widget> widgets) {
    return initPanel(new FlowPanel(), null, widgets);
  }

  public static FlowPanel flowPanel(WidgetStyle style, Iterable<? extends Widget> widgets) {
    return initPanel(new FlowPanel(), style, widgets);
  }

  public static FlowPanel flowPanel(String styleName, Widget... widgets) {
    return initPanel(new FlowPanel(), new WidgetStyle(styleName), widgets);
  }

  public static FlowPanel flowPanel(String styleName, Iterable<? extends Widget> widgets) {
    return initPanel(new FlowPanel(), new WidgetStyle(styleName), widgets);
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

  private static <T extends Widget & HasWidgets> T initPanel(T panel, WidgetStyle style, Iterable<? extends Widget> children) {
    if (children instanceof Widget) {
      /*
       In case the iterable is a single widget (like a Panel, which implements Iterable<Widget>),
       we want to add just the widget itself, not its children.
       Otherwise, for example, if that panel is empty, nothing would be added.
      */
      addChild(panel, (Widget)children);
    }
    else
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

  public static ScrollPanel scrollPanel(String styleName, Widget child) {
    return applyStyleName(new ScrollPanel(child), styleName);
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
    applyStyleName(lb, styleName);
    return lb;
  }

  /** Returns a new HTML("&nbsp") */
  public static HTML spacer() {
    return new InlineHTML(HtmlUtils.nbsp);
  }

  /**
   * Creates an empty {@link InlineLabel} without a style name (instead of {@code "gwt-InlineLabel"}).
   */
  public static InlineLabel inlineLabel() {
    return clearStyleName(new InlineLabel());
  }

  /**
   * Creates an {@link InlineLabel} without a style name (instead of {@code "gwt-InlineLabel"}).
   */
  public static InlineLabel inlineLabel(String txt) {
    return inlineLabel(txt, "");
  }

  public static InlineLabel inlineLabel(String txt, String styleName) {
    return applyStyleName(new InlineLabel(txt), styleName);
  }
  
  public static InlineLabel inlineLabel(String txt, String styleName, boolean wordWrap) {
    InlineLabel lbl = inlineLabel(txt, styleName);
    lbl.setWordWrap(wordWrap);
    return lbl;
  }

  /**
   * Creates an empty {@link InlineHTML} without a style name (instead of {@code "gwt-InlineHTML"}).
   */
  public static InlineHTML inlineHTML() {
    return clearStyleName(new InlineHTML());
  }

  /**
   * Creates an {@link InlineHTML} without a style name (instead of {@code "gwt-InlineHTML"}).
   */
  public static InlineHTML inlineHTML(String txt) {
    return inlineHTML(txt, "");
  }

  public static InlineHTML inlineHTML(String txt, String styleName) {
    return applyStyleName(new InlineHTML(txt), styleName);
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

  /** A hyperlink with a click handler that invokes the given command */
  public static Anchor anchor(String text, Command onClick) {
    return anchor(text, click -> onClick.execute());
  }

  /** A hyperlink with a click handler and help (title) text */
  public static Anchor anchor(String text, String title, ClickHandler clickHandler) {
    Anchor anchor = anchor(text, clickHandler);
    anchor.setTitle(title);
    return anchor;
  }

  /** A hyperlink with a click handler, title, and style name */
  public static Anchor anchor(String text, String title, String styleName, ClickHandler clickHandler) {
    return applyStyleName(anchor(text, title, clickHandler), styleName);
  }

  /** A hyperlink rendered as HTML with a click handler */
  public static Anchor anchorHTML(String html, ClickHandler clickHandler) {
    Anchor anchor = new Anchor(html, true);
    anchor.addClickHandler(clickHandler);
    return anchor;
  }

  /** A hyperlink rendered as HTML with a click handler that invokes the given command */
  public static Anchor anchorHTML(String html, Command onClick) {
    return anchorHTML(html, click -> onClick.execute());
  }

  /** A hyperlink with text rendered as HTML, with a click handler and help (title) text */
  public static Anchor anchorHTML(String html, String title, ClickHandler clickHandler) {
    Anchor anchor = anchorHTML(html, clickHandler);
    anchor.setTitle(title);
    return anchor;
  }

  /** A hyperlink with text rendered as HTML, with a click handler, title text, and style name */
  public static Anchor anchorHTML(String html, String title, String styleName, ClickHandler clickHandler) {
    return applyStyleName(
        anchorHTML(html, title, clickHandler), styleName);
  }
}
