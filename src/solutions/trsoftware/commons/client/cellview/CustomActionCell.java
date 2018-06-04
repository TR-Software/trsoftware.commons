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

package solutions.trsoftware.commons.client.cellview;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;
import static com.google.gwt.dom.client.BrowserEvents.KEYDOWN;

/**
 * Provides the same functionality as {@link ActionCell}, but allows rendering custom HTML instead of a button.
 * @author Alex, 10/18/2017
 */
public abstract class CustomActionCell<C> extends AbstractCell<C> {

  protected final ActionCell.Delegate<C> delegate;
  // TODO: replace this class with a more general class that simply takes the html as the constructor arg
  protected final SafeHtml html;

  public CustomActionCell(ActionCell.Delegate<C> delegate) {
    this(delegate, CLICK, KEYDOWN);
  }

  public CustomActionCell(ActionCell.Delegate<C> delegate, String... consumedEvents) {
    super(consumedEvents);
    this.delegate = delegate;
    // wrap the custom HTML with a div that sets its class attribute
    this.html = new SafeHtmlBuilder()
        .appendHtmlConstant("<div class=\"" + CommonsClientBundleFactory.INSTANCE.getCss().CustomActionCell() + "\">")
        .append(generateHtml())
        .appendHtmlConstant("</div>")
        .toSafeHtml();
  }

  /** Subclass should implement this to provide their custom HTML */
  protected abstract SafeHtml generateHtml();

  @Override
  public void onBrowserEvent(Context context, Element parent, C value,
                             NativeEvent event, ValueUpdater<C> valueUpdater) {
    super.onBrowserEvent(context, parent, value, event, valueUpdater);
    if (CLICK.equals(event.getType())) {
      EventTarget eventTarget = event.getEventTarget();
      if (!Element.is(eventTarget)) {
        return;
      }
      if (parent.getFirstChildElement().isOrHasChild(Element.as(eventTarget))) {
        // Ignore clicks that occur outside of the main element.
        onEnterKeyDown(context, parent, value, event, valueUpdater);
      }
    }
  }

  @Override
  public void render(Context context, C value, SafeHtmlBuilder sb) {
    sb.append(html);
  }

  @Override
  protected void onEnterKeyDown(Context context, Element parent, C value,
                                NativeEvent event, ValueUpdater<C> valueUpdater) {
    delegate.execute(value);
  }
}
