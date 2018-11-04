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

package solutions.trsoftware.commons.server.management.tomcat;

import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardService;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.util.ArrayList;

/**
 * A facade for accessing information provided by Tomcat's various MBeans.
 *
 * @author Alex
 * @since 6/13/2018
 */
public class CatalinaMBeans {

  private final MBeanServer mBeanServer;

  private Service service;
  private Connector[] connectors;

  /**
   * Obtains the Catalina {@link Service} from the system's {@link MBeanServer}
   * @throws JMException if unable to obtain the Catalina {@link Service} from the {@link MBeanServer},
   * which probably means that Tomcat is not running in this JVM.
   */
  public CatalinaMBeans() throws JMException {
    ArrayList<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
    if (mBeanServers.isEmpty()) {
      // there's no MBeanServer running in this JVM yet, which probably means that the current app is not running
      // in Tomcat, so there's probably no chance of getting the Catalina MBeans, so we'll throw an exception here
      throw new JMException("MBeanServer not found");
    }
    this.mBeanServer = mBeanServers.get(0);
    service = (StandardService)this.mBeanServer.getAttribute(new ObjectName("Catalina", "type", "Service"), "managedResource");
    connectors = service.findConnectors();
  }

  /**
   * @return the {@link Connector} configured to listen on the given port, or {@code null} if no such {@link Connector}
   * was found.
   */
  public Connector findConnectorByPortNumber(int port) {
    for (Connector connector : connectors) {
      if (connector.getPort() == port)
        return connector;
    }
    return null;
  }
}
