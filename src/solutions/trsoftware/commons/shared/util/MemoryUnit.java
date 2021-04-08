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

package solutions.trsoftware.commons.shared.util;

/**
 * A GWT-compatible, simplified, version of {@code java.util.concurrent.TimeUnit} for memory size quantities.
 *
 * @author Alex
 */
public enum MemoryUnit {
  BYTES(1, "B"),
  KILOBYTES(BYTES.bytes * 1024, "KB"),
  MEGABYTES(KILOBYTES.bytes * 1024, "MB"),
  GIGABYTES(MEGABYTES.bytes * 1024, "GB");

  /** The number of bytes in this memory unit */
  public final double bytes;
  public final String abbreviation;

  MemoryUnit(double bytes, String abbreviation) {
    this.bytes = bytes;
    this.abbreviation = abbreviation;
  }

  /**
   * Convert the given memory amount in the given unit to this unit.
   * @param sourceUnit the unit of the <tt>sourceAmount</tt> argument
   * @param sourceAmount the amount in the given <tt>sourceUnit</tt>
   * @return the converted amount in this unit, which could be a fraction.
   */
  public double from(MemoryUnit sourceUnit, double sourceAmount) {
    return (sourceUnit.bytes * sourceAmount) / bytes;
  }

  /**
   * Convert the give time duration in this unit to the given unit.
   * @param targetUnit the unit of the desired result<tt>amount</tt> argument
   * @param amount the amount in this unit.
   * @return the converted amount in <tt>targetUnit</tt>, which could be a fraction.
   */
  public double to(MemoryUnit targetUnit, double amount) {
    return targetUnit.from(this, amount);
  }

  // the following methods provide shorthand notation for the most common conversions
  public double toBytes(double amount) {
    return to(BYTES, amount);
  }

  public double fromBytes(double amount) {
    return from(BYTES, amount);
  }

  /** Picks the best unit to represent the given number of bytes to a human consumer */
  public static MemoryUnit bestForHuman(double bytes) {
    MemoryUnit[] units = values();
    MemoryUnit best = units[0];
    for (int i = 1; i < units.length; i++) {
      MemoryUnit unit = units[i];
      if (unit.fromBytes(bytes) > 1)
        best = unit;
      else
        break;
    }
    return best;
  }
}