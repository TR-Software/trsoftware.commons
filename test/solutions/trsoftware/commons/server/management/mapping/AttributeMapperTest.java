/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.server.management.mapping;

import solutions.trsoftware.commons.server.SuperTestCase;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * @author Alex
 * @since 6/13/2018
 */
public class AttributeMapperTest extends SuperTestCase {

  private AttributeMapper mapper;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    // NOTE: the JVM doesn't start an MBeanServer by default, so we call ManagementFactory.getPlatformMBeanServer() for that
    mapper = new AttributeMapper(ManagementFactory.getPlatformMBeanServer());
  }

  @Override
  public void tearDown() throws Exception {
    mapper = null;
    super.tearDown();
  }

  static class OperatingSystemMBeanProxy implements MBeanProxy {
    private String Name;
    @Attribute(name = "Arch")
    private String architecture;
    private int AvailableProcessors;

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("OperatingSystemMBeanProxy{");
      sb.append("name='").append(Name).append('\'');
      sb.append(", architecture='").append(architecture).append('\'');
      sb.append(", availableProcessors=").append(AvailableProcessors);
      sb.append('}');
      return sb.toString();
    }
  }

  public void testMap() throws Exception {
    OperatingSystemMBeanProxy proxy = mapper.map(new ObjectName("java.lang:type=OperatingSystem"), OperatingSystemMBeanProxy.class);
    System.out.println(proxy);
    assertEquals(System.getProperty("os.name"), proxy.Name);
    assertEquals(System.getProperty("os.arch"), proxy.architecture);
    assertEquals(Runtime.getRuntime().availableProcessors(), proxy.AvailableProcessors);
  }
}