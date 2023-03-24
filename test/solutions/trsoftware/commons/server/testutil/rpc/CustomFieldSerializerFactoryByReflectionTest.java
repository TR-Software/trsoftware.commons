/*
 * Copyright 2023 TR Software Inc.
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

package solutions.trsoftware.commons.server.testutil.rpc;

import com.google.common.base.MoreObjects;
import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;
import solutions.trsoftware.commons.server.util.rpc.SimpleSerializationPolicy;
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.testutil.MockSerializationStreamReader;
import solutions.trsoftware.commons.shared.testutil.MockSerializationStreamWriter;

import java.io.Serializable;

/**
 * @author Alex
 * @since 1/14/2023
 */
public class CustomFieldSerializerFactoryByReflectionTest extends BaseTestCase {

  private SimpleSerializationPolicy serializationPolicy;
  private CustomFieldSerializerFactoryByReflection factory;
  private MockSerializationStreamWriter mockWriter;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    serializationPolicy = new SimpleSerializationPolicy();
    factory = new CustomFieldSerializerFactoryByReflection(serializationPolicy);
    mockWriter = new MockSerializationStreamWriter(factory);
  }

  public void testGetCustomFieldSerializer() throws Exception {
    Foo foo = new Foo(1);
    System.out.println("foo = " + foo);
    assertEquals(1, foo.foo);
    assertEquals("foo1", foo.bar);
    assertEquals(2, foo.x);
    CustomFieldSerializer<Object> fooSerializer = factory.getCustomFieldSerializer(Foo.class);
    fooSerializer.serializeInstance(mockWriter, foo);
    System.out.println("mockWriter =   " + mockWriter);
    ServerSerializationStreamWriter serverWriter = new ServerSerializationStreamWriter(serializationPolicy);
    fooSerializer.serializeInstance(serverWriter, foo);
    System.out.println("serverWriter = " + serverWriter);
    MockSerializationStreamReader mockReader = new MockSerializationStreamReader(mockWriter);

    Foo fooDeserialized = (Foo)fooSerializer.instantiateInstance(mockReader);
    assertEquals(0, fooDeserialized.foo);
    assertEquals(0, fooDeserialized.x);
    assertNull(fooDeserialized.bar);

    fooSerializer.deserializeInstance(mockReader, fooDeserialized);
    System.out.println("fooDeserialized = " + fooDeserialized);
    assertEquals(1, fooDeserialized.foo);
    assertEquals(0, fooDeserialized.x);  // x is transient, so still 0
    assertEquals("foo1", fooDeserialized.bar);
  }


  private static class Foo implements Serializable {
    public transient int x;
    private int foo;
    private String bar;

    private Foo(int foo) {
      this.foo = foo;
      this.bar = "foo" + foo;
      x = foo + 1;
    }

    private Foo() {

    }

    public int getFoo() {
      return foo;
    }

    public String getBar() {
      return bar;
    }

    public void setBar(String bar) {
      this.bar = bar;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("foo", foo)
          .add("bar", bar)
          .add("x", x)
          .toString();
    }
  }
}