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

package solutions.trsoftware.commons.shared.util.template;

import java.lang.annotation.*;

/**
 * Dec 11, 2008
 *
 * @author Alex
 */
public interface TemplateBundle {
  /**
   * Explicitly specifies a file name or path to the image resource to be
   * associated with a method in an {@link TemplateBundle}. If the path is
   * unqualified (that is, if it contains no slashes), then it is sought in the
   * package enclosing the image bundle to which the annotation is attached. If
   * the path is qualified, then it is expected that the string can be passed
   * verbatim to <code>ClassLoader.getResource()</code>.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @interface Resource {
    String value();
  }
}
