/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.client.event;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import solutions.trsoftware.commons.client.jso.JsDocument;

/**
 * Entry point into the HTML5 "Page Visibility API", which can tell us whether the browser tab/window is currently visible.
 * Defines {@link ChangeEvent}, which can be subscribed to by calling {@link #addVisibilityChangeHandler(ChangeHandler)}
 *
 * Use {@link #isSupported()} to check if the browser supports this API.
 *
 * @see <a href="https://docs.webplatform.org/wiki/dom/Document/visibilitychange">webplatform.org Reference</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Guide/User_experience/Using_the_Page_Visibility_API">Mozilla Reference</a>
 * @see <a href="http://caniuse.com/#feat=pagevisibility">Browser support for this feature</a>
 * @see <a href="http://ie.microsoft.com/testdrive/Performance/PageVisibility/Default.html">Test Page</a>
 *
 *  @author Alex, 3/26/2015
 */
public class PageVisibility {

  // TODO: test this class with different browsers -- might want to implement the additional hacks described in https://developer.mozilla.org/en-US/docs/Web/Guide/User_experience/Using_the_Page_Visibility_API

  /*
   NOTE: we can't use deferred binding based on the "user.agent" property here because page visibility support
   only got added to the more recent versions of Webkit and Firefox.  We could, of course, create a new deferred
   binding property called something like "pageVisibilitySupported", but it's not worth the extra complexity and
   the doubling in the number of permutations just to save a few bytes in the compiled module size.
   */

  private PageVisibility() {  // make this class uninstantiable, since it only provides static methods
  }

  /**
   * @return true if the current browser supports this API
   */
  public static boolean isSupported() {
    return JsDocument.get().hasKey("hidden");
  }

  /**
   * @return false if the page is currently hidden or if the current browser doesn't support this API
   */
  public static boolean isHidden() {
    return JsDocument.get().getBoolean("hidden");
  }

  /**
   * Attaches the given handler to the the {@code document} element, the caller must remember to remove the handler
   * explicitly to avoid leaking memory.
   * @return a memento to be used to remove the handler, or {@code null} if {@link #isSupported()} is {@code false}.
   */
  public static <H extends ChangeHandler> HandlerRegistration addVisibilityChangeHandler(final H handler) {
    if (!isSupported())
      return null;
    return new NativeEvents.ListenerRegistration(Document.get(), ChangeEvent.NAME, false) {
      @Override
      public void onBrowserEvent(Event event) {
        handler.onVisibilityChange((ChangeEvent)event);
      }
    };
  }

  /**
   * Handler interface for {@link ChangeEvent} events.
   */
  public static interface ChangeHandler extends EventHandler {
    void onVisibilityChange(ChangeEvent event);
  }

  /**
   * This event is only fired on the {@code document} element, and therefore it neither captures nor bubbles to
   * any other element on the page (since {@code document} is the root of the DOM tree).
   *
   * Use {@link #isHidden()} to check whether this event is being fired in response to the page
   * being hidden or the opposite.
   */
  public static class ChangeEvent extends Event {
    public static final String NAME = "visibilitychange";

    /**
     * Protected constructor, since JSOs are not directly instantiable.
     */
    protected ChangeEvent() {
    }

    /**
     * Shortcut for {@link PageVisibility#isHidden()}
     */
    public final boolean isHidden() {
      return PageVisibility.isHidden();
    }
  }

}
