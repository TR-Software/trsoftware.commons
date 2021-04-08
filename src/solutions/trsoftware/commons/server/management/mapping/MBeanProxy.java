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

package solutions.trsoftware.commons.server.management.mapping;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker interface for objects that obtain their attribute values from {@link MBeanServer#getAttribute(ObjectName, String)}
 *
 * @see AttributeMapper
 *
 * @author Alex
 * @since 6/13/2018
 */
public interface MBeanProxy {

  /**
   * Fields of an {@link MBeanProxy} subclass may be marked with this optional annotation, which
   * allows customizing how they're mapped from the attributes of the underlying MBean by {@link AttributeMapper}.
   */
  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  @interface Attribute {
    /**
     * @return the name of the MBean attribute to be mapped to this field by an {@link AttributeMapper}.
     * This will be used as the 2nd arg for invoking {@link MBeanServer#getAttribute(ObjectName, String)}.
     * Defaults to the declared name of the field, if not specified.
     */
    String name() default "";
  }

  /**
   * Fields of an {@link MBeanProxy} subclass may be marked with this optional annotation, which
   * tells {@link AttributeMapper} to exclude this field from the attribute mapping.
   */
  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  @interface ExcludeField {

  }

}
