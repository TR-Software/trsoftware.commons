/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util.rpc;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import solutions.trsoftware.commons.shared.util.MathUtils;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.util.Random;

/**
 * @author Alex
 * @since 4/29/2022
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1)
@Fork(value = 1, warmups = 1)
@Measurement(time = 2)
//@Threads(4)
public class SerializationUtilsBenchmark {

  @State(Scope.Benchmark)
  public static class BenchmarkConfig {

    @Param({"0", "1", "2", "3", "4", "5"})
    public int precision;

    public static double[] values = randomValues(1000, 0, 300);

  }

  @Benchmark
  public void baseline(BenchmarkConfig config, Blackhole blackhole) {
    for (double value : BenchmarkConfig.values) {
      blackhole.consume(value);
    }
  }

  @Benchmark
  public void doubleToScaledInt(BenchmarkConfig config, Blackhole blackhole) {
    for (double value : BenchmarkConfig.values) {
      int scaledInt = SerializationUtils.doubleToScaledInt(value, config.precision);
      blackhole.consume(scaledInt);
    }
  }

  @Benchmark
  public void roundDouble(BenchmarkConfig config, Blackhole blackhole) {
    for (double value : BenchmarkConfig.values) {
      double rounded = MathUtils.round(value, config.precision);
      blackhole.consume(rounded);
    }
  }

  /**
   * @return {@code n} random {@code double}s in range {@code [lowerBound, upperBound]}.
   */
  public static double[] randomValues(int n, double lowerBound, double upperBound) {
    // generate some random doubles
    Random rnd = new Random(1);
    double[] values = new double[n];
    for (int i = 0; i < values.length; i++) {
      values[i] = RandomUtils.nextDoubleInRange(rnd, lowerBound, upperBound);
    }
    return values;
  }

/*  public static void main(String[] args) {
    // generate an array of string literals for the precision @Param of BenchmarkConfig
    System.out.println(IntStream.rangeClosed(0, 5).mapToObj(String::valueOf).map(StringUtils::quote).collect(Collectors.joining(", ", "{", "}")));
  }*/

}
