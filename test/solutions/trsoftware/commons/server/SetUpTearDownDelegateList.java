package solutions.trsoftware.commons.server;

import java.util.ArrayList;

/**
 * @author Alex, 3/28/2016
 */
public class SetUpTearDownDelegateList extends ArrayList<SetUpTearDownDelegate> {

  public void setUpAll() throws Exception {
    for (SetUpTearDownDelegate delegate : this) {
      delegate.setUp();
    }
  }

  public void tearDownAll() throws Exception {
    for (SetUpTearDownDelegate delegate : this) {
      delegate.tearDown();
    }
  }

}
