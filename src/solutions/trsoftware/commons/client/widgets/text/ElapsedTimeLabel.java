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

package solutions.trsoftware.commons.client.widgets.text;

import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.ValueLabel;
import solutions.trsoftware.commons.shared.util.TimeUtils;

/**
 * Renders an elapsed time value (in millis) into a human-readable string like "15 minutes ago".
 *
 * @author Alex
 * @since 6/18/2018
 */
public class ElapsedTimeLabel extends ValueLabel<Double> {

  public ElapsedTimeLabel() {
    super(new ElapsedTimeRenderer());
    setStyleName("ElapsedTimeLabel");
  }

  public ElapsedTimeLabel(double elapsedMillis) {
    this();
    setValue(elapsedMillis);
  }

  /**
   * Renders a milliseconds value into a human-readable string like "15 minutes ago"
   */
  public static class ElapsedTimeRenderer extends AbstractRenderer<Double> {
    @Override
    public String render(Double elapsedMillis) {
      return TimeUtils.generateRelativeTimeElapsedString(elapsedMillis);
    }
  }
}
