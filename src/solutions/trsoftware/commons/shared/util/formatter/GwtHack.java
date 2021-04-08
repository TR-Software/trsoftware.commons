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

package solutions.trsoftware.commons.shared.util.formatter;

import com.google.gwt.core.shared.GwtIncompatible;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Marker annotation that indicates that a particular piece of code had to be modified from the original
 * JDK source to make it compatible with GWT.
 *
 * @see GwtIncompatible
 * @see <a href="http://www.gwtproject.org/doc/latest/RefJreEmulation.html">JRE Classes Emulated by GWT</a>
 * @author Alex
 * @since 8/11/2019
 */
@Retention(RetentionPolicy.SOURCE)
// applies to all possible element types:
@Target({METHOD, TYPE, FIELD, LOCAL_VARIABLE, PACKAGE, CONSTRUCTOR, PARAMETER, TYPE_PARAMETER, TYPE_USE})
public @interface GwtHack {
  /**
   * This default attribute can be used to explain why the code is incompatible, for documentation purposes.
   */
  String description() default "";

  /**
   * This optional attribute can be used to give a snippet of the original code that wan't compatible with GWT.
   */
  String code() default "";

}
