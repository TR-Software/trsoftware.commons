package solutions.trsoftware.commons.server.util.persistence;

import com.google.gson.GsonBuilder;
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.server.io.file.FileUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Alex
 * @since 4/23/2019
 */
public class ObjectWithPersistentSettingsTest extends TestCase {

  private Path tempDir;

  public void setUp() throws Exception {
    super.setUp();
    tempDir = FileUtils.deleteOnExit(Files.createTempDirectory(getClass().getSimpleName()));
  }

  public void tearDown() throws Exception {
    FileUtils.deleteFileTree(tempDir);
    tempDir = null;
    super.tearDown();
  }

  public void testFileBackedPersistence() throws Exception {
    Foo foo = new Foo(true, tempDir.toFile());
    assertNull(foo.getStringField());
    assertNull(foo.getIntArrField());
    // settings should not have been written to a file yet
    assertTrue(FileUtils.isEmpty(tempDir));
    // but they should be written to a file if we call a setter method on foo
    String strVal = "foo";
    foo.setStringField(strVal);
    assertNull(foo.getIntArrField());
    printFiles(1, System.out);  // TODO: verify the JSON contents of this file?
    // creating a new instance backed by the same dir should load the settings file saved above
    Foo foo2 = new Foo(true, tempDir.toFile());
    assertEquals(strVal, foo2.getStringField());
    assertNull(foo2.getIntArrField());
    // modify the settings again via the first object
    int[] arrVal = {1, 2, 3};
    foo.setIntArrField(arrVal);
    printFiles(1, System.out);  // TODO: verify the JSON contents of this file?
    assertTrue(Arrays.equals(arrVal, foo.getIntArrField()));
    /*
    NOTE: in the original implementation, the Proxies for both objects point to the same target Settings instance,
    hence modifications made via foo will propagate to foo2.
    TODO: is this really what we want? i.e. do we want to assert this behavior?
    */
    assertTrue(Arrays.equals(foo.getIntArrField(), foo2.getIntArrField()));
    // creating yet another instance should load all of the changes made above
    Foo foo3 = new Foo(true, tempDir.toFile());
    assertEquals(strVal, foo3.getStringField());
    assertEquals(arrVal, foo3.getIntArrField());
  }

  private void printFiles(int expectedCount, PrintStream out) throws IOException {
    List<Path> files = FileUtils.listFiles(tempDir);
    assertEquals(expectedCount, files.size());
    for (Path file : files) {
      assertTrue(Files.isRegularFile(file));
      String filename = file.toString();
      String border = StringUtils.repeat('=', Math.max(40, filename.length()));
      out.println(border);
      out.println(filename);
      out.println(border);
      try (InputStream in = Files.newInputStream(file)) {
        ServerIOUtils.copyInputToOutput(in, out);
      }
      out.println();
      out.println(border);
    }

  }

  /**
   * Uses a {@link SettingsLoaderStub} to test re-loading the settings instance.
   */
  public void testReloadingSettings() throws Exception {
    String strVal = "x1";
    SettingsLoaderStub settingsDAO = new SettingsLoaderStub(new Foo.SettingsImpl(strVal, null));
    Foo foo = new Foo(settingsDAO);
    assertTrue(foo.settings instanceof Proxy);
    // these methods should delegate to the SettingsImpl loaded by our DAO stub
    assertEquals(strVal, foo.getStringField());
    assertNull(foo.getIntArrField());
    // the DAO should not have been used to persist any changes yet
    assertEquals(0, settingsDAO.savedStates.size());
    // now invoke a setter and make sure the DAO was invoked to persist the changes
    int[] arrVal = {1};
    foo.setIntArrField(arrVal);
    assertEquals(1, settingsDAO.savedStates.size());
    settingsDAO.assertLastSavedStateEquals(new Foo.SettingsImpl(strVal, arrVal));
    // test reloading the settings from the DAO
    String strVal2 = "x2";
    int[] arrVal2 = {1, 2};
    settingsDAO.toLoad = new Foo.SettingsImpl(strVal2, arrVal2);
    foo.reloadSettings();
    assertEquals(strVal2, foo.getStringField());
    assertEquals(arrVal2, foo.getIntArrField());
    // make sure persistence still works
    String strVal3 = "x3";
    foo.setStringField(strVal3);
    settingsDAO.assertLastSavedStateEquals(new Foo.SettingsImpl(strVal3, arrVal2));
    fail("TODO"); // TODO
  }

  static class Foo extends ObjectWithPersistentSettings<Foo.Settings> {

    interface Settings {
      String getStringField();

      void setStringField(String stringField);

      int[] getIntArrField();

      void setIntArrField(int[] intArrField);
    }

    protected static class SettingsImpl implements Settings {
      private String stringField;
      private int[] intArrField;

      public SettingsImpl() {
      }

      public SettingsImpl(String stringField, int[] intArrField) {
        this.stringField = stringField;
        this.intArrField = intArrField;
      }

      @Override
      public String getStringField() {
        return stringField;
      }

      @Override
      public void setStringField(String stringField) {
        this.stringField = stringField;
      }

      @Override
      public int[] getIntArrField() {
        return intArrField;
      }

      @Override
      public void setIntArrField(int[] intArrField) {
        this.intArrField = intArrField;
      }
    }

    public Foo(boolean persistChanges, File outputDir) {
      this(persistChanges, outputDir, new SettingsImpl());
    }

    public Foo(boolean persistChanges, File outputDir, SettingsImpl defaultSettings) {
      super(Settings.class, defaultSettings, persistChanges, outputDir);
    }

    public Foo(PersistentObjectDAO<Settings> settingsDAO) {
      super(Settings.class, new SettingsImpl(), true, settingsDAO);
    }

    public String getStringField() {
      return settings.getStringField();
    }

    public void setStringField(String stringField) {
      settings.setStringField(stringField);
    }

    public int[] getIntArrField() {
      return settings.getIntArrField();
    }

    public void setIntArrField(int[] intArrField) {
      settings.setIntArrField(intArrField);
    }
  }

  static class SettingsLoaderStub implements PersistentObjectDAO<Foo.Settings> {
    private Foo.Settings toLoad;

    private LinkedList<String> savedStates = new LinkedList<>();

    private GsonSerializer<Foo.Settings> serializer = new GsonSerializer<Foo.Settings>(Foo.SettingsImpl.class) {
      @Override
      protected void configureGson(GsonBuilder gsonBuilder) {
        super.configureGson(gsonBuilder);
//        gsonBuilder.setPrettyPrinting();
      }
    };

    public SettingsLoaderStub() {
    }

    public SettingsLoaderStub(Foo.Settings toLoad) {
      this.toLoad = toLoad;
    }

    @Override
    public void persist(Foo.Settings entity) throws IOException {
      savedStates.add(serializer.toJson(entity));
      System.out.println("savedStates = " + savedStates);
    }

    @Nullable
    @Override
    public Foo.Settings load() throws IOException {
      return toLoad;
    }

    public Foo.Settings getToLoad() {
      return toLoad;
    }

    public void setToLoad(Foo.Settings toLoad) {
      this.toLoad = toLoad;
    }

    public void assertLastSavedStateEquals(Foo.Settings expected) {
      assertFalse(savedStates.isEmpty());
      assertEquals(serializer.toJson(expected), savedStates.getLast());
    }
  }
}