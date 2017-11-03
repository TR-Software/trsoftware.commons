/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.io.csv;

import solutions.trsoftware.commons.client.util.callables.Function1;

import java.util.Map;

/**
 * Mar 15, 2010
 *
 * @author Alex
 */
public class CSVObjectBinder<T> extends CSVObjectBinderBase<T> {
  private final Map<String, Function1<String, Object>> fieldParsers;
  private final Map<String, Function1<Object, String>> fieldSerializers;

  public CSVObjectBinder(Class<T> type, String[] fieldNames, Map<String, Function1<String, Object>> fieldParsers, Map<String, Function1<Object, String>> fieldSerializers) {
    super(type, fieldNames);
    this.fieldParsers = fieldParsers;
    this.fieldSerializers = fieldSerializers;
  }

  public Object fieldFromString(String name, String value) {
    return fieldParsers.get(name).call(value);
  }

  public String fieldToString(String name, Object value) {
    return fieldSerializers.get(name).call(value);
  }
}
