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


import com.google.web.bindery.event.shared.HandlerRegistration;
import solutions.trsoftware.commons.shared.util.callables.Function1_;
import solutions.trsoftware.commons.shared.util.callables.Functions;

import java.util.Arrays;

/**
 * Combines several handler registrations into one.
 *
 * @author Alex, 10/6/2015
 */
public class MultiHandlerRegistration implements com.google.gwt.event.shared.HandlerRegistration {

  private final HandlerRegistration[] registrations;

  public MultiHandlerRegistration(HandlerRegistration...registrations) {
    this.registrations = registrations;
  }

  @Override
  public void removeHandler() {
    Functions.tryCall(Arrays.asList(registrations), new Function1_<HandlerRegistration>() {
      @Override
      public void call(HandlerRegistration reg) {
        if (reg != null)
          reg.removeHandler();
      }
    });
  }
}
