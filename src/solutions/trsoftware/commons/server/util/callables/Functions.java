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

package solutions.trsoftware.commons.server.util.callables;

import solutions.trsoftware.commons.client.util.callables.Function1;

import java.lang.reflect.InvocationTargetException;

/**
 * Feb 15, 2010
 *
 * @author Alex
 */
public class Functions {

  public static <V,A> Function1<A, V> fromMethod1Arg(final Object instance, final String methodName, final Class<A> argType) {
    return arg -> {
      try {
        return (V)instance.getClass().getMethod(methodName, argType).invoke(instance, arg);
      }
      catch (IllegalAccessException | NoSuchMethodException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      catch (InvocationTargetException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    };
  }
}
