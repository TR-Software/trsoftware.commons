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

package solutions.trsoftware.commons.shared.util;

import com.google.common.collect.ImmutableSet;
import solutions.trsoftware.commons.client.util.GwtUtils;
import solutions.trsoftware.commons.shared.util.iterators.TransformingIterator;

import java.util.Set;

/**
 * Provides utility methods for working with sub-types of {@link Number}
 * @author Alex
 * @since 12/30/2017
 */
public class NumberUtils {

  private static ImmutableSet<Class<? extends Number>> primitiveWrapperTypes;

  /**
   * @param cls a primitive wrapper type
   * @return the maximum value representable by the given numeric type
   * @throws IllegalArgumentException if the given class is not a primitive wrapper (as defined by {@link #allPrimitiveWrapperTypes()})
   */
  public static Number maxValue(Class<? extends Number> cls) {
    assertIsPrimitiveWrapper(cls);
    if (cls == Integer.class)
      return Integer.MAX_VALUE;
    else if (cls == Long.class)
      return Long.MAX_VALUE;
    else if (cls == Double.class)
      return Double.MAX_VALUE;
    else if (cls == Float.class)
      return Float.MAX_VALUE;
    else if (cls == Short.class)
      return Short.MAX_VALUE;
    else if (cls == Byte.class)
      return Byte.MAX_VALUE;
    else
      throw new IllegalStateException();
  }

  /**
   * @param cls a primitive wrapper type
   * @return the minimum value representable by the given numeric type
   * @throws IllegalArgumentException if the given class is not a primitive wrapper (as defined by {@link #allPrimitiveWrapperTypes()})
   */
  public static Number minValue(Class<? extends Number> cls) {
    assertIsPrimitiveWrapper(cls);
    if (cls == Integer.class)
      return Integer.MIN_VALUE;
    else if (cls == Long.class)
      return Long.MIN_VALUE;
    else if (cls == Double.class)
      return Double.MIN_VALUE;
    else if (cls == Float.class)
      return Float.MIN_VALUE;
    else if (cls == Short.class)
      return Short.MIN_VALUE;
    else if (cls == Byte.class)
      return Byte.MIN_VALUE;
    else
      throw new IllegalStateException();
  }

  /**
   * Casts a {@code double} to the given primitive wrapper type.
   * <p style="font-style: italic;">
   *   <b>WARNING:</b> this might require a lossy
   *   <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.1.3">narrowing conversion</a>,
   *   in which case the returned value will likely be undesirable.
   * </p>
   * @param cls a primitive wrapper type
   * @param value the value to cast to the given type
   * @return the given {@code double} cast to the given primitive wrapper type
   * @throws IllegalArgumentException if the given class is not a primitive wrapper (as defined by {@link #allPrimitiveWrapperTypes()})
   */
  public static Number fromDouble(Class<? extends Number> cls, double value) {
    assertIsPrimitiveWrapper(cls);
    if (cls == Integer.class)
      return (int)value;
    else if (cls == Long.class)
      return (long)value;
    else if (cls == Double.class)
      return value;
    else if (cls == Float.class)
      return (float)value;
    else if (cls == Short.class)
      return (short)value;
    else if (cls == Byte.class)
      return (byte)value;
    else
      throw new IllegalStateException();
  }

  private static void assertIsPrimitiveWrapper(Class<? extends Number> cls) {
    Set<Class<? extends Number>> primitiveWrapperTypes = allPrimitiveWrapperTypes();
    if (!primitiveWrapperTypes.contains(cls))
      throw new IllegalArgumentException(cls.toString() + " is not a primitive wrapper ("
          + StringUtils.join(", ", ", or ",
          new TransformingIterator<Class<? extends Number>, String>(primitiveWrapperTypes.iterator()) {
            @Override
            protected String transform(Class<? extends Number> input) {
              return GwtUtils.getSimpleName(input);
            }
          })
          + ")");
  }

  /**
   * @return an immutable set containing all wrapper classes for Java's primitive types, i.e:
   * {@link Integer}, {@link Long}, {@link Double}, {@link Float}, {@link Short}, and {@link Byte}
   */
  public static Set<Class<? extends Number>> allPrimitiveWrapperTypes() {
    if (primitiveWrapperTypes == null)
      primitiveWrapperTypes = ImmutableSet.<Class<? extends Number>>of(Integer.class, Long.class, Double.class, Float.class, Short.class, Byte.class);
    return primitiveWrapperTypes;
  }
}
