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
import com.google.gwt.resources.client.ClientBundle;

/**
 * @author Alex
 * @since 12/27/2017
 */
public interface CommonsClientBundle extends ClientBundle {

  CommonsClientBundleFactory FACTORY = GWT.create(CommonsClientBundleFactory.class);
  CommonsClientBundle INSTANCE = FACTORY.getCommonsClientBundle();

  @Source("Commons.gss")
  CommonsCss css();
}
