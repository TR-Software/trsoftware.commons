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

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;

/**
 * @author Alex, 5/6/2017
 */
public class GoogleChartsApiLoader {

  private static GoogleChartsApiLoader instance;

  public static GoogleChartsApiLoader get() {
    if (instance == null)
      instance = new GoogleChartsApiLoader();
    return instance;
  }

  private boolean loaded;

  public void runWhenLoaded(final Command command) {
    Scheduler.get().scheduleIncremental(new Scheduler.RepeatingCommand() {
      @Override
      public boolean execute() {
        if (loaded) {
          command.execute();
          return false;  // stop executing this RepeatingCommand
        }
        return true;  // try again
      }
    });
  }

  private void loaded() {
    loaded = true;
  }

  public void loadChartsApi(String[] packages, Command onLoaded) {
    JsArrayString packagesJsArr = JsArrayString.createArray().cast();
    for (String p : packages) {
      packagesJsArr.push(p);
    }
    loadChartsApi(packagesJsArr);
    if (onLoaded != null)
      runWhenLoaded(onLoaded);
  }

  private native void loadChartsApi(JsArrayString packages)/*-{
    // TODO: figure out how to pass a list of packages to this method
    var instance = this;
    $wnd.google.charts.load('current', {packages: packages});
    $wnd.google.charts.setOnLoadCallback(function() {
      instance.@solutions.trsoftware.gcharts.client.GoogleChartsApiLoader::loaded()();
    });
    $wnd.google.charts.setOnLoadCallback();
  }-*/;

}
