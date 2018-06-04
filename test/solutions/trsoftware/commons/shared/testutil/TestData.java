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

package solutions.trsoftware.commons.shared.testutil;

import solutions.trsoftware.commons.server.io.ResourceLocator;
import solutions.trsoftware.commons.shared.util.LazyReference;

import java.io.IOException;
import java.util.List;

import static solutions.trsoftware.commons.server.io.ServerIOUtils.readLines;

/**
 * @author Alex
 * @since 11/4/2017
 */
public class TestData {

  public static final ResourceLocator ALICE_TEXT_RESOURCE = new ResourceLocator("aliceInWonderlandCorpus.txt", TestData.class);
  private static LazyReference<String> aliceInWonderlandText = new LazyReference<String>() {
    @Override
    protected String create() {
      try {
        return ALICE_TEXT_RESOURCE.getContentAsString();
      }
      catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  };

  private static LazyReference<List<String>> aliceInWonderlandTextLines = new LazyReference<List<String>>() {
    @Override
    protected List<String> create() {
      return readLines(ALICE_TEXT_RESOURCE.getReaderUTF8(), true);
    }
  };

  public static String getAliceInWonderlandText() throws IOException {
    return aliceInWonderlandText.get();
  }

  public static List<String> getAliceInWonderlandTextLines() {
    return aliceInWonderlandTextLines.get();
  }
}
