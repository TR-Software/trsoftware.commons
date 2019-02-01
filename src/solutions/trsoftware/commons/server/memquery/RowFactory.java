package solutions.trsoftware.commons.server.memquery;

import solutions.trsoftware.commons.server.memquery.schema.ColSpec;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates new instances of {@link MutableRow} on demand.  If the system property {@value #SYS_PROP_USE_DYNAMIC_CLASSES}
 * is set to {@code "true"}, will use a new instance of a class generated with {@link DynamicRowImplGenerator}, which
 * might offer some memory savings compared with {@link RowImpl}.
 * <p>
 * The memory savings offered by using {@link DynamicRowImplGenerator} come at the expense of runtime compilation of
 * the required dynamic classes, hence the choice depends on the structure and quantity of data being processed.
 * A simple experiment produced the following results:
 * <ol>
 * <li>Using classes dynamically generated with {@link DynamicRowImplGenerator}:
 * <pre>
 *   Summary for ScoresForUniverseMemQuery for GameResults between [Mon Jan 01 00:00:00 EST 2018] and [Thu Feb 01 00:00:00 EST 2018]:
 *          Duration: 03:18
 *      JVM CPU Load: min=     2%  mean=    18%  max=    61%  stdev=    12%  (1,977 samples)
 *         Heap Used: min=    5.6 MB  mean=  551.7 MB  max=2,125.4 MB  stdev=  494.3 MB  (1,977 samples)
 *    Heap Committed: min=  384.0 MB  mean=  925.2 MB  max=2,452.0 MB  stdev=  764.7 MB  (1,977 samples)
 * </pre></li>
 * <li>Using {@link RowImpl}:
 * <pre>
 *   Summary for ScoresForUniverseMemQuery for GameResults between [Mon Jan 01 00:00:00 EST 2018] and [Thu Feb 01 00:00:00 EST 2018]:
 *          Duration: 02:22
 *      JVM CPU Load: min=     2%  mean=    20%  max=    70%  stdev=    10%  (1,422 samples)
 *         Heap Used: min=    5.6 MB  mean=  555.7 MB  max=2,676.9 MB  stdev=  558.6 MB  (1,423 samples)
 *    Heap Committed: min=  384.0 MB  mean=1,178.1 MB  max=3,772.0 MB  stdev=1,254.1 MB  (1,423 samples)
 * </pre></li>
 * </ol>
 * @see #newRow(RelationSchema)
 * @author Alex
 * @since 1/10/2019
 */
public class RowFactory {

  /**
   * System property used to set the value of {@link #useDynamicClasses}
   */
  public static final String SYS_PROP_USE_DYNAMIC_CLASSES = "memquery.useDynamicClasses";
  /**
   * System property used to set the value of {@link #debug}
   */
  public static final String SYS_PROP_DEBUG = "memquery.debug";

  private static RowFactory instance;

  public static RowFactory getInstance() {
    if (instance == null) {
      synchronized (RowFactory.class) {
        if (instance == null) {
          instance = new RowFactory(Boolean.getBoolean(SYS_PROP_USE_DYNAMIC_CLASSES), Boolean.getBoolean(SYS_PROP_DEBUG));
        }
      }
    }
    return instance;
  }

  /**
   * Exposed for unit testing.
   */
  static synchronized void setInstance(RowFactory instance) {
    RowFactory.instance = instance;
  }

  /**
   * If {@code true}, {@link #newRow(RelationSchema)} will create an instance of a class dynamically generated
   * with {@link DynamicRowImplGenerator} (specially tailored to the given schema to avoid storing wrappers for primitives);
   * otherwise will create an instance of {@link RowImpl}
   */
  private final boolean useDynamicClasses;

  /**
   * If {@code true}, dynamically generated classes will be written to {@link #outputDir} prior to being compiled.
   */
  private final boolean debug;

  /**
   * If specified, will write generated java code to this directory (for debugging)
   */
  private Path outputDir;

  /**
   * Caches the generated classes; the keys of this map are results of {@link RelationSchema#getColTypes()},
   * and its values are results of {@link DynamicRowImplGenerator#generateClass()} for that schema.
   */
  private Map<List<Class>, Class<? extends MutableRow>> generatedClasses;

  /**
   * Exposed for unit testing.
   * @param useDynamicClasses if {@code true}, {@link #newRow(RelationSchema)} will create an instance of a class
   *   dynamically generated with {@link DynamicRowImplGenerator}, otherwise will create a new instance of {@link RowImpl}
   * @param debug if {@code true}, the source code of dynamically generated implementations will be written to
   *   a temp directory prior to being compiled.
   */
  RowFactory(boolean useDynamicClasses, boolean debug) {
    this.useDynamicClasses = useDynamicClasses;
    this.debug = debug;
    if (useDynamicClasses)
      generatedClasses = new ConcurrentHashMap<>();
    if (debug) {
      try {
        outputDir = Files.createTempDirectory(getClass().getSimpleName());
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public boolean isUseDynamicClasses() {
    return useDynamicClasses;
  }

  public boolean isDebug() {
    return debug;
  }

  /**
   * Factory method for creating new row instances using a dynamically-generated {@link MutableRow} class
   * optimized for the given schema.
   *
   * @return a new instance of a {@link MutableRow} class dynamically generated from the given schema if
   * {@link #useDynamicClasses} is set, otherwise a new instance of {@link RowImpl}.
   *
   * @throws RuntimeException if {@link DynamicRowImplGenerator#generateClass()} or {@link Class#newInstance()} threw an exception
   */
  public MutableRow newRow(RelationSchema schema) throws RuntimeException {
    if (useDynamicClasses) {
      List<Class> colTypes = schema.getColTypes();
      Class<? extends MutableRow> cls = generatedClasses.computeIfAbsent(colTypes, k ->
          new DynamicRowImplGenerator(schema, outputDir).generateClass());
      try {
        return cls.getConstructor(RelationSchema.class).newInstance(schema);
      }
      catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
    else {
      return new RowImpl(schema);
    }
  }

  /**
   * Factory method for creating a new row instance representing the given row transformed by the given schema.
   */
  public MutableRow transformRow(RelationSchema outputSchema, Row inputRow) {
    MutableRow outputRow = newRow(outputSchema);
    for (ColSpec outputCol : outputSchema) {
      String name = outputCol.getName();
      outputRow.setValue(name, outputCol.getValue(inputRow));
    }
    return outputRow;
  }
}
