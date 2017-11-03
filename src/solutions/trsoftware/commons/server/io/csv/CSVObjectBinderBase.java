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

import java.lang.reflect.Field;

/**
 *
 * Uses reflection to read and write instances of a particular class to and from
 * a CSV line (String[]).
 * 
 * @author Alex
 */
public abstract class CSVObjectBinderBase<T> {
  private final Class<T> type;

  private final String[] fieldNames;
  private final Field[] fields;


  protected CSVObjectBinderBase(Class<T> type, String[] fieldNames) {
    this.type = type;
    this.fieldNames = fieldNames;
    fields = new Field[fieldNames.length];
    for (int i = 0; i < fields.length; i++) {
      // NOTE: Class.getField() only returns public fields even if we set a custom SecurityManager, etc.
      // that's why we must call cls.getDeclaredField() TODO: for each class in the inheritance hierarchy
      try {
        fields[i] = type.getDeclaredField(fieldNames[i]);
      }
      catch (NoSuchFieldException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      fields[i].setAccessible(true);
    }
  }

  public T parseCsvLine(String[] line) throws IllegalAccessException, InstantiationException {
    T instance = type.newInstance();
    for (int i = 0; i < line.length; i++) {
      fields[i].set(instance, fieldFromString(fieldNames[i], line[i]));
    }
    return instance;
  }

  public String[] writeCsvLine(T instance) throws IllegalAccessException {
    String[] line = new String[fieldNames.length];
    for (int i = 0; i < line.length; i++) {
      line[i] = fieldToString(fieldNames[i], fields[i].get(instance));
    }
    return line;
  }

  public abstract Object fieldFromString(String name, String value);

  public abstract String fieldToString(String name, Object value);
}