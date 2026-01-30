/*
 * Copyright 2025 TR Software Inc.
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

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utils for data structures from Guava.
 *
 * @author Alex
 * @since 5/24/2025
 */
public class GuavaUtils {

  /**
   * If the specified {@code (rowKey, columnKey)} pair is not already associated with a value in the specified table
   * (or is mapped to {@code null}), attempts to compute its value using the given mapping function
   * and enters it into the table map unless {@code null}.
   *
   * @param rowKey row key that the computed value should be associated with
   * @param columnKey column key that the computed value should be associated with
   * @param mappingFunction the function to compute a value
   *
   * @return the current (existing or computed) value associated with
   *         the specified {@code (rowKey, columnKey)} pair, or {@code null} if the computed value is {@code null}
   * @throws NullPointerException if the specified keys are null and the table does not support null keys,
   *         or the mappingFunction is null
   * @throws IllegalArgumentException if the specified keys do not satisfy some constraint
   *         imposed by the the table implementation (e.g. {@link ArrayTable#put})
   *         (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
   * @see Map#computeIfAbsent(Object, Function)
   */
  public static <R, C, V> V computeIfAbsent(Table<R, C, V> table, R rowKey, C columnKey,
                                            BiFunction<R, C, V> mappingFunction) {
    /*
     TODO: maybe contribute patch to Guava (as a default method of Table interface)
     (see https://github.com/google/guava/issues/2170)
    */
    Objects.requireNonNull(mappingFunction);
    V v;
    if ((v = table.get(rowKey, columnKey)) == null) {
        V newValue;
        if ((newValue = mappingFunction.apply(rowKey, columnKey)) != null) {
            table.put(rowKey, columnKey, newValue);
            return newValue;
        }
    }
    return v;
  }
}
