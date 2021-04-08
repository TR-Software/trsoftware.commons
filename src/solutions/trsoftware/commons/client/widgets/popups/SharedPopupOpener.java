/*
 * Copyright 2021 TR Software Inc.
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

package solutions.trsoftware.commons.client.widgets.popups;

import com.google.common.collect.LinkedHashMultimap;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.ui.Widget;
import solutions.trsoftware.commons.client.util.geometry.Alignment;
import solutions.trsoftware.commons.client.util.geometry.RelativePosition;
import solutions.trsoftware.commons.shared.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Convenience class to save code when multiple instances of {@link PopupOpener} should open the same instance of
 * {@link P}.  Provides a static cache of {@link P} instances by key, so that {@link #createPopup()} is only
 * invoked when there is no corresponding entry in the static cache.
 *
 * @author Alex, 2/17/2016
 */
public abstract class SharedPopupOpener<W extends Widget, P extends EnhancedPopup, K> extends PopupOpener<W, P> {

  /**
   * A static cache of popup instances by key, such that {@link #createPopup()} is only
   * invoked when there is no corresponding entry in this static cache.
   */
  private static final Map<Object, EnhancedPopup> popupCache = new HashMap<Object, EnhancedPopup>();

  private static final LinkedHashMultimap<Object, SharedPopupOpener> attachedOpeners = LinkedHashMultimap.create();

  /** This opener's key for {@link #popupCache} and {@link #attachedOpeners} */
  protected final K key;

  {
    setReusePopup(true);  // setting this to false never makes sense for instances of this class
  }

  protected SharedPopupOpener(K cacheKey, W opener, RelativePosition position) {
    super(opener, position);
    this.key = cacheKey;
    init();
  }

  /**
   * Same as {@link PopupOpener#PopupOpener(Widget, int, RelativePosition)}.
   */
  protected SharedPopupOpener(K cacheKey, W opener, int eventBits, RelativePosition position) {
    super(opener, eventBits, position);
    this.key = cacheKey;
    init();
  }

  /**
   * Same as {@link PopupOpener#PopupOpener(Widget, int, Alignment...)}.
   */
  protected SharedPopupOpener(K cacheKey, W opener, int eventBits, Alignment... alignmentPrefs) {
    super(opener, eventBits, alignmentPrefs);
    this.key = cacheKey;
    init();
  }

  // TODO: move this to a canonical constructor
  private void init() {
    getWidget().addAttachHandler(this);
    if (getWidget().isAttached())
      attachedOpeners.put(key, this);
  }

  @Override
  public PopupOpener<W, P> setReusePopup(boolean enable) {
    if (!enable)
      throw new UnsupportedOperationException();  // disabling this feature never makes sense for instances of this class
    return super.setReusePopup(true);
  }

  @Override
  public PopupOpener<W, P> setHideOnDetach(boolean enable) {
    if (enable)
      throw new UnsupportedOperationException();  // enabling this feature never makes sense for instances of this class (because multiple instances might be showing the same popup (TODO: could instead hide it if all Openers are detached)
    return super.setHideOnDetach(false);
  }

  @Override
  public void showPopup() {
    // we override this method to ensure all instances for the same key will use same popup instance in #isPopupShowing
    getOrCreatePopup();
    super.showPopup();
  }

  @Override
  public P getOrCreatePopup() {
    if (popup == null) {
      P cachedPopup = (P)popupCache.get(key);
      if (cachedPopup != null)
        popup = cachedPopup;  // use the cached instance
      else {
        // create a new instance and cache it
        popup = createPopup();
        if (popup == null)
          throw new NullPointerException("SharedPopupOpener.createPopup should never return null.");
        popupCache.put(key, popup);
      }
    }
    return popup;
  }

  @Override
  public void onAttachOrDetach(AttachEvent event) {
    super.onAttachOrDetach(event);
    if (event.isAttached()) {
      attachedOpeners.put(key, this);
    }
    else {
      Set<SharedPopupOpener> siblings = attachedOpeners.get(key);
      Assert.assertTrue(siblings.remove(this));
      if (siblings.isEmpty()) {
        EnhancedPopup cachedPopup = popupCache.get(key);
        if (cachedPopup != null && !cachedPopup.isShowing())
          popupCache.remove(key);  // no more referents to this popup instance, can safely remove it from the cache to free up some memory
      }
      // clear the popup reference (so that if this opener is ever reattached, it will have to obtain a new instance by calling getOrCreatePopup
      popup = null;
    }
  }
}
