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

package solutions.trsoftware.commons.server.testutil;

import solutions.trsoftware.commons.server.io.file.FileUtils;
import solutions.trsoftware.commons.server.util.CloseablePool;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A unit test helper that sets up and tears down a new temporary directory
 * (under the default temporary-file directory specified by {@code java.io.tmpdir}).
 *
 * @since Mar 4, 2010
 * @author Alex
 */
public class TempDirDelegate extends SetUpTearDownDelegate {
  /**
   * For convenience, readers and writers that manipulate files in this
   * directory can be registered here, to ensure they are closed at tear down
   * (otherwise will not be able to delete the directory).
   * @deprecated TODO: use try-with-resources blocks instead
   */
  private CloseablePool closeablePool = new CloseablePool();

  private Path tempDir;

  /**
   * Sets up the fixture, for example, open a network connection. This method is
   * called before a test is executed.
   */
  @Override
  public void setUp() throws Exception {
    tempDir = Files.createTempDirectory(getClass().getName());
    assertTrue(Files.exists(tempDir));
    FileUtils.deleteOnExit(tempDir);
  }

  /**
   * Tears down the fixture, for example, close a network connection. This method
   * is called after a test is executed.
   */
  @Override
  public void tearDown() throws Exception {
    closeablePool.close();
    FileUtils.deleteFileTree(tempDir);
    assertFalse(Files.exists(tempDir));
  }

  /**
   * @return the directory that was created by {@link #setUp()}, as a {@link Path} object.
   */
  public Path getTempDir() {
    return tempDir;
  }

  /**
   * For convenience, readers and writers that manipulate files in this
   * directory can be registered here, to ensure they are closed at tear down
   * (otherwise will not be able to delete the directory).
   * @deprecated TODO: use try-with-resources blocks instead
   */
  public CloseablePool getCloseablePool() {
    return closeablePool;
  }
}
