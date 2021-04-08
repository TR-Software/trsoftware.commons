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

package solutions.trsoftware.commons.client.bundle;

import com.google.gwt.core.client.GWT;

/**
 * Allows overriding the default resources in {@link CommonsClientBundle} (like the default CSS styles for widgets).
 * Inheriting modules can define a subclass and add the following declaration to their module {@code .gwt.xml} file:
 *  <pre>
 *  {@code <replace-with class="my.package.MyCommonsClientBundleFactorySubclass">}
 *    {@code <when-type-is class="solutions.trsoftware.commons.client.bundle.CommonsClientBundleFactory"/>}
 *  {@code </replace-with>}
 * </pre>
 * @see <a href="https://stackoverflow.com/questions/5943895/how-exactly-would-one-override-a-cssresource">How to override CssResource</a>
 * @author Alex
 * @since 12/28/2017
 */
public abstract class CommonsClientBundleFactory {

  public static final CommonsClientBundleFactory INSTANCE = GWT.create(CommonsClientBundleFactory.class);

  private static CommonsClientBundle commonsClientBundle;

  protected abstract CommonsClientBundle createCommonsClientBundle();

  public CommonsClientBundle getCommonsClientBundle() {
    if (commonsClientBundle == null) {
      commonsClientBundle = createCommonsClientBundle();
      commonsClientBundle.css().ensureInjected();
    }
    return commonsClientBundle;
  }

  public CommonsCss getCss() {
    return getCommonsClientBundle().css();
  }
}
