package solutions.trsoftware.tools.javac;

import com.sun.source.doctree.DocCommentTree;
import solutions.trsoftware.commons.server.io.file.FileUtils;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;

import javax.annotation.processing.Processor;
import javax.tools.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utilities for working with the Java Compiler API.
 *
 * @see <a href="https://www.javacodegeeks.com/2015/09/java-compiler-api.html">Java Compiler API tutorial</a>
 * @author Alex
 * @since 5/12/2019
 */
public class JavacUtils {

  /**
   * Uses the {@link JavaCompiler} tool in conjunction with the
   * <a href="https://docs.oracle.com/javase/8/docs/jdk/api/javac/tree/">Java Compiler Tree API</a>
   * to obtain the Javadoc comments of the fields in the given class.
   *
   * @param javaClass will collect doc comments for fields of this class.
   * @return a map of doc comments by field name; will not contain entries for fields without doc comments
   * @throws IOException
   *
   * @see FieldDocTreeProcessor
   * @see <a href="https://www.javacodegeeks.com/2015/09/java-compiler-api.html">Java Compiler API tutorial</a>
   */
  public static Map<String, DocCommentTree> getJavadocCommentsForFields(Class javaClass) throws IOException {
    Path sourceFile = ReflectionUtils.getSourceFile(javaClass);
    if (!Files.exists(sourceFile)) {
      throw new FileNotFoundException(String.format("Unable to locate source file of %s (expected %s)",
          javaClass, sourceFile.toString()));
    }
    FieldDocTreeProcessor processor = new FieldDocTreeProcessor(javaClass);
    compileWithAnnotationProcessors(Collections.singletonList(processor), null, null,
        new DiagnosticCollector<JavaFileObject>(), sourceFile.toString());
    return processor.getJavadocByFieldName();
  }

  /**
   * Runs the {@link JavaCompiler} tool on the given files, using the given annotation processors.
   *
   * @param processors processors (for annotation processing)
   * @param out a Writer for additional output from the compiler; will use {@code System.err} if {@code null}
   * @param outputDir the given files will be compiled to this directory; if {@code null}, will use a system temp
   * directory that will be deleted after the JVM process exits.
   * @param diagnostics pass an instance of {@link DiagnosticCollector} to collect the compilation diagnostics
   * (e.g. compilation errors); if {@code null}, will use the compiler's default method for reporting diagnostics
   * @param filenames the Java files to compile
   * @return true if and only all the files compiled without errors; false otherwise
   * @see <a href="https://www.javacodegeeks.com/2015/09/java-compiler-api.html">Java Compiler API tutorial</a>
   */
  public static boolean compileWithAnnotationProcessors(
      Iterable<? extends Processor> processors, Writer out, Path outputDir, DiagnosticListener<JavaFileObject> diagnostics, String... filenames) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
    Iterable<? extends JavaFileObject> sources = fileManager.getJavaFileObjects(filenames);
    if (outputDir == null) {
      // use a temp dir for the compiler output
      /*
       TODO: perhaps use an in-memory file system instead?
       @see https://stackoverflow.com/questions/30394737/in-memory-file-system-in-java
       @see https://github.com/google/jimfs
       * might need to use a com.sun.tools.javac.nio.PathFileManager instead of javax.tools.StandardJavaFileManager
         to get these libs to work with the JavaCompiler tool (or upgrade to Java 9+, which provides the
         StandardJavaFileManager.getJavaFileObjects​(Path... paths) methods
       */
      outputDir = FileUtils.deleteOnExit(Files.createTempDirectory(JavacUtils.class.getName()));
    }
    List<String> compilerOptions = Arrays.asList(
        "-d",  // output directory (see https://stackoverflow.com/questions/9665768/javacompiler-set-the-compiled-class-output-folder)
        outputDir.toString()
    );

    JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics,
        compilerOptions, null, sources);
    task.setProcessors(processors);
    System.out.printf("Compiling %s%n  to %s%n  using annotation processors %s%n",
        Arrays.toString(filenames), outputDir, processors);
    return task.call();
  }
}
