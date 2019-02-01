package solutions.trsoftware.tools.codegen;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeVariableName;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.compare.ComparisonOperator;

import java.io.PrintStream;

/**
 * Generates rich comparison methods using the definitions in {@link ComparisonOperator}.
 * <p>
 * Example of a method that would be generated when invoked with command line args {@code "public"} and {@code "synchronized"}:
 * <pre>{@code
 *   public synchronized boolean greaterThan(T o) {
 *     return GT.compare(this, o);
 *   }
 * }</pre>
 *
 * @author Alex
 * @since 1/10/2019
 */
public class RichComparisonMethodsGenerator {

  /**
   * Will be prepended to the declaration of each generated method.
   */
  private String modifiers;

  /**
   * The type of the arg for the generated method.
   */
  private String argType = "T";

  public RichComparisonMethodsGenerator(String modifiers) {
    this.modifiers = modifiers;
  }

  public RichComparisonMethodsGenerator(String modifiers, String argType) {
    this.modifiers = modifiers;
    this.argType = argType;
  }

  /**
   * Generates rich comparison methods using the definitions in {@link ComparisonOperator}.
   * <p>
   * Example (with {@link #modifiers} = {@code "public synchronized"} and {@link #argType} = {@code "T"}):
   * <pre>{@code
   *   public synchronized boolean greaterThan(T o) {
   *     return GT.compare(this, o);
   *   }
   * }</pre>
   *
   * @author Alex
   * @since 1/10/2019
   */
  public void printMethods(PrintStream out) {
    for (ComparisonOperator op : ComparisonOperator.values()) {
      String prettyName = op.prettyName();
      String methodName = StringUtils.toCamelCase(prettyName, " ");
      ParameterSpec inputParam = ParameterSpec.builder(TypeVariableName.get(argType), "o").build();
      // TODO: argType isn't necessarily going to be a TypeVariableName
      MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
          .addJavadoc("A \"rich\" comparison method using the result of {@link Comparable#compareTo(Object)}\n\n" +
              "@return {@code true} iff this instance is $L the given arg, in accordance with the\n" +
              "<i>natural ordering</i> imposed by this {@link Comparable}.\n", prettyName)
          .addParameter(inputParam)
          .addModifiers(ReflectionUtils.parseModifiers(modifiers))
          .returns(boolean.class);
      // generate the method body
      CodeBlock.Builder code = CodeBlock.builder()
          .addStatement("return $T.$L.compare(this, $N)", ComparisonOperator.class, op.name(), inputParam);
      methodBuilder.addCode(code.build());
      out.println(methodBuilder.build());
    }
  }

  public static void main(String[] args) {
    // get the desired modifiers from the command line
    String modifiers = "default";  // default method of an interface
    if (args.length > 0) {
      modifiers = StringUtils.join(" ", args);
    }
    new RichComparisonMethodsGenerator(modifiers).printMethods(System.out);
  }

}
