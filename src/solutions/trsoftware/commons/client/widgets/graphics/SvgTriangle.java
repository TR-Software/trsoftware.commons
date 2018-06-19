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

package solutions.trsoftware.commons.client.widgets.graphics;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineHTML;
import solutions.trsoftware.commons.client.templates.CommonTemplates;
import solutions.trsoftware.commons.shared.util.template.Template;

/**
 * Wrapper for {@link CommonTemplates#triangle()}
 *
 * @author Alex
 * @since 6/18/2018
 */
public class SvgTriangle extends Composite {

  public static final String DEFAULT_POLYGON_STYLE = "triangle";

  public SvgTriangle() {
    this(DEFAULT_POLYGON_STYLE);
  }

  /**
   * @param polygonStyle the CSS class name for the inner {@code <polygon>} element defining the triangle.
   */
  public SvgTriangle(String polygonStyle) {
    Template svgTemplate = CommonTemplates.INSTANCE.triangle();
    String html = svgTemplate.render("POLYGON_CLASS", polygonStyle);
    initWidget(new InlineHTML(html));
    setStyleName("SvgTriangle");
  }
}
