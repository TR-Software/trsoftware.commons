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

import com.google.gwt.core.shared.GwtIncompatible;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import java.util.Random;

/**
 * @author Alex
 * @since 4/29/2022
 */
@GwtIncompatible
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1)
@Fork(value = 1, warmups = 1)
@Measurement(time = 2)
//@Threads(4)
public class SerializationUtilsBenchmark2 {

  public static double[] values = randomDoubles(1000, 0, 300);
  public static int[] precisions = randomInts(1000, 0, 10);

  @State(Scope.Benchmark)
  public static class ScaledIntBenchmarkConfig {
    @Param
    public Impl impl;

  }


//  @Benchmark
  public void doubleToScaledInt(ScaledIntBenchmarkConfig config, Blackhole blackhole) {
    for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
      double value = values[i];
      int precision = precisions[i];
      int scaledInt = config.impl.doubleToScaledInt(value, precision);
      blackhole.consume(scaledInt);
    }
  }

  @Benchmark
  public void doubleToScaledIntString(ScaledIntBenchmarkConfig config, Blackhole blackhole) {
    for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
      double value = values[i];
      int precision = precisions[i];
      int scaledInt = config.impl.doubleToScaledInt(value, precision);
      blackhole.consume(String.valueOf(scaledInt));
    }
  }
  
  @Benchmark
  @Measurement(time = 10)
  public void doubleArithmetic(ArithmeticBenchmarkConfig config, Blackhole blackhole) {
    for (double value : values) {
      for (int i = 1; i <= 5; i++) {
        blackhole.consume(config.op.apply(value, i));
      }
    }
  }


  @State(Scope.Benchmark)
  public static class ArithmeticBenchmarkConfig {
    @Param
    public DoubleOperator op;
  }

  public enum DoubleOperator {
    DIVISION() {
      public double apply(double x, int i) {
        return x / powersOf10[i];
      }
    },
    MULTIPLICATION() {
      public double apply(double x, int i) {
        return x * powersOf10Minus[i];
      }
    };
    public abstract double apply(double x, int i);
  }



  private static final double[] powersOf10 = new double[10];
  private static final double[] powersOf10Minus = new double[powersOf10.length];

  static {
    for (int i = 0; i < powersOf10.length; i++) {
      powersOf10[i] = Math.pow(10, i);
      powersOf10Minus[i] = Math.pow(10, -i);
    }
  }

  public enum Impl {
    V1() {
      @Override
      public int doubleToScaledInt(double value, int precision) {
        assert precision >= 0;
        // TODO: what if arg is NaN or Infinity?  throw exception?
        double scaled;
        if (value == 0.0) {
          scaled = value;  // fast path  (TODO: benchmark to make sure that having an if-stmt doesn't actually degrade the performance
        }
        else {
          scaled = value * Math.pow(10, precision);
          // assert that the scaled value won't overflow an int
          assert scaled >= Integer.MIN_VALUE && scaled <= Integer.MAX_VALUE;
        }
        return (int)Math.round(scaled);
      }
    },
    V2() {
      @Override
      public int doubleToScaledInt(double value, int precision) {
        assert precision >= 0;
        double scaled = value * Math.pow(10, precision);
        // assert that the scaled value won't overflow an int
        assert scaled >= Integer.MIN_VALUE && scaled <= Integer.MAX_VALUE;
        return (int)Math.round(scaled);
      }
    },
    V3() {
      @Override
      public int doubleToScaledInt(double value, int precision) {
        assert precision >= 0;
        double factor = powersOf10[precision];
        double scaled = value * factor;
        // assert that the scaled value won't overflow an int
        assert scaled >= Integer.MIN_VALUE && scaled <= Integer.MAX_VALUE;
        return (int)Math.round(scaled);
      }
    },
    V4() {
      @Override
      public int doubleToScaledInt(double value, int precision) {
        assert precision >= 0;
        double factor = precision < powersOf10.length ? powersOf10[precision] : Math.pow(10, precision);
        double scaled = value * factor;
        // assert that the scaled value won't overflow an int
        assert scaled >= Integer.MIN_VALUE && scaled <= Integer.MAX_VALUE;
        return (int)Math.round(scaled);
      }
    },
    ;
    public abstract int doubleToScaledInt(double value, int precision);
  }


  /**
   * @return {@code n} random {@code double}s in range {@code [lowerBound, upperBound]}.
   */
  public static double[] randomDoubles(int n, double lowerBound, double upperBound) {
    // generate some random doubles
    Random rnd = new Random(1);
    double[] values = new double[n];
    for (int i = 0; i < values.length; i++) {
      values[i] = RandomUtils.nextDoubleInRange(rnd, lowerBound, upperBound);
    }
    return values;
  }

  /**
   * @return {@code n} random {@code int}s in range {@code [lowerBound, upperBound]}.
   */
  public static int[] randomInts(int n, int lowerBound, int upperBound) {
    // generate some random ints
    Random rnd = new Random(1);
    int[] values = new int[n];
    for (int i = 0; i < values.length; i++) {
      values[i] = RandomUtils.nextIntInRange(rnd, lowerBound, upperBound);
    }
    return values;
  }

/*  public static void main(String[] args) {
    // generate an array of string literals for the precision @Param of ScaledIntBenchmarkConfig
    System.out.println(IntStream.rangeClosed(0, 5).mapToObj(String::valueOf).map(StringUtils::quote).collect(Collectors.joining(", ", "{", "}")));
  }*/

}
