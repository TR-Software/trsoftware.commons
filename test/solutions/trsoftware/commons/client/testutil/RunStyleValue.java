/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.client.testutil;

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.shared.util.SetUtils;

import java.util.Collections;
import java.util.Set;

/**
 * Parsed value of the {@code -runStyle} arg in {@code gwt.args}.
 * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideTesting.html">GWT Testing Guide</a>
 * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideTestingHtmlUnit.html">GWT Testing HTML Unit Guide</a>
 */
public class RunStyleValue {
  private final String value;
  private String name;
  private Set<String> args;

  /**
   * @param value the {@code -runStyle} arg
   */
  public RunStyleValue(String value) {
    this.value = value;
    name = value;
    int colon = value.indexOf(':');
    if (colon >= 0) {
      name = value.substring(0, colon);
      String args = value.substring(colon + 1);
      this.args = SetUtils.newSet(args.split(","));
    } else {
      this.args = Collections.emptySet();
    }
  }

  /**
   * @return the name of this RunStyle (e.g. "HtmlUnit", "Manual", etc.)
   */
  public String getName() {
    return name;
  }

  /**
   * @return the args for this RunStyle (e.g. browser names for "HtmlUnit", or number of clients for "Manual")
   */
  public Set<String> getArgs() {
    return args;
  }

  /**
   * @return the full {@code -runStyle} string.
   */
  @Override
  public String toString() {
    return value;
  }

  public String toDebugString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("args", args)
        .toString();
  }
}
