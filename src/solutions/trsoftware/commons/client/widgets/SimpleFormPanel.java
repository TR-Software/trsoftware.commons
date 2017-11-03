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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * GWT's FormPanel widgets submits as an AJAX request.  This widget, on
 * the other hand, creates a plain HTML form.
 *
 * Dec 21, 2009
 *
 * @author Alex
 */
public class SimpleFormPanel extends SimplePanel {

  /** Creates an empty panel that uses a FORM for its contents. */
  public SimpleFormPanel() {
    super(Document.get().createFormElement());
  }

  public FormElement getFormElement() {
    return FormElement.as(getElement());
  }
}
