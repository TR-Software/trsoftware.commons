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

package solutions.trsoftware.commons.server.net;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.annotations.Slow;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Enumeration;
import java.util.List;

import static solutions.trsoftware.commons.server.net.NetUtils.*;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThat;

/**
 * @author Alex
 * @since 3/26/2018
 */
public class NetUtilsTest extends TestCase {

  public void tearDown() throws Exception {
    super.tearDown();
    System.out.println("--------------------------------------------------------------------------------");
  }

  @Slow
  public void testIsLocalPortAvailable() throws Exception {
    int port = findFirstAvailablePort();
    // make this port unavailable
    try (ServerSocket ss = new ServerSocket(port)) {
      System.out.printf("Started a server socket on port %d%n", port);
      assertFalse(isLocalPortAvailable(port));
      System.out.printf("Port %d no longer available%n", port);
    }
  }

  protected static int findFirstAvailablePort() {
    Integer port = findAvailableLocalPort(MIN_USER_PORT, MAX_VALID_PORT);
    System.out.printf("First available port in range [%d, %d] is %s%n", MIN_USER_PORT, MAX_VALID_PORT, port);
    assertNotNull(port);  // should be able to find at least 1 available port
    assertTrue(isLocalPortAvailable(port));
    return port;
  }

  @Slow
  public void testFindAvailableLocalPort() throws Exception {
    int port = findFirstAvailablePort();
    // make this port unavailable
    try (ServerSocket ss = new ServerSocket(port)) {
      System.out.printf("Started a server socket on port %d%n", port);
      assertThat(findFirstAvailablePort()).isGreaterThan(port);
    }
  }

  public void testIsLocalAddress() throws Exception {
    // 1) test the string version of the method
    //   a) check some typical local hostname strings
    for (String hostname : new String[]{"localhost", "127.1.2.3", "0.0.0.0"}) {
      assertTrue(isLocalAddress(hostname));
    }
    for (String hostname : new String[]{"google.com", "ietf.org"}) {
      assertFalse(isLocalAddress(hostname));
    }
    // 2) test the InetAddress version of the method
    //   a) check some typical local addresses
    assertTrue(isLocalAddress(InetAddress.getLocalHost()));
    assertTrue(isLocalAddress(InetAddress.getLoopbackAddress()));
    //   b) now test all the network interfaces on this machine
    Enumeration<NetworkInterface> localInterfaces = NetworkInterface.getNetworkInterfaces();
    while (localInterfaces.hasMoreElements()) {
      NetworkInterface netInter = localInterfaces.nextElement();
      byte[] macAddress = netInter.getHardwareAddress();
      List<InterfaceAddress> interfaceAddresses = netInter.getInterfaceAddresses();
      for (InterfaceAddress interAddr : interfaceAddresses) {
        System.out.printf("Testing IP address %s%n  (from local network interface <%s>)%n", interAddr, netInter);
        assertTrue(isLocalAddress(interAddr.getAddress()));
      }
    }
  }
}