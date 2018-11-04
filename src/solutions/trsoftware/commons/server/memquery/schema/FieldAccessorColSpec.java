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

package solutions.trsoftware.commons.server.memquery.schema;

import java.lang.reflect.Field;

/**
 * A fully-specified ColSpec based on a Field of some class.
 *
 * @author Alex, 1/5/14
 */
public class FieldAccessorColSpec<T> extends ReflectionAccessorColSpec<T> {

  private final Field field;

  public FieldAccessorColSpec(Field field) {
    super(field.getName(), (Class<T>)field.getType());
    field.setAccessible(true);
    this.field = field;
  }

  public Field getField() {
    return field;
  }


  @Override
  protected T getValueByReflection(Object instance) throws IllegalAccessException {
    return (T)field.get(instance);
  }


}
