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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.ext.ResourceGeneratorType;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;
import solutions.trsoftware.commons.rebind.bundle.SvgImageResourceGenerator;

/**
 * Convenience subclass of {@link DataResource} for SVG images.
 * <p>
 * The generated MIME type will be {@value SvgImageResourceGenerator#DEFAULT_MIME_TYPE} unless specified explicitly
 * with a {@link MimeType} annotation. Can use a {@link DoNotEmbed} annotation to prevent a resource from being embedded.
 *
 * @author Alex
 * @since 11/2/2021
 * @see SvgImageResourceGenerator
 * @see DataResource
 * @see com.google.gwt.resources.rg.DataResourceGenerator
 */
@ResourceGeneratorType(SvgImageResourceGenerator.class)
public interface SvgImageResource extends DataResource {

  // TODO: unit test this class in order to test SvgImageResourceGenerator

  /**
   * Allows using this {@link ClientBundle} resource with older {@link ImageBundle}-based APIs.
   */
  AbstractImagePrototype asImagePrototype();
}
