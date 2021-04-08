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

package solutions.trsoftware.commons.server.util.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Implements {@link JsonSerializer} using the GSON library.
 *
 * @see <a href="https://github.com/google/gson/blob/master/UserGuide.md">GSON User Guide</a>
 *
 * @author Alex, 12/31/2014
 */
public class GsonSerializer<T> implements JsonSerializer<T> {

  protected final Gson gson;

  protected final Class<T> valueType;

  /**
   * @param valueType Needs to be passed explicitly despite this class already having a generic type argument,
   * because Java's generic "type erasure" (the removal of actual type arguments for instances at compile time)
   * makes it impossible to derive the actual generic type from an instance.
   * Read <a href="http://www.javacodegeeks.com/2013/12/advanced-java-generics-retreiving-generic-type-arguments.html">
   * this article</a> for more info.
   */
  public GsonSerializer(Class<T> valueType) {
    this.valueType = valueType;
    GsonBuilder gsonBuilder = new GsonBuilder();
    configureGson(gsonBuilder);
    gson = gsonBuilder.create();
  }

  /**
   * Subclasses can override this method to change the GSON output/parsing settings.
   * @see <a href="https://github.com/google/gson/blob/master/UserGuide.md">GSON User Guide</a>
   */
  protected void configureGson(GsonBuilder gsonBuilder) {
    gsonBuilder.setDateFormat("MMM d, yyyy h:mm:ss.S a");  // preserve millisecond precision in dates
  }

  @Override
  public T parseJson(String json) {
    return gson.fromJson(json, getValueType());
  }

  @Override
  public String toJson(T instance) {
    return gson.toJson(instance, getValueType());
  }

  @Override
  public Class<T> getValueType() {
    return valueType;
  }

}
