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

import javax.management.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * An ORM-like facility for mapping MBean attributes (obtained from {@link MBeanServer#getAttribute(ObjectName, String)})
 * to an instance of a POJO.
 *
 * @author Alex
 * @since 6/13/2018
 */
public class AttributeMapper {

  private MBeanServer mBeanServer;

  public AttributeMapper() {
    this(MBeanServerFactory.findMBeanServer(null).get(0));
  }

  public AttributeMapper(MBeanServer mBeanServer) {
    this.mBeanServer = mBeanServer;
  }

  public <T extends MBeanProxy> T map(ObjectName objectName, Class<T> proxyClass) throws IllegalAccessException, InstantiationException, InstanceNotFoundException {
    T instance = proxyClass.newInstance();
    Field[] fields = proxyClass.getDeclaredFields();
    for (Field field : fields) {
      int mod = field.getModifiers();
      if (Modifier.isStatic(mod) || Modifier.isFinal(mod) || field.getAnnotation(MBeanProxy.ExcludeField.class) != null) {
        // we exclude static and final fields, as well as fields excluded by annotation
        continue;
      }
      MBeanProxy.Attribute attrAnn = field.getAnnotation(MBeanProxy.Attribute.class);
      String attrName = attrAnn != null && !attrAnn.name().isEmpty()
          ? attrAnn.name()
          : field.getName();
      try {
        Object attrValue = mBeanServer.getAttribute(objectName, attrName);
        field.setAccessible(true);
        field.set(instance, attrValue);
      }
      catch (MBeanException | AttributeNotFoundException | ReflectionException e) {
        e.printStackTrace();  // suppress these exceptions - we simply won't set the field value in this case
      }
    }
    return instance;
  }
}
