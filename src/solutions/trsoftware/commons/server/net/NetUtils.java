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

package solutions.trsoftware.commons.server.net;

import solutions.trsoftware.commons.shared.util.NumberRange;

import java.io.IOException;
import java.net.*;
import java.util.NoSuchElementException;

/**
 * Provides networking utility methods.
 *
 * @author Alex
 * @since 3/22/2018
 */
public class NetUtils {

  /**
   * Hostname of the "loopback" interface.
   * @see <a href="https://en.wikipedia.org/wiki/Localhost">"Localhost" article on Wikipedia</a>
   */
  public static final String LOCALHOST = "localhost";

  /**
   * The lowest port number supported by {@link ServerSocket} (and the typical IP stack of an OS).
   *
   * @see ServerSocket#ServerSocket(int, int, InetAddress)
   * @see <a href="https://tools.ietf.org/html/rfc6335#section-6">RFC 6335 (Section 6: Port Number Ranges)</a>
   * @see <a href="https://stackoverflow.com/q/10476987">StackOverflow discussion</a>
   * @see <a href="https://support.microsoft.com/en-us/help/832017/service-overview-and-network-port-requirements-for-windows">Service overview and network port requirements for Windows</a>
   */
  public static final int MIN_VALID_PORT = 0;
  /**
   * {@value #MAX_VALID_PORT} is the highest port number supported by {@link ServerSocket} (and the typical IP stack of an OS).
   *
   * @see ServerSocket#ServerSocket(int, int, InetAddress)
   * @see <a href="https://tools.ietf.org/html/rfc6335#section-6">RFC 6335 (Section 6: Port Number Ranges)</a>
   * @see <a href="https://stackoverflow.com/q/10476987">StackOverflow discussion</a>
   * @see <a href="https://support.microsoft.com/en-us/help/832017/service-overview-and-network-port-requirements-for-windows">Service overview and network port requirements for Windows</a>
   */
  public static final int MAX_VALID_PORT = 0xFFFF;

  /**
   * The minimum port number that is likely not reserved by the OS.
   * <p>
   * According to the IETF <a href="https://www.ietf.org/rfc/rfc1700.txt">RFC 1700</a>, it appears to be {@code 1024},
   * but for Microsoft Windows it might be {@code 1025}.
   *
   * <p style="font-style: italic;">
   *   CAUTION: although a port number higher than this value is unlikely to be reserved by the OS,
   *   it could be in use by some other popular application, so it might be a good idea to check the
   *   <a href="https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml">
   *     IANA Service Name and Transport Protocol Port Number Registry</a>
   *   when choosing a port number for your application.
   * </p>
   *
   * @see <a href="https://tools.ietf.org/html/rfc6335#section-6">RFC 6335 (Section 6: Port Number Ranges)</a>
   * @see <a href="https://stackoverflow.com/q/10476987">StackOverflow discussion</a>
   * @see <a href="https://support.microsoft.com/en-us/help/832017/service-overview-and-network-port-requirements-for-windows">Service overview and network port requirements for Windows</a>
   * @see #isLocalPortAvailable(int)
   */
  public static final int MIN_USER_PORT = 1024;

  /**
   * The minimum port number used for dynamic TCP/IP connection allocations by the typical IP stack of an OS.
   * It's probably never a good idea to use these for an app server.
   *
   * @see <a href="https://tools.ietf.org/html/rfc6335#section-6">RFC 6335 (Section 6: Port Number Ranges)</a>
   * @see <a href="https://stackoverflow.com/q/10476987">StackOverflow discussion</a>
   * @see <a href="https://support.microsoft.com/en-us/help/832017/service-overview-and-network-port-requirements-for-windows">Service overview and network port requirements for Windows</a>
   */
  public static final int MIN_DYNAMIC_PORT = 49152;

  /**
   * @return {@code true} iff a socket connection cannot be established to the given port on the loopback interface
   * ({@value #LOCALHOST})
   * @see <a href="https://stackoverflow.com/q/434718">StackOverflow discussion</a>
   */
  public static boolean isLocalPortAvailable(int port) {
    // solution suggested by https://stackoverflow.com/q/434718
    try (Socket s = new Socket(LOCALHOST, port)) { // will throw exception if unable to connect
      return false;  // we were able to connect to the port, which means some process is listening on it
    }
    catch (IOException e) {
      return true;  // unable to connect, so the port is most likely free
    }
  }

