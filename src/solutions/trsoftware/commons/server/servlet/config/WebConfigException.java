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

package solutions.trsoftware.commons.server.servlet.config;

import static solutions.trsoftware.commons.shared.util.StringUtils.valueToString;

/**
 * @author Alex
 * @since 1/3/2018
 */
public abstract class WebConfigException extends Exception {
  private String paramName;
  private String paramValue;
  private Class<?> expectedType;
  private HasInitParameters config;

  WebConfigException(String message, Throwable cause, String paramName, String paramValue, Class<?> expectedType, HasInitParameters config) {
    super(message + " " + paramInfoToString(paramName, paramValue, expectedType), cause);
    this.paramName = paramName;
    this.paramValue = paramValue;
    this.expectedType = expectedType;
  }

  WebConfigException(String message, String paramName, String paramValue, Class<?> expectedType, HasInitParameters config) {
    this(message, null, paramName, paramValue, expectedType, config);
  }

  public String getParamName() {
    return paramName;
  }

  public String getParamValue() {
    return paramValue;
  }

  public Class<?> getExpectedType() {
    return expectedType;
  }

  public HasInitParameters getConfig() {
    return config;
  }

  private static String paramInfoToString(String paramName, String paramValue, Class<?> expectedType) {
    final StringBuilder sb = new StringBuilder("{");
    sb.append("param-name: ").append(valueToString(paramName));
    sb.append(", param-value: ").append(valueToString(paramValue));
    sb.append(", expected type: ").append(expectedType.getSimpleName());
    sb.append('}');
    return sb.toString();
  }

}
