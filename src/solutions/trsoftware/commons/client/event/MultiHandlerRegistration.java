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

package solutions.trsoftware.commons.client.event;


import com.google.common.annotations.VisibleForTesting;
import com.google.web.bindery.event.shared.HandlerRegistration;
import solutions.trsoftware.commons.shared.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Combines several handler registrations into one.
 *
 * @author Alex, 10/6/2015
 */
public class MultiHandlerRegistration implements HandlerRegistration {

  private final List<HandlerRegistration> handlerRegistrations = new ArrayList<>();

  public MultiHandlerRegistration(HandlerRegistration... registrations) {
    for (HandlerRegistration reg : registrations) {
      if (reg != null)
        handlerRegistrations.add(reg);
    }
  }

  /**
   * Add another handler reg.
   * @return a reference to this object, for call chaining
   */
  public MultiHandlerRegistration addHandlerRegistration(HandlerRegistration handlerRegistration) {
    if (handlerRegistration != null) {
      handlerRegistrations.add(handlerRegistration);
    }
    return this;
  }

  @VisibleForTesting
  public List<HandlerRegistration> getHandlerRegistrations() {
    return handlerRegistrations;
  }

  @Override
  public void removeHandler() {
    CollectionUtils.safeForEach(handlerRegistrations, reg -> {
      if (reg != null)
        reg.removeHandler();
    });
  }

  public com.google.gwt.event.shared.HandlerRegistration asLegacyGwtRegistration() {
    return new com.google.gwt.event.shared.HandlerRegistration() {
      @Override
      public void removeHandler() {
        MultiHandlerRegistration.this.removeHandler();
      }
    };
  }
}
