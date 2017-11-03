package solutions.trsoftware.commons.server.util.persistence;

import solutions.trsoftware.commons.client.testutil.AssertUtils;
import solutions.trsoftware.commons.server.testutil.TempFileTestCase;

import java.io.IOException;
import java.util.Arrays;

import static solutions.trsoftware.commons.server.io.ServerIOUtils.readCharactersIntoString;
import static solutions.trsoftware.commons.server.io.ServerIOUtils.readFileUTF8;
import static solutions.trsoftware.commons.server.io.ServerIOUtils.writeStringToFileUTF8;

public class ObjectToFileMappingTest extends TempFileTestCase {

  public void testProxyWithNewFile() throws Exception {
    Foo fileBackedObject = new FooImpl("foo", null);  // the instance that is to be backed up to a file after every setter invocation
    Foo proxy = ObjectToFileMapping.createProxy(tempFile, fileBackedObject, Foo.class);  // the proxy to be used to access fileBackedObject in order to enable the backup functionality
    assertFalse(tempFile.exists());
    assertEquals("foo", proxy.getStringField());
    assertEquals(null, proxy.getIntArrField());
    finishTest(fileBackedObject, proxy);
  }

  public void testProxyWithExistingFile() throws Exception {
    writeStringToFileUTF8(tempFile, "{\"stringField\":\"foo\",\"intArrField\":null}");
    Foo defaultTarget = new FooImpl("asdf", new int[]{1});
    Foo proxy = ObjectToFileMapping.createProxy(tempFile, defaultTarget, Foo.class);  // the proxy to be used to access defaultTarget in order to enable the backup functionality
    // make sure that the proxy target is not the default instance we used above, but rather a new instance initialized from the contents of the given file
    assertNotSame(defaultTarget, ObjectToFileMapping.getProxyTarget(tempFile));
    assertEquals("foo", proxy.getStringField());
    assertEquals(null, proxy.getIntArrField());
    finishTest((Foo)ObjectToFileMapping.getProxyTarget(tempFile), proxy);
  }

  /** Makes sure that all proxies created for a particular file are bound to the same target object */
  protected void finishTest(Foo fileBackedObject, Foo proxy) throws IOException {
    // 1) make sure the proxied object will be persisted to disk as soon as one of its setters is called
    proxy.setIntArrField(new int[]{1, 2, 3});
    assertTrue(tempFile.exists());
    assertEquals("{\"stringField\":\"foo\",\"intArrField\":[1,2,3]}", readCharactersIntoString(readFileUTF8(tempFile)).replaceAll("\\s", ""));
    // 2) make sure that all proxies created for a particular file are bound to the same target object
    {
      Foo newProxy = ObjectToFileMapping.createProxy(tempFile, new FooImpl(null, null), Foo.class);
      assertSame(fileBackedObject, ObjectToFileMapping.getProxyTarget(tempFile));
      assertEquals("foo", newProxy.getStringField());
      AssertUtils.assertArraysEqual(new int[]{1, 2, 3}, newProxy.getIntArrField());
      // now check persistence using both proxies, to ensure they both represent the same target object
      newProxy.setStringField("bar");
      proxy.setIntArrField(new int[]{2, 3});
      assertEquals("{\"stringField\":\"bar\",\"intArrField\":[2,3]}", readCharactersIntoString(readFileUTF8(tempFile)).replaceAll("\\s", ""));
    }
    // 3) now repeat the above using the version of the factory method that expects the file to exist (no default instance)
    {
      Foo newProxy = ObjectToFileMapping.createProxyFromFile(tempFile, FooImpl.class, Foo.class);
      assertSame(fileBackedObject, ObjectToFileMapping.getProxyTarget(tempFile));
      assertEquals("bar", newProxy.getStringField());
      AssertUtils.assertArraysEqual(new int[]{2, 3}, newProxy.getIntArrField());
      // now check persistence using both proxies, to ensure they both represent the same target object
      newProxy.setStringField("baz");
      proxy.setIntArrField(new int[]{3, 4, 5});
      assertEquals("{\"stringField\":\"baz\",\"intArrField\":[3,4,5]}", readCharactersIntoString(readFileUTF8(tempFile)).replaceAll("\\s", ""));
    }
  }


  /** The type of object we'll use for testing backup with {@link ObjectToFileMapping} */
  public static interface Foo {
    String getStringField();

    void setStringField(String stringField);

    int[] getIntArrField();

    void setIntArrField(int[] intArrField);
  }

  /** The implmentation of the object we'll use for testing backup with {@link ObjectToFileMapping} */
  public static class FooImpl implements Foo {
    private String stringField;
    private int[] intArrField;

    public FooImpl() {
    }

    public FooImpl(String stringField, int[] intArrField) {
      this.stringField = stringField;
      this.intArrField = intArrField;
    }

    @Override
    public String getStringField() {
      return stringField;
    }

    @Override
    public void setStringField(String stringField) {
      this.stringField = stringField;
    }

    @Override
    public int[] getIntArrField() {
      return intArrField;
    }

    @Override
    public void setIntArrField(int[] intArrField) {
      this.intArrField = intArrField;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      FooImpl valueType = (FooImpl)o;

      if (!Arrays.equals(intArrField, valueType.intArrField)) return false;
      if (stringField != null ? !stringField.equals(valueType.stringField) : valueType.stringField != null)
        return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = stringField != null ? stringField.hashCode() : 0;
      result = 31 * result + (intArrField != null ? Arrays.hashCode(intArrField) : 0);
      return result;
    }
  }


}