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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Ensures that at most one of a group of disclosure panels is open at any given time.
 * 
 * Nov 5, 2009
 *
 * @author Alex
 */
public class DisclosurePanelGroup implements OpenHandler<DisclosurePanel>, Iterable<DisclosurePanel> {

  private final List<DisclosurePanel> disclosurePanels = new ArrayList<DisclosurePanel>();
  private final boolean enableAnimations;

  public DisclosurePanelGroup(boolean enableAnimations, DisclosurePanel... panels) {
    this.enableAnimations = enableAnimations;
    for (DisclosurePanel panel : panels) {
      add(panel);
    }
  }

  public void add(DisclosurePanel panel) {
    disclosurePanels.add(panel);
    panel.addOpenHandler(this);
    panel.setAnimationEnabled(enableAnimations);
  }

  public List<DisclosurePanel> getPanels() {
    return disclosurePanels;
  }

  public void onOpen(OpenEvent<DisclosurePanel> event) {
    for (DisclosurePanel panel : disclosurePanels) {
      if (panel != event.getSource())
        panel.setOpen(false);
    }
  }

  public Iterator<DisclosurePanel> iterator() {
    return disclosurePanels.iterator();
  }
}
