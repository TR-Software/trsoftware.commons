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
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;

/**
 * Computes file checksums.
 *
 * @author Alex
 * @since 11/11/2017
 */
public class FileDigest extends LinkedHashMap<String, File> {

  private final NullOutputStream nullOutputStream;
  private final MessageDigest md5;

  private static FileDigest instance;

  public static FileDigest getInstance() {
    if (instance == null)
      instance = new FileDigest();
    return instance;
  }

  public FileDigest() {
    nullOutputStream = new NullOutputStream();
    try {
      md5 = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * @return a base64-encoded md5 checksum of the given file.
   */
  public String md5sumBase64(File file) throws IOException {
    byte[] digest = md5sum(file);
    return Base64.getEncoder().encodeToString(digest);
  }

  /**
   * @return md5 checksum of the given file.
   */
  public byte[] md5sum(File file) throws IOException {
    md5.reset();
    try (DigestInputStream inputStream = new DigestInputStream(new FileInputStream(file), md5)) {
      ServerIOUtils.copyInputToOutput(inputStream, nullOutputStream, 1 << 23);  // using an 8-meg buffer for better perf
    }
    return md5.digest();
  }


}
