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

package solutions.trsoftware.commons.server.io;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.util.Duration;

import java.io.*;
import java.util.Arrays;

import static solutions.trsoftware.commons.server.io.ServerIOUtils.*;

/**
 * Oct 18, 2009
 *
 * @author Alex
 */
public class ServerIOUtilsTest extends TestCase {

  public void testFileWithSuffix() throws Exception {
    assertEquals(System.getProperty("user.dir") + File.separator + "bar_bak.txt",
        fileWithSuffix(new File(System.getProperty("user.dir") + File.separator + "bar.txt"), "_bak").getAbsolutePath());
  }

  public void testFilenameWithSuffix() throws Exception {
    assertEquals("bar_bak.txt", filenameWithSuffix("bar.txt", "_bak"));
  }

  public void testResourceNameFromFilenameInPackageOf() throws Exception {
    Class<? extends ServerIOUtilsTest> cls = getClass();
    String filename = "ServerIOUtilsTest.testResourceNameFromFilenameInPackageOf.txt";
    // we want to make sure that this resource can actually be loaded as a file
    // (NOTE: this requires ServerIOUtilsTest.testResourceNameFromFilenameInPackageOf.txt to actually exist in same directory as this class)
    String resourceName = resourceNameFromFilenameInSamePackage(filename, cls);
    File file = resourceNameToFile(resourceName);
    System.out.printf("resourceNameFromFilenameInSamePackage(\"%s\", %s) ->%n%s->%n%s%n", filename, cls, resourceName, file);
    // verify that this file exists
    assertNotNull(file);
    assertTrue(file.exists());
    // verify that it cn be read
    String expectedFileContent = "This file is simply used to test solutions.trsoftware.commons.server.io.ServerIOUtils.resourceNameFromFilenameInPackageOf";
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      // ARM block that automatically closes the reader (this construct was introduced in Java 7; see: http://www.oracle.com/technetwork/articles/java/trywithresources-401775.html )
      assertEquals(expectedFileContent, reader.readLine());
    }
    try (BufferedReader reader = new BufferedReader(readResourceFile(resourceName))) {
      // ARM block that automatically closes the reader (this construct was introduced in Java 7; see: http://www.oracle.com/technetwork/articles/java/trywithresources-401775.html )
      assertEquals(expectedFileContent, reader.readLine());
    }
  }

  public void testFilenamePrefix() throws Exception {
    assertEquals("foo", filenamePrefix("foo.txt"));
    assertEquals("foo", filenamePrefix("foo.txt.gif"));
  }

  public void testFilenameExtension() throws Exception {
    assertEquals("txt", filenameExtension("foo.txt"));
    assertEquals("gif", filenameExtension("foo.txt.gif"));
  }

  public void testReadCharacterStreamIntoStringUtf8() throws Exception {
    int size = 1048576;
    byte[] inputSource = new byte[size];  // use a 1 meg size array filled with 'x'
    Arrays.fill(inputSource, (byte)'x');
    ByteArrayInputStream inputStream = new ByteArrayInputStream(inputSource);
    Duration readTimeDuration = new Duration("Reading input stream of size " + size);
    String result = ServerIOUtils.readCharacterStreamIntoStringUtf8(inputStream);
    System.out.println(readTimeDuration);
    assertEquals(size, result.length());
    assertEquals(new String(inputSource), result);
  }

  public void testReadCharactersIntoString() throws Exception {
    int size = 1048576;
    byte[] inputSource = new byte[size];  // use a 1 meg size array filled with 'x'
    Arrays.fill(inputSource, (byte)'x');
    String inputString = new String(inputSource);
    StringReader reader = new StringReader(inputString);
    Duration readTimeDuration = new Duration("Reading reader stream of size " + size);
    String result = ServerIOUtils.readCharactersIntoString(reader);
    System.out.println(readTimeDuration);
    assertEquals(size, result.length());
    assertEquals(inputString, result);
  }
}