  /**
   * Finds the next available port in the range [{@code preferredPort}, {@value #MIN_DYNAMIC_PORT})
   * on the loopback interface {@value #LOCALHOST}.
   *
   * @param preferredPort the search will start with this port number
   * @return the first available local port in the range [{@code preferredPort}, {@value #MIN_DYNAMIC_PORT})
   * @throws NoAvailablePortException if unable to find an available port in this range
   * @see #isLocalPortAvailable(int)
   * @see #MIN_USER_PORT
   * @see #MIN_DYNAMIC_PORT
   * @see #MAX_VALID_PORT
   */
  public static int findNextAvailableLocalPort(int preferredPort) {
    return findAvailableLocalPort(preferredPort, MIN_DYNAMIC_PORT);
  }

  /**
   * Finds an available port in the given range on the loopback interface {@value #LOCALHOST}.
   * <p>
   * It's probably best to use ports in the range [{@value #MIN_USER_PORT}, {@value #MIN_DYNAMIC_PORT}] when looking for
   * a port for your (long-running) application, or [{@value #MIN_DYNAMIC_PORT}, {@value #MAX_VALID_PORT}] for temporary
   * allocations.
   *
   * @param minPort the search will start with this port number
   * @param maxPort the search will end on this port number (inclusive)
   * @return the first available local port in the given range
   * @throws NoAvailablePortException if unable to find an available port in this range
   * @see #isLocalPortAvailable(int)
   * @see #MIN_USER_PORT
   * @see #MIN_DYNAMIC_PORT
   * @see #MAX_VALID_PORT
   */
  public static int findAvailableLocalPort(int minPort, int maxPort) {
    for (int p = minPort; p <= maxPort; p++) {
      if (isLocalPortAvailable(p))
        return p;
    }
    throw new NoAvailablePortException(minPort, maxPort);
  }

  /**
   * Thrown by {@link #findAvailableLocalPort(int, int)} to indicate that no available port was found within the given
   * search range.
   */
  public static class NoAvailablePortException extends NoSuchElementException {
    private NumberRange<Integer> scannedRange;

    public NoAvailablePortException(NumberRange<Integer> scannedRange) {
      super("No available ports found in range " + scannedRange);
      this.scannedRange = scannedRange;
    }

    NoAvailablePortException(int minPort, int maxPort) {
      this(new NumberRange<>(minPort, maxPort));
    }

    public NumberRange<Integer> getScannedRange() {
      return scannedRange;
    }
  }

  /**
   * Attempts to determine whether the host specified by the given hostname or IP address is on a local network.
   * Simply resolves the given hostname (or IP address string) using {@link InetAddress#getByName(String)} and
   * calls {@link #isLocalAddress(InetAddress)} with the result.
   *
   * @param hostname a host name or ip address string (the hostname can be obtained from a URL with {@link URL#getHost()})
   * @return {@code true} if the IP address of the given host is typically used only for a local network interface;
   * <b>NOTE</b>: a return value of {@code false} <i>does not</i> necessarily mean that it's not on the local network.
   * @throws UnknownHostException propagated from {@link InetAddress#getByName(String)}
   * @see #isLocalAddress(InetAddress)
   */
  public static boolean isLocalAddress(String hostname) throws UnknownHostException {
    return isLocalAddress(InetAddress.getByName(hostname));
  }

  /**
   * Attempts to determine whether the given IP address specifies an interface on a local network.
   * Without knowing the local network topology, the best we can do is to use a naive approach that simply
   * checks whether the IP address falls into any of the reserved ranges, such as:
   * <ul>
   *   <li>
   *     <a href="https://en.wikipedia.org/wiki/Localhost#Loopback">loopback</a> (see {@link InetAddress#isLoopbackAddress()})
   *   </li>
   *   <li>
   *     <a href="https://en.wikipedia.org/wiki/0.0.0.0">0.0.0.0</a> (wildcard that satisfies any interface on local machine);
   *     see {@link InetAddress#isAnyLocalAddress()}
   *   </li>
   *   <li><a href="https://tools.ietf.org/html/rfc1918">private</a> (see {@link InetAddress#isSiteLocalAddress()})</li>
   *   <li>etc...
   *     <ul>
   *       <li>see {@link InetAddress#isLinkLocalAddress()}</li>
   *       <li>see {@link InetAddress#isMulticastAddress()}</li>
   *     </ul>
   *   </li>
   * </ul>
   *
   * @param address an IP address
   * @return {@code true} if the given IP address is typically used only for a local network interface;
   * <b>NOTE</b>: a return value of {@code false} <i>does not</i> necessarily mean that it's not on the local network.
   * @throws UnknownHostException propagated from {@link InetAddress#getByName(String)}
   */
  protected static boolean isLocalAddress(InetAddress address) {
    // this code was borrowed from https://codereview.stackexchange.com/a/65072
    return address.isLoopbackAddress() ||
        address.isAnyLocalAddress() ||
        address.isSiteLocalAddress() ||
        address.isLinkLocalAddress() ||
        address.isMulticastAddress();
  }
}
