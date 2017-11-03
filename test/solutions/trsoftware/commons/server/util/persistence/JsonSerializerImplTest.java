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

package solutions.trsoftware.commons.server.util.persistence;

import junit.framework.TestCase;

import java.util.Date;

public class JsonSerializerImplTest extends TestCase {


  public void testSerialization() throws Exception {
    // 1) test an object with primitive fields
    verifySerialization(new Foo(1, "asdf"));
    // 2) test serialization of dates: the default GSON date serialization loses the millisecond precision;
    // we want to make sure our implementation doesn't suffer from that
    verifySerialization(new Date());

  }

  private static <T> void verifySerialization(T instance) {
    JsonSerializerImpl<T> jsonizer = new JsonSerializerImpl<T>((Class<T>)instance.getClass());
    String json = jsonizer.toJson(instance);
    System.out.printf("<%s> serialized as %s%n", instance, json);
    assertEquals(instance, jsonizer.parseJson(json));
  }


  private static class Foo {
    private int a;
    private String b;

    private Foo(int a, String b) {
      this.a = a;
      this.b = b;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Foo foo = (Foo)o;

      if (a != foo.a) return false;
      if (b != null ? !b.equals(foo.b) : foo.b != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = a;
      result = 31 * result + (b != null ? b.hashCode() : 0);
      return result;
    }
  }
}