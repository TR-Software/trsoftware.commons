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
import solutions.trsoftware.commons.shared.util.MapDecorator;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static solutions.trsoftware.commons.server.util.codec.CodecUtils.hexToBase64;

/**
 * @author Alex
 * @since 11/11/2017
 */
public class FileIndexTest extends TestCase {

  public void testIndexing() throws Exception {
    Class<? extends FileIndexTest> thisClass = getClass();
    File thisClassFile = ServerIOUtils.getClassFile(thisClass);
    File testDir = new File(thisClassFile.getParentFile(), thisClass.getSimpleName() + "_files");
    assertTrue(testDir.exists());
    assertTrue(testDir.isDirectory());
    FileIndex fileIndex = new FileIndex(FileSet.allFiles(testDir));
    /* Expected results:
      $ md5sum *
      2126bfa3aa041a9b1cc4a1f73eb8161b *clipboard24.png
      9f2078fa28a44277c5822c1a760c3ed4 *text.txt
     */
    Map<String, File> expectedResults = new MapDecorator<String, File>(new LinkedHashMap<>())
        .put(hexToBase64("2126bfa3aa041a9b1cc4a1f73eb8161b"), new File(testDir, "clipboard24.png"))
        .put(hexToBase64("9f2078fa28a44277c5822c1a760c3ed4"), new File(testDir, "text.txt"))
        .getMap();
    assertEquals(expectedResults, fileIndex);
  }

}