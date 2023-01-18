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
import com.google.gwt.core.client.GWT;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides run-time access to the {@code runStyleHtmlUnit} and {@code junit.runStyle} properties
 * defined in {@code TestCommons.gwt.xml}.
 *
 * @author Alex
 * @since 12/12/2021
 * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideTesting.html">GWT Testing Guide</a>
 * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideTestingHtmlUnit.html">GWT Testing HTML Unit Guide</a>
 */
public interface RunStyleInfo {

  RunStyleInfo INSTANCE = GWT.create(RunStyleInfo.class);

  /**
   * Getter for the {@code runStyleHtmlUnit} property defined in {@code TestCommons.gwt.xml}.
   *
   * @return {@code true} iff the {@code -runStyle} arg in {@code gwt.args} starts with {@code "HtmlUnit"}
   */
  boolean isHtmlUnit();

  /**
   * Getter for the {@code junit.runStyle} property defined in {@code TestCommons.gwt.xml}.
   *
   * @return the full value of the actual {@code -runStyle} arg in {@code gwt.args}
   *   or an empty string if not specified
   * @see #getRunStyleValue()
   */
  @Nonnull
  String getRunStyleString();

  /**
   * @return the parsed value of the {@code -runStyle} arg in {@code gwt.args}, or {@code null} if not available.
   * @see #getRunStyleString()
   */
  @Nullable
  default RunStyleValue getRunStyleValue() {
    String value = getRunStyleString();
    if (StringUtils.notBlank(value))
      return new RunStyleValue(value);
    return null;
  }

  default String toDebugString() {
    return MoreObjects.toStringHelper(this)
        .add("htmlUnit", isHtmlUnit())
        .add("runStyleString", StringUtils.quote(getRunStyleString()))
        .toString();
  }
}
