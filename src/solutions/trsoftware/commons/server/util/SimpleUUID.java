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

package solutions.trsoftware.commons.server.util;

import java.util.UUID;

/**
 * Produces compact url-safe Base64-encoded strings derived from
 * a {@linkplain UUID#randomUUID() random (version 4) UUID}.
 * <p>
 * Because this class encodes the binary UUID as Base64, it produces strings that are (approximately) only 24 chars long
 * instead of the 36 produced by {@link UUID#toString()}.
 *
 * <h3>Implementation Details</h3>
 * The {@link #randomUUID()} method takes the result {@link UUID#randomUUID()} and encodes its 128-bit value using a
 * url-safe Base64 variant specifically tailored for encoding 128-bit integers to avoid any trailing padding
 * characters normally present when using the standard Base64 encoding on arbitrary binary data.
 * <strong><em>This is a lossy encoding:</em></strong>
 * in order to avoid padding chars, it doesn't preserve the sign of integer
 * (leading 0-bits will be prepended to its binary representation such that the total number
 * of bits is divisible by 6). Hence, it may not be possible to convert the result back to the original {@link UUID}.
 *
 * @author Alex
 * @see UUID
 * @see <a href="https://en.wikipedia.org/wiki/Universally_unique_identifier#Version_4_(random)">Version 4 (random) UUID
 *     (Wikipedia)</a>
 * @see NumberRadixEncoder#toStringBase64(long, long)
 */
public abstract class SimpleUUID {

  /**
   * @return a short, url-safe, Base64-encoded representation of a {@link UUID#randomUUID()}
   * @see NumberRadixEncoder#toStringBase64(long, long)
   */
  public static String randomUUID() {
    UUID uuid = UUID.randomUUID();
    return NumberRadixEncoder.toStringBase64(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
  }

}