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

package solutions.trsoftware.commons.client.util;

/**
 * Mar 24, 2010
 *
 * @author Alex
 */
public class MessageFormatter {
  public static String exceptionTypeAndMessageToString(Throwable ex) {
    return StringUtils.template("$1 ($2)", ex.getMessage(), exceptionTypeToString(ex));
  }

  /** Convenience method for getting the name of the class of the given Exception */
  public static String exceptionTypeToString(Throwable ex) {
    String type = "Unknown";
    // these null checks are probably not necessary, but just in case...
    if (ex.getClass() != null) {
      String className = ex.getClass().getName();
      if (StringUtils.notBlank(className))
        type = className;
    }
    return type;
  }
}
