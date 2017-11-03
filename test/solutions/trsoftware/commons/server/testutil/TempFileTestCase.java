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

package solutions.trsoftware.commons.server.testutil;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

import static solutions.trsoftware.commons.server.io.ServerIOUtils.*;

/**
 * Base test case that creates a temporary file in the system's base temp directory (specified by
 * the system property {@code java.io.tmpdir}) on {@link #setUp()} and deletes it on {@link #tearDown()}.
 *
 * This file will be named <code>[{@link #getName()}]_TEMPFILE_[{@link System#nanoTime()}].[suffix]</code>.
 * The {@code [suffix]} part defaults to {@code .txt}, but subclasses can override {@link #getFilenameSuffix()} to use
 * a different value.
 * 
 * @since Nov 4, 2009
 *
 * @author Alex
 */
public abstract class TempFileTestCase extends TestCase {

  protected File tempFile;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    tempFile = new File(System.getProperty("java.io.tmpdir"), getName() + "_TEMPFILE_" + System.nanoTime() + getFilenameSuffix());
    deleteTempFileIfExists();
    tempFile.deleteOnExit();
    assertFalse(tempFile.exists());
  }

  /**
   * Will be used in naming the file.  Defaults to {@code .txt}, but subclasses may override to provide a different suffix.
   *
   * @return the name {@link #tempFile} will end in this suffix
   */
  protected String getFilenameSuffix() {
    return ".txt";
  }

  @Override
  protected void tearDown() throws Exception {
    deleteTempFileIfExists();
    super.tearDown();
  }

  private void deleteTempFileIfExists() {
    if (tempFile.exists())
      assertTrue(tempFile.delete());
  }

  protected void writeTempFile(String content) throws IOException {
    if (!tempFile.exists())
      tempFile.createNewFile();
    writeStringToFileUTF8(tempFile, content);
  }

  protected String readTempFile() throws IOException {
    return readCharactersIntoString(readFileUTF8(tempFile));
  }
}
