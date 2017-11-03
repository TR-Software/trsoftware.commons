package solutions.trsoftware.commons.server.testutil;

import junit.framework.TestCase;

import static solutions.trsoftware.commons.server.io.ServerIOUtils.*;

import java.io.File;
import java.io.IOException;

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
