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

package solutions.trsoftware.gcharts.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.ScriptInjector;

/**
 * @author Alex, 5/6/2017
 */
public class GoogleChartsEntryPoint implements EntryPoint {
  // TODO: implement the callback (perhaps set a field in GoogleChartsApiLoader)
  public void onModuleLoad() {
    final String googleChartsJsUrl = "https://www.gstatic.com/charts/loader.js";
    ScriptInjector.fromUrl(googleChartsJsUrl).setCallback(
        new Callback<Void, Exception>() {
          @Override
          public void onFailure(Exception reason) {
            System.err.println("Error loading " + googleChartsJsUrl);
            reason.printStackTrace();
            // TODO: notify GoogleChartsApiLoader?
          }

          @Override
          public void onSuccess(Void result) {
            // TODO: notify GoogleChartsApiLoader?
          }
        }
    ).inject();
  }
}
