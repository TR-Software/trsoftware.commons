package solutions.trsoftware.commons.shared.testutil;

import solutions.trsoftware.commons.server.io.ServerIOUtils;

import java.io.IOException;

/**
 * @author Alex
 * @since 11/4/2017
 */
public class TestData {


  public static String getAliceInWonderlandText() throws IOException {
    return ServerIOUtils.readResourceFileIntoString(getAliceInWonderlandTextResourceName());
  }

  public static String getAliceInWonderlandTextResourceName() {
    return ServerIOUtils.resourceNameFromFilenameInSamePackage("aliceInWonderlandCorpus.txt", TestData.class);
  }

}
