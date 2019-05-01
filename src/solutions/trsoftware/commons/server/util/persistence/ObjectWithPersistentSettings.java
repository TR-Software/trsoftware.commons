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

package solutions.trsoftware.commons.server.util.persistence;

import solutions.trsoftware.commons.shared.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Convenience base class for making some object state persistent with {@link ObjectToFileMapping}.
 * This can be useful for MBeans, for example.
 *
 * @param <I> interface to be used for accessing the state of the object
 *
 * @author Alex, 9/4/2015
 */
public class ObjectWithPersistentSettings<I> {

  /** Dynamic proxy for settings persistence */
  protected I settings;

  protected PersistentObjectProxy<I> proxyHandler;

  protected PersistentObjectDAO<I> settingsDAO;

  public ObjectWithPersistentSettings(Class<I> settingsInterface, I defaultSettings, boolean persistChanges, File outputDir) {
    settings = defaultSettings;
    // TODO: get rid of the persistChanges arg (since it's always true)?
    if (persistChanges)
      createSettingsProxy(settingsInterface, defaultSettings, new JsonFileDAO<I>(
          new File(outputDir, StringUtils.join(".", getClass().getSimpleName(), settingsInterface.getSimpleName(), "json")),
          (Class<I>)defaultSettings.getClass()
      ));
  }

  public ObjectWithPersistentSettings(Class<I> settingsInterface, I defaultSettings, boolean persistChanges, PersistentObjectDAO<I> settingsDAO) {
    this.settingsDAO = settingsDAO;
    // TODO: get rid of the persistChanges arg (since it's always true)?
    /*if (persistChanges) {
      createSettingsProxy(settingsInterface, defaultSettings, settingsDAO);
    }*/
    proxyHandler = new PersistentObjectProxy<I>(settingsDAO, defaultSettings, settingsInterface);
    settings = proxyHandler.getProxy();
  }

  public void reloadSettings() throws IOException {
    proxyHandler.setTarget(settingsDAO.load());
  }

  private void createSettingsProxy(Class<I> settingsInterface, I defaultSettings, PersistentObjectDAO<I> settingsDAO) {
    settings = PersistentObjectProxy.createProxy(settingsDAO, defaultSettings, settingsInterface);
  }

}
