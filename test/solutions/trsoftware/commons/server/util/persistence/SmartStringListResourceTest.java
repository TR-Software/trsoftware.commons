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

package solutions.trsoftware.commons.server.util.persistence;

import solutions.trsoftware.commons.server.io.file.FileUtils;
import solutions.trsoftware.commons.server.testutil.TempFileTestCase;
import solutions.trsoftware.commons.shared.util.callables.Function1;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static solutions.trsoftware.commons.server.io.ServerIOUtils.LINE_SEPARATOR;

/**
 * Nov 4, 2009
 *
 * @author Alex
 */
public class SmartStringListResourceTest extends TempFileTestCase {

  private static final String colonSeparatedFilename = "colonSeparatedStringsMultiline.txt";
  /**
   * All of these files conain the strings "foo", "bar", "baz", but formatted
   * differenly
   */
  private static final String[] defaultSeparatorFilenames = new String[]{
      "commaSeparatedStrings.txt",
      "commaAndSpaceSeparatedStrings.txt",
      "commaSeparatedStringsMultiline.txt"
  };

  public void testCommaSeparatedStringResources() throws Exception {
    for (String filename : defaultSeparatorFilenames) {
      SmartStringListResource<List<String>> slr = newStringListResource(filename);
      assertContains(slr, "foo", "bar", "baz");  // the whitespace got trimmed from the second line
    }
  }

  public void testCommaSeparatedStringResourceWithParser() throws Exception {
    for (String filename : defaultSeparatorFilenames) {
      SmartStringListResource<Set<String>> slr = new SmartStringListResource<Set<String>>(
          getFile(filename),
          new Function1<List<String>, Set<String>>() {
            public Set<String> call(List<String> arg) {
              return new HashSet<String>(arg);
            }
          });
      assertContains(slr, "foo", "bar", "baz");  // the whitespace got trimmed from the second line
    }
  }

  public void testColonSeparatedStringResources() throws Exception {
    SmartStringListResource<List<String>> slr = newStringListResource(colonSeparatedFilename, ":");
    assertContains(slr, "foo", "bar", "  baz");  // the whitespace didn't get trimmed from the second line because space isn't a separator here
  }

  public void testAdd() throws Exception {
    String lineSeparator = LINE_SEPARATOR;
    String tempFileContent = "foo, bar, baz" + lineSeparator + "asdf" + lineSeparator + "a,s,d, f";
    writeTempFile(tempFileContent);
    SmartStringListResource<List<String>> slr = new SmartStringListResource<List<String>>(tempFile);
    assertContains(slr, "foo", "bar", "baz", "asdf", "a", "s", "d", "f");
    // add another string to the resource
    slr.add("qwer");
    // make sure the change has been reflected in memory
    assertContains(slr, "foo", "bar", "baz", "asdf", "a", "s", "d", "f", "qwer");
    // and persisted to disk
    assertEquals("foo, bar, baz" + lineSeparator + "asdf" + lineSeparator + "a,s,d, f,qwer", readTempFile());
  }

  public void testRemove() throws Exception {
    String lineSeparator = LINE_SEPARATOR;
    String tempFileContent = "foo, bar, baz" + lineSeparator + "asdf" + lineSeparator + "a,s,d, f";
    writeTempFile(tempFileContent);
    SmartStringListResource<List<String>> slr = new SmartStringListResource<List<String>>(tempFile);
    assertContains(slr, "foo", "bar", "baz", "asdf", "a", "s", "d", "f");

    // remove a string from the resource
    slr.remove("bar");
    // make sure the change has been persisted to disk
    // (platform specific line breaks will be inserted)
    assertEquals("foo, baz" + lineSeparator + "asdf" + lineSeparator + "a,s,d, f", readTempFile());
    // and reflected in memory
    assertContains(slr, "foo", "baz", "asdf", "a", "s", "d", "f");

    // removing a string that doesn't exist should have no effect
    slr.remove("as");
    assertEquals("foo, baz" + lineSeparator + "asdf" + lineSeparator + "a,s,d, f", readTempFile());
    assertContains(slr, "foo", "baz", "asdf", "a", "s", "d", "f");

    // removing a string that's a substring of another should not modify the superstring
    slr.remove("a");
    assertEquals("foo, baz" + lineSeparator + "asdf" + lineSeparator + "s,d, f", readTempFile());
    assertContains(slr, "foo", "baz", "asdf", "s", "d", "f");
  }

  private SmartStringListResource<List<String>> newStringListResource(String filename, String separators) {
    return new SmartStringListResource<List<String>>(getFile(filename), null, separators);
  }

  private File getFile(String filename) {
    return FileUtils.urlToFile(getClass().getResource(filename));
  }

  private SmartStringListResource<List<String>> newStringListResource(String filename) {
    return new SmartStringListResource<List<String>>(getFile(filename), null);
  }

  private void assertContains(SmartStringListResource<? extends Collection<String>> slr, String... strings) {
    Collection<String> value = slr.getStrings();
    assertEquals(strings.length, value.size());
    for (String string : strings) {
      assertTrue(value.contains(string));
    }
  }
}