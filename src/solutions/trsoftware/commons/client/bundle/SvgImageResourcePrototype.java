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

import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import solutions.trsoftware.commons.rebind.bundle.SvgImageResourceGenerator;

/**
 * Simple implementation of {@link SvgImageResource}
 *
 * @author Alex
 * @since 11/2/2021
 */
public class SvgImageResourcePrototype extends DataResourceImagePrototype implements SvgImageResource {

  private final String name;

  /**
   * Only called by generated code (see {@link SvgImageResourceGenerator})
   */
  public SvgImageResourcePrototype(String name, SafeUri safeUri) {
    super(safeUri);
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getUrl() {
    return getSafeUri().asString();
  }

  @Override
  public AbstractImagePrototype asImagePrototype() {
    return this;
  }

}
