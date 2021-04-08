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

package solutions.trsoftware.gcharts.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.user.client.Command;
import solutions.trsoftware.commons.client.util.SchedulerUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton interface for the <a href="https://developers.google.com/chart/">Google Charts</a> API.
 *
 * @author Alex, 5/6/2017
 */
public class GoogleCharts {

  public static final String GOOGLE_CHARTS_JS_URL = "https://www.gstatic.com/charts/loader.js";

  private static GoogleCharts instance;

  public static GoogleCharts get() {
    if (instance == null)
      instance = new GoogleCharts();
    return instance;
  }

  public enum State {
    /**
     * Not ready to use
     */
    LOADING,
    /**
     * Ready to use: the script was injected successfully and the 'corechart' package has been loaded
     */
    LOADED
  }

  /** The current state of this instance */
  private State state;

  /**
   * Private constructor (to be used only by {@link #get()}).
   * <p>
   * Injects the {@value #GOOGLE_CHARTS_JS_URL} script into the page using {@link ScriptInjector} and calls
   * <pre>
   *    google.charts.load("current", {packages: ["corechart"]});
   * </pre>
   *
   * @see <a href="https://developers.google.com/chart/interactive/docs/basic_load_libs">Google Charts docs</a>
   */
  private GoogleCharts() {
    final String googleChartsJsUrl = GOOGLE_CHARTS_JS_URL;
    ScriptInjector.fromUrl(googleChartsJsUrl)
        .setCallback(
            new Callback<Void, Exception>() {
              @Override
              public void onFailure(Exception reason) {
                getLogger().log(Level.SEVERE,
                    "Error injecting '" + googleChartsJsUrl + "' using ScriptInjector", reason);
                reason.printStackTrace();
              }

              @Override
              public void onSuccess(Void result) {
                loadCorePackages();
              }
            }
        )
        .setWindow(ScriptInjector.TOP_WINDOW)  // this is required to enable native methods to access the API using $wnd
        .inject();
  }

  private Logger getLogger() {
    return Logger.getLogger(getClass().getName());
  }

  /**
   * Runs the given command as soon as {@link #isLoaded()} returns {@code true}
   */
  public void runWhenLoaded(final Command command) {
    if (isLoaded())
      command.execute();
    else {
      SchedulerUtils.checkAndWait(this::isLoaded, 100, command);
    }
  }

  /**
   * @return {@code true} iff the API is ready to be used
   * @see State#LOADED
   */
  public boolean isLoaded() {
    return state == State.LOADED;
  }

  private native void loadCorePackages()/*-{
    var instance = this;
    $wnd.google.charts.load('current', {packages: ['corechart']});
    $wnd.google.charts.setOnLoadCallback(function() {
      instance.@solutions.trsoftware.gcharts.client.GoogleCharts::corePackagesLoaded()();
    });
    $wnd.google.charts.setOnLoadCallback();
  }-*/;

  private void corePackagesLoaded() {
    state = State.LOADED;
  }

}
