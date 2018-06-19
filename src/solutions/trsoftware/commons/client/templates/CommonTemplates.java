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

package solutions.trsoftware.commons.client.templates;

import com.google.gwt.core.shared.GWT;
import solutions.trsoftware.commons.shared.util.template.Template;
import solutions.trsoftware.commons.shared.util.template.TemplateBundle;

/**
 * @author Alex
 * @since 11/17/2017
 */
public interface CommonTemplates extends TemplateBundle {

  CommonTemplates INSTANCE = GWT.create(CommonTemplates.class);

  Template uncaught_exception_warning();

  /**
   * SVG graphic containing a triangle, which can be styled with CSS.
   */
  @Resource("triangle.svg")
  Template triangle();
}
