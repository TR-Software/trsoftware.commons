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

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * Maps files in a {@link FileSet} by their {@code MD5} checksums (encoded as base64 strings).
 *
 * @author Alex
 * @since 11/11/2017
 */
public class FileIndex extends LinkedHashMap<String, File> {

  public interface ProgressListener {
    /**
     * Will be called right before the given file is added to this {@link FileIndex} 
     */
    void before(File file);

    /**
     * Will be called right after the given file is added to this {@link FileIndex}
     * @param md5sum the base64-encoded checksum computed for the file.
     */
    void after(File file, String md5sum);
  }
  
  private final File baseDir;
  
  private ProgressListener progressListener;
  
  private FileDigest fileDigest;

  /**
   * @param files the set of files to index
   * @param progressListener will be invoked once  
   */
  public FileIndex(FileSet files, ProgressListener progressListener) {
    this(files);
    this.progressListener = progressListener;
  }

  public FileIndex(FileSet files) {
    this.baseDir = files.getBaseDir();
    fileDigest = new FileDigest();
    addFiles(files);
    // clear the refs to utility objects, to free memory
    fileDigest = null;
  }

  public File getBaseDir() {
    return baseDir;
  }

  private void addFiles(FileSet files) {
    for (File file : files) {
      if (progressListener != null)
        progressListener.before(file);
      try {
        String md5sum = fileDigest.md5sumBase64(file);
        put(md5sum, file);
        if (progressListener != null)
          progressListener.after(file, md5sum);
      }
      catch (IOException e) {
        // a FileNotFoundException might happen if the file system was modified after the FileSet was computed
        // we just ignore it, and move on to the next file
      }
    }
  }


}
