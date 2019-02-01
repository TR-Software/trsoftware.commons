package solutions.trsoftware.commons.server.memquery.algebra;

import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.memquery.RelationSchema;
import solutions.trsoftware.commons.server.memquery.schema.NameAccessorColSpec;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.util.CollectionUtils;
import solutions.trsoftware.commons.shared.util.MapUtils;

import java.util.Arrays;

/**
 * @author Alex
 * @since 1/13/2019
 */
public class RenameTest extends TestCase {

  private RelationSchema fooSchema;
  private Rename renameOpFooToBar;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    fooSchema = new RelationSchema("Foo", Arrays.asList(
        new NameAccessorColSpec<>("foo", Integer.class),
        new NameAccessorColSpec<>("bar", String.class)
    ));
    renameOpFooToBar = new Rename(new RelationalValue(fooSchema), "Bar",
        MapUtils.stringLinkedHashMap(
            "foo", "bar",
            "bar", "bar1"
        ));
  }

  public void testConstructor() throws Exception {
    // what if the input schema already contains a col name that we're trying to rename another col to?
    AssertUtils.assertThrows(IllegalArgumentException.class, (Runnable)() -> {
      Rename renameOp = new Rename(new RelationalValue(fooSchema), "Bar",
          MapUtils.stringLinkedHashMap(
              "foo", "bar"
          ));
    });
  }

  public void testCreateOutputName() throws Exception {
    assertEquals("Bar", renameOpFooToBar.createOutputName());
  }

  public void testGetOutputColNames() throws Exception {
    assertEquals(Arrays.asList("bar", "bar1"), CollectionUtils.asList(renameOpFooToBar.getOutputColNames()));
  }

  public void testGetOutputSchema() throws Exception {
    RelationSchema outputSchema = renameOpFooToBar.getOutputSchema();
    assertEquals("Bar", outputSchema.getName());
    assertEquals(ImmutableMap.of("bar", Integer.class, "bar1", String.class), outputSchema.getColTypesByName());
  }
}