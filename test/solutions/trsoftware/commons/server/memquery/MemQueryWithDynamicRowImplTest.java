package solutions.trsoftware.commons.server.memquery;

import solutions.trsoftware.commons.shared.annotations.Slow;

/**
 * Tests the same functionality as {@link MemQueryTest} with a {@link RowFactory} instance that uses
 * {@link DynamicRowImplGenerator}.
 *
 * @author Alex
 * @since 1/28/2019
 */
@Slow
public class MemQueryWithDynamicRowImplTest extends MemQueryTest {

  private RowFactory defaultRowFactory;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    defaultRowFactory = RowFactory.getInstance();  // back up the original instance
    RowFactory.setInstance(new RowFactory(true, true));
  }

  @Override
  public void tearDown() throws Exception {
    RowFactory.setInstance(defaultRowFactory);  // restore the original instance
    super.tearDown();
  }
}
