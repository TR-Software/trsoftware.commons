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

package solutions.trsoftware.commons.client;

import com.google.gwt.core.client.EntryPoint;

/**
 * Singleton defining the settings to be used by the various components of the TR Commons library.
 *
 * <p>
 * Inheriting modules should provide their own values in their {@link EntryPoint#onModuleLoad()} method.
 * Example:
 * <pre>
 *     Settings.get()
 *       .setAppName("My App")
 *       .setSupportEmail("support@example.com")
 *       .setStackTraceSizeLimit(32)
 *       .setStackTraceDeobfuscatorServletUrl("/stackTraceServlet");
 * </pre>
 * </p>
 *
 * @author Alex, 10/25/2017
 */
public class Settings {

  private static Settings instance;

  public static Settings get() {
    if (instance == null)
      instance = new Settings();
    return instance;
  }

  private String appName;
  private String supportEmail;
  private int stackTraceSizeLimit = Integer.MAX_VALUE;
  private String stackTraceDeobfuscatorServletUrl;

  private Settings() {
  }

  public String getAppName() {
    return appName;
  }
  
  public Settings setAppName(String appName) {
    this.appName = appName;
    return this;
  }

  public String getSupportEmail() {
    return supportEmail;
  }

  public Settings setSupportEmail(String supportEmail) {
    this.supportEmail = supportEmail;
    return this;
  }

  public int getStackTraceSizeLimit() {
    return stackTraceSizeLimit;
  }

  public Settings setStackTraceSizeLimit(int stackTraceSizeLimit) {
    this.stackTraceSizeLimit = stackTraceSizeLimit;
    return this;
  }

  public String getStackTraceDeobfuscatorServletUrl() {
    return stackTraceDeobfuscatorServletUrl;
  }

  public Settings setStackTraceDeobfuscatorServletUrl(String stackTraceDeobfuscatorServletUrl) {
    this.stackTraceDeobfuscatorServletUrl = stackTraceDeobfuscatorServletUrl;
    return this;
  }
}
