package solutions.trsoftware.commons.server.memquery;

import com.squareup.javapoet.JavaFile;
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.memquery.schema.NameAccessorColSpec;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;
import solutions.trsoftware.commons.shared.annotations.Slow;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author Alex
 * @since 1/8/2019
 */
public class DynamicRowImplGeneratorTest extends TestCase {

  private RelationSchema schema;

  // TODO: temp: outputting the file to this directory (for manual testing)
//  private boolean writeToFile = false;
  private boolean writeToFile = true;
  private DynamicRowImplGenerator generator;

  public void setUp() throws Exception {
    super.setUp();
    schema = createSchema();
    generator = new DynamicRowImplGenerator(schema);
    createSchema();
  }

  private RelationSchema createSchema() {
    return new RelationSchema(getClass().getSimpleName(), Arrays.asList(
        new NameAccessorColSpec<>("foo", int.class),
        new NameAccessorColSpec<>("bar", Double.class),
        new NameAccessorColSpec<>("str", String.class)
    ));
  }

  @Slow
  public void testGenerateClass() throws Exception {
    Class<? extends MutableRow> cls = generator.generateClass();
    MutableRow row = cls.getConstructor(RelationSchema.class).newInstance(schema);
    // all attrs should be null until we set some values
    List<String> colNames = schema.getColNames();
    for (int i = 0; i < colNames.size(); i++) {
      String attr = colNames.get(i);
      assertNull(row.getValue(attr));
      assertNull(row.getValue(i));
    }
    System.out.println(row);
    assertEquals("DynamicRowImpl{foo=null, bar=null, str=null}", row.toString());
    // set some values
    row.setValue(0, 1);
    assertEquals((Object)1, row.getValue(0));
    assertEquals((Object)1, row.getValue("foo"));
    System.out.println(row);
    row.setValue("foo", 2);
    assertEquals((Object)2, row.getValue(0));
    assertEquals((Object)2, row.getValue("foo"));
    assertEquals("DynamicRowImpl{foo=2, bar=null, str=null}", row.toString());
    // test setting primitive value back to null
    row.setValue(0, null);
    assertNull(row.getValue("foo"));
    assertEquals("DynamicRowImpl{foo=null, bar=null, str=null}", row.toString());
    row.setValue("str", "Hello");
    System.out.println(row);
  }


  public void testBuildJavaFile() throws Exception {
    JavaFile javaFile = generator.buildJavaFile();
    System.out.println(javaFile.toString());
    Path sourceRoot = ReflectionUtils.getSourceRoot(getClass());
    // TODO: test some assertions
    if (writeToFile) {
      Path outDir = Files.createDirectories(
          sourceRoot.resolve(javaFile.packageName.replace('.', File.separatorChar)));
      Path outFile = outDir.resolve(
          javaFile.typeSpec.name + ".java");
      System.out.println("Writing generated java file to " + outFile);
      try (BufferedWriter writer = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)) {
        javaFile.writeTo(writer);
      }
    }
  }


}