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

package solutions.trsoftware.commons.client.util;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.user.client.Command;
import solutions.trsoftware.commons.shared.util.CollectionUtils;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds on top of {@link ScriptInjector} to provide a clean API for lazy-loading and consuming native JavaScript
 * libraries.
 * <p>
 * To execute native code that depends on an external library having been successfully loaded, just instantiate
 * this class and call {@link #runWhenLoaded(Command)} instead of using {@link ScriptInjector}.
 * This will inject the script into the page and execute the given command as soon as script is loaded.
 * <p>
 * Should make sure that you don't accidentally use more than one instance for any particular script and that you
 * aren't also using {@link ScriptInjector} somewhere else, as that will cause the same script
 * to be loaded more than once.  A good practice might be to create a separate subclass for each script and
 * use it as a singleton.
 *
 * @author Alex
 * @since 5/18/2022
 */
public class ScriptLoader {

  /*
   TODO:
    - unit test & document this class
    - rewrite
   TODO: idea: could have a global flyweight of injected scripts
  */


  /**
   * The script URL
   */
  private final String url;


  public enum State {
    /**
     * Waiting for the script to load (the script tag was injected but not ready yet)
     */
    LOADING,
    /**
     * Ready to use: the script was loaded successfully
     */
    LOADED,
    /**
     * The script loading failed
     */
    FAILED
  }

  /** The current state of this script */
  private State state;

  /**
   * Any pending tasks that need this script; they will be executed after the script has loaded.
   * @see #runWhenLoaded(Command)
   */
  private LinkedList<Command> waitingCommands;

  public ScriptLoader(String url) {
    this.url = url;
  }

  public boolean runWhenLoaded(final Command command) {
    injectIfNeeded();
    switch (state) {
      case LOADED:
        command.execute(); // execute right away
        return true;
      case LOADING:
        // enqueue until loaded
        enqueueCommand(command);
        return true;
      case FAILED:
        return false; // will not execute the command
        // TODO: provide a retry mechanism?
    }
    return false;
  }

  public State getState() {
    return state;
  }

  public String getUrl() {
    return url;
  }

  /**
   * @return {@code true} iff the script is ready to be used
   * @see State#LOADED
   */
  public boolean isLoaded() {
    return state == State.LOADED;
  }

  private Logger getLogger() {
    return Logger.getLogger(getClass().getName());
  }

  private void injectIfNeeded() {
    if (state == null) {
      ScriptInjector.fromUrl(url)
          .setWindow(ScriptInjector.TOP_WINDOW)  // TODO: allow changing this
          .setCallback(new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {
              state = State.FAILED;
              getLogger().log(Level.SEVERE, "Failed to load '" + url + "' using ScriptInjector", reason);
            }

            @Override
            public void onSuccess(Void result) {
              state = State.LOADED;
              onLoaded();
            }
          })
          .inject();
      state = State.LOADING;
    }
  }

  private void enqueueCommand(Command command) {
    if (waitingCommands == null)
      waitingCommands = new LinkedList<>();
    waitingCommands.add(command);
  }

  private void onLoaded() {
    // execute all the waiting commands
    if (waitingCommands != null) {
      CollectionUtils.tryForEach(waitingCommands, Command::execute);
      waitingCommands = null;
    }
  }

}
