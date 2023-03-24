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

package solutions.trsoftware.commons.shared.testutil;

import com.google.common.base.MoreObjects;
import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamWriter;
import solutions.trsoftware.commons.server.util.rpc.SimpleSerializationPolicy;
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.testutil.rpc.ArrayCustomFieldSerializer;
import solutions.trsoftware.commons.shared.util.function.ThrowingRunnable;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Arrays;

import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertArraysEqual;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;

/**
 * @author Alex
 * @since 1/14/2023
 */
public class MockSerializationStreamWriterTest extends BaseTestCase {

  private MockSerializationStreamWriter mockWriter;

  public void setUp() throws Exception {
    super.setUp();
    mockWriter = new MockSerializationStreamWriter();
  }

  public void testWriteObject() throws Exception {
    int expectedId = 99;
    String expectedName = "foo" + expectedId;
    Foo foo = new Foo(expectedId);
    System.out.println("foo = " + foo);
    assertEquals(expectedId, foo.id);
    assertEquals(expectedName, foo.name);
    assertEquals(expectedId + 1, foo.x);

    mockWriter.prepareToWrite();
    System.out.println("mockWriter =   " + mockWriter);

    // needs a CustomFieldSerializer for Foo 
    assertThrows(UnsupportedOperationException.class, (ThrowingRunnable)() -> mockWriter.writeObject(foo));
    System.out.println("mockWriter =   " + mockWriter);
    
    mockWriter.setClassSerializer(Foo.class, new FooCustomFieldSerializer());
    
    mockWriter.prepareToWrite();
    mockWriter.writeObject(foo);
    System.out.println("mockWriter =   " + mockWriter);

    ServerSerializationStreamWriter serverWriter = createServerWriter();
    serverWriter.writeObject(foo);
    System.out.println("serverWriter = " + serverWriter);

    // TODO: compare the output of MockSerializationStreamWriter to that of ServerSerializationStreamWriter (compensating for the typeName '/' suffixes)

    String fooSerialized = mockWriter.toString();
    MockSerializationStreamReader mockReader = new MockSerializationStreamReader(mockWriter);
    mockReader.prepareToRead(fooSerialized);

    /*
    TODO: this fails because AbstractSerializationStreamReader.prepareToRead calls readInt() x 2 to read the
      version and flags from header, and our MockSerializationStreamReader fails to account for the header (it's not passed by MockSerializationStreamWriter)
     */

    Foo fooDeserialized = (Foo)mockReader.readObject();
    System.out.println("fooDeserialized = " + fooDeserialized);
    assertEquals(expectedId, fooDeserialized.id);
    assertEquals(expectedName, fooDeserialized.name);
    assertEquals(0, fooDeserialized.x);  // x is transient, so still 0

    // TODO: test writing arrays & enums
  }

  @Nonnull
  private ServerSerializationStreamWriter createServerWriter() {
    ServerSerializationStreamWriter serverWriter = new ServerSerializationStreamWriter(new SimpleSerializationPolicy());
    serverWriter.prepareToWrite();
    return serverWriter;
  }

  public void testWriteArray() throws Exception {
    int length = 5;
    int[] iArr = new int[length];
    String[] stArr = new String[length];
    Foo[] fooArr = new Foo[length];

    for (int i = 0; i < length; i++) {
      iArr[i] = 10 + i;
      stArr[i] = "foo" + i;
      fooArr[i] = new Foo(10 + i);
    }

    System.out.println("===== Writing " + Arrays.toString(iArr));
    int[] iArrResult = (int[])writeAndReadObject(iArr);
    assertArraysEqual(iArr, iArrResult);

    System.out.println("\n===== Writing " + Arrays.toString(stArr));
    String[] stArrResult = (String[])writeAndReadObject(stArr);
    assertArraysEqual(stArr, stArrResult);

    mockWriter.setClassSerializer(Foo.class, new FooCustomFieldSerializer());
    mockWriter.setClassSerializer(Foo[].class, ArrayCustomFieldSerializer.create(Foo[]::new));
    System.out.println("\n===== Writing " + Arrays.toString(fooArr));
    Foo[] fooArrResult = (Foo[])writeAndReadObject(fooArr);
    assertArraysEqual(fooArr, fooArrResult);

  }


  private Object writeAndReadObject(Object instance) throws SerializationException {
    mockWriter.prepareToWrite();
    mockWriter.writeObject(instance);
    System.out.println("mockWriter =   " + mockWriter);

    ServerSerializationStreamWriter serverWriter = createServerWriter();
    serverWriter.writeObject(instance);
    System.out.println("serverWriter = " + serverWriter);

    String serialized = mockWriter.toString();

    MockSerializationStreamReader mockReader = new MockSerializationStreamReader(mockWriter);
    mockReader.prepareToRead(serialized);
    return mockReader.readObject();
  }
  
  public static void assertSerializedOutputMatchesServer(MockSerializationStreamWriter mockWriter, ServerSerializationStreamWriter serverWriter) {
    String mockWriterOut = mockWriter.toString();
    String serverWriterOut = serverWriter.toString();
    // TODO: remove the CRC32 suffix from type names in server output, to compare results
  }


  private static class Foo implements Serializable {
    public transient int x;
    private int id;
    private String name;

    private Foo(int id) {
      this.id = id;
      this.name = "foo" + id;
      x = id + 1;
    }

    private Foo() {

    }

    int getId() {
      return id;
    }

    String getName() {
      return name;
    }

    void setName(String name) {
      this.name = name;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;

      Foo foo = (Foo)o;

      if (id != foo.id)
        return false;
      return name.equals(foo.name);
    }

    @Override
    public int hashCode() {
      int result = id;
      result = 31 * result + name.hashCode();
      return result;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("foo", id)
          .add("name", name)
          .add("x", x)
          .toString();
    }
  }

  private static class FooCustomFieldSerializer extends CustomFieldSerializer<Foo> {
    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, Foo instance) throws SerializationException {
      streamWriter.writeInt(instance.id);
      streamWriter.writeString(instance.name);
    }

    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, Foo instance) throws SerializationException {
      instance.id = streamReader.readInt();
      instance.name = streamReader.readString();
    }

    @Override
    public Foo instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
      return new Foo();
    }
  }

  private static class FooArrCustomFieldSerializer extends ArrayCustomFieldSerializer<Foo> {
    @Override
    protected Foo[] instantiateArray(int length) {
      return new Foo[length];
    }
  }


}