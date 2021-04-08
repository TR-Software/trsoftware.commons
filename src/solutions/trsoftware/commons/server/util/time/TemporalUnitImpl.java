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

package solutions.trsoftware.commons.server.util.time;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

/**
 * Encapsulates a {@link Duration} and provides a basic implementation of all the methods required by the 
 * {@link TemporalUnit} interface based on that duration.  Allows creating new time units
 * similar to {@link ChronoUnit}.
 * <p>
 * Subclasses may override the methods {@link #isDurationEstimated()}, {@link #isDateBased()}, and {@link #isTimeBased()}
 * as needed.  This base implementation returns {@code true} only for {@link #isTimeBased()}.
 *
 * @author Alex
 * @since 9/25/2019
 */
public class TemporalUnitImpl implements TemporalUnit {

  private final String name;
  private final Duration duration;

  public TemporalUnitImpl(String name, Duration duration) {
    this.name = Objects.requireNonNull(name);
    this.duration = duration;
  }

  public TemporalUnitImpl(Duration duration) {
    this.duration = Objects.requireNonNull(duration);
    name = duration.toString();
  }

  @Override
  public Duration getDuration() {
    return duration;
  }

  @Override
  public boolean isDurationEstimated() {
    return false;
  }

  @Override
  public boolean isDateBased() {
    return false;
  }

  @Override
  public boolean isTimeBased() {
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends Temporal> R addTo(R temporal, long amount) {
    return (R)temporal.plus(duration.multipliedBy(amount));
  }

  @Override
  public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
    return temporal1Inclusive.until(temporal2Exclusive, this);
  }

  @Override
  public String toString() {
    return name;
  }
}
