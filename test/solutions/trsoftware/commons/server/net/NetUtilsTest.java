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
import solutions.trsoftware.commons.shared.util.CollectionUtils;
import solutions.trsoftware.commons.shared.util.NumberRange;
import solutions.trsoftware.commons.shared.util.function.ThrowingRunnable;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static solutions.trsoftware.commons.server.net.NetUtils.*;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThat;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.shared.util.function.ThrowingConsumer.unchecked;

/**
 * @author Alex
 * @since 3/26/2018
 */
public class NetUtilsTest extends TestCase {

  public void tearDown() throws Exception {
    super.tearDown();
    System.out.println("--------------------------------------------------------------------------------");
  }

  public void testIsLocalPortAvailable() throws Exception {
    int port = findFirstAvailablePort();
    assertTrue(isLocalPortAvailable(port));
    // make this port unavailable
    try (ServerSocket socket = new ServerSocket(port)) {
      socket.setReuseAddress(true); // Allow immediate reuse
      System.out.printf("Started a server socket on port %d%n", port);
      assertFalse(isLocalPortAvailable(port));
      System.out.printf("Port %d no longer available%n", port);
    }
    assertTrue(isLocalPortAvailable(port));  // port should be released by the above try-with-resources
    // also check unavailable UDP port
    try (DatagramSocket socket = new DatagramSocket(port)) {
      socket.setReuseAddress(true); // Allow immediate reuse
      assertFalse(isLocalPortAvailable(port));
    }
    assertTrue(isLocalPortAvailable(port));  // port should be released by the above try-with-resources
  }

  private static int findFirstAvailablePort() {
    int port = findAvailableLocalPort(MIN_USER_PORT, MAX_VALID_PORT);
    System.out.printf("First available port in range [%d, %d] is %s%n", MIN_USER_PORT, MAX_VALID_PORT, port);
    assertTrue(isLocalPortAvailable(port));
    return port;
  }

  public void testFindAvailableLocalPort() throws Exception {
    int port = findFirstAvailablePort();
    assertTrue(isLocalPortAvailable(port));
    // make this port unavailable
    try (ServerSocket socket = new ServerSocket(port)) {
      socket.setReuseAddress(true); // Allow immediate reuse
      System.out.printf("Started a server socket on port %d%n", port);
      assertFalse(isLocalPortAvailable(port));
      int nextPort = findFirstAvailablePort();
      assertTrue(isLocalPortAvailable(nextPort));
      assertThat(nextPort).isGreaterThan(port);
    }
    /* test findAvailableLocalPort(int, int) without any available ports in a given range (should throw NoAvailablePortException)
       TODO(9/28/2026): consider refactoring findAvailableLocalPort to returni an OptionalInt instead of throwing
     */
    int minPort = port;
    int maxPort = port + 10;
    NumberRange<Integer> occupiedPorts = NumberRange.of(minPort, maxPort);
    System.out.printf("Ensuring that ports %s are unavailable%n", occupiedPorts);
    withOccupiedPorts(occupiedPorts, (ThrowingRunnable)() -> {
      assertThrows(NoAvailablePortException.class, () -> findAvailableLocalPort(minPort, maxPort));
    });
  }

  /**
   * Executes the given action after ensuring that the specified ports are not available.
   * The ports that were occupied by this method will be released upon return.
   *
   * @param occupiedPorts the ports for which to ensure unavailability
   */
  private void withOccupiedPorts(Iterable<Integer> occupiedPorts, Runnable action) throws Exception {
    List<ServerSocket> openedSockets = new ArrayList<>();  // keep track of opened sockets to close them on finally
    try {
      for (Integer port : occupiedPorts) {
        try {
          ServerSocket socket = new ServerSocket(port);  // Note: intentionally not using try-with-resources for this
          socket.setReuseAddress(true); // Allow immediate reuse
          System.out.printf("Started a server socket on port %d%n", port);
          assertTrue(socket.isBound());
          assertFalse(isLocalPortAvailable(port));
          openedSockets.add(socket);
        }
        catch (IOException | SecurityException | IllegalArgumentException e) {
          // ignoring exception: port must be already occupied
        }
      }
      // verify that all the specified ports are now unavailable
      occupiedPorts.forEach(port -> assertFalse("port " + port, isLocalPortAvailable(port)));
      // all the desired ports are now occupied: execute the action
      action.run();
    }
    finally {
      CollectionUtils.tryForEach(openedSockets, unchecked(ServerSocket::close));
      // make sure all the sockets created by this method have been released
      openedSockets.forEach(socket ->
          assertTrue(socket.toString(), socket.isClosed()));
    }
  }

  @Slow
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