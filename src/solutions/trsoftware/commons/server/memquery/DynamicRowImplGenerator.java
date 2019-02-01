package solutions.trsoftware.commons.server.memquery;

import com.squareup.javapoet.*;
import net.openhft.compiler.CompilerUtils;
import solutions.trsoftware.commons.server.util.Duration;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static solutions.trsoftware.commons.server.util.ServerStringUtils.toJavaIdentifier;

/**
 * Dynamically generates a class for a {@link Row} implementation from a given {@link RelationSchema}, to use less memory
 * that {@link RowImpl}, by using primitive fields for wrapper types (rather than an {@code Object[]} for all attributes.
 *
 * @author Alex
 * @since 1/8/2019
 */
public class DynamicRowImplGenerator {

  /**
   * The generated classes will be in this package.
   */
  private static String packageName = Row.class.getPackage().getName() + ".generated";
  /**
   * The generated class name will start with this prefix
   */
  private static final String classNamePrefix = "DynamicRowImpl";

  private static final TypeVariableName V = TypeVariableName.get("V");

  private final RelationSchema schema;
  /**
   * If specified, will write generated java code to this directory (for debugging)
   */
  private Path outputDir;

  private final String classSimpleName;
  private TypeSpec.Builder classBuilder;
  private List<Class> colTypes;
  private List<FieldInfo> fields;
  private FieldSpec nullBitField;
  private MethodSpec getValueByIndexMethod;
  private MethodSpec setValueByIndexMethod;

  DynamicRowImplGenerator(RelationSchema schema) {
    this(schema, null);
  }

  DynamicRowImplGenerator(RelationSchema schema, Path outputDir) {
    this.schema = schema;
    this.outputDir = outputDir;
    String schemaName = schema.getName();
    // construct a unique name for the classed based on schema name as well as its cols (to differentiate different schemas having the same name)
    colTypes = schema.getColTypes();
    StringBuilder classNameBuilder = new StringBuilder(classNamePrefix);
    for (Class colType : colTypes) {
      String colTypeName = colType.getName();
      colTypeName = StringUtils.stripPrefix(colTypeName, "java.lang.");  // strip pkg prefix if in java.lang
      classNameBuilder.append('$').append(toJavaIdentifier(colTypeName));
    }
    // TODO: make sure that the generated class name doesn't hit the length limit of 65535 chars
    // see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.11
    classSimpleName = classNameBuilder.toString();
    classBuilder = TypeSpec.classBuilder(classSimpleName)
        .addModifiers(Modifier.PUBLIC)
        .superclass(AbstractRow.class)
        .addSuperinterface(MutableRow.class)
        .addJavadoc("Generated with {@link $T} on $L\nfrom schema $L\n", getClass(), new Date(), schema)
    ;
    addFields();
    addMethods();
  }


  public Class<? extends MutableRow> generateClass() throws RuntimeException {
    try (Duration duration = new Duration("DEBUG: DynamicRowImplGenerator.generateClass")) {
      String javaCode = generateJavaCode();
      return CompilerUtils.CACHED_COMPILER.loadFromJava(getClassName(), javaCode);
    }
    catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nonnull
  private String generateJavaCode() throws IOException {
    try (Duration duration = new Duration("DEBUG: DynamicRowImplGenerator.generateJavaCode")){
      JavaFile javaFile = buildJavaFile();
      String javaCode = javaFile.toString();
      if (outputDir != null) {
        // output the java source code (for debugging)
        javaFile.writeTo(outputDir);
        Path outFile = outputDir.resolve(javaFile.packageName.replace('.', File.separatorChar))
            .resolve(javaFile.typeSpec.name + ".java");
        System.out.printf("DEBUG: Generated class written to %s for schema %s%n", outFile, schema);
      }
      return javaCode;
    }
  }

  /**
   * @return the (simple) name of the generated class
   */
  public String getClassSimpleName() {
    return classSimpleName;
  }

  /**
   * @return the full name of the generated class
   */
  public String getClassName() {
    return packageName + "." + classSimpleName;
  }



  private void addFields() {
    fields = new ArrayList<>();
    int unwrappedCount = 0;
    for (int i = 0; i < colTypes.size(); i++) {
      Class fieldType = colTypes.get(i);
      String fieldName = "_" + i;
      int nullBitFieldIdx = -1;
      if (!fieldType.isPrimitive() && ReflectionUtils.isPrimitiveWrapper(fieldType)) {
        // if the col type is a wrapper, we want to use the corresponding primitive type for the field
        fieldType = ReflectionUtils.primitiveTypeFor(fieldType);
      }
      assert fieldType != void.class;
      if (fieldType.isPrimitive()) {
        // this is going to be a primitive field, so we'll have to check for null whenever it's accessed
        if (unwrappedCount < Long.SIZE) {
          // we support at most 64 primitive fields (to avoid having to use more than 1 null flags field)
          nullBitFieldIdx = unwrappedCount++;
        } else {
          // we can't support any additional primitive fields, so use a wrapper for this field
          fieldType = ReflectionUtils.wrapperTypeFor(fieldType);
        }
      }
      FieldInfo fieldInfo = new FieldInfo(i, fieldType, fieldName, nullBitFieldIdx);
      fields.add(fieldInfo);
      classBuilder.addField(fieldInfo.fieldSpec);
    }
    if (unwrappedCount > 0) {
      // need an additional BitSet-like field to check the unwrapped wrappers for null
      // 1) choose the smallest integer type that can hold all the bits we need
      Class bitFieldType;
      if (unwrappedCount <= Byte.SIZE)
        bitFieldType = byte.class;
      else if (unwrappedCount <= Short.SIZE)
        bitFieldType = short.class;
      else if (unwrappedCount <= Integer.SIZE)
        bitFieldType = int.class;
      else {
        assert unwrappedCount <= Long.SIZE;
        bitFieldType = long.class;
      }
      // initialize this field to all 1-bits (i.e. -1 in 2's complement binary)
      nullBitField = FieldSpec.builder(bitFieldType, "$nullFlags", Modifier.PRIVATE)
          // initialize this field to all 1-bits (i.e. -1 in 2's complement binary)
          .initializer("-1").build();
      classBuilder.addField(nullBitField);
    }

  }

  private static class FieldInfo {
    private final int idx;
    private final Class fieldType;
    private final String fieldName;
    private final int nullBitFieldIdx;
    private final FieldSpec fieldSpec;

    private FieldInfo(int idx, Class fieldType, String fieldName, int nullBitFieldIdx) {
      this.idx = idx;
      this.fieldType = fieldType;
      this.fieldName = fieldName;
      this.nullBitFieldIdx = nullBitFieldIdx;
      this.fieldSpec = FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE).build();
    }
  }

  private void addMethods() {
    // add constructor
    classBuilder.addMethod(buildConstructor());
    // add the other methods defined by the MutableRow interface
    add_getValue_methods();
    add_setValue_methods();
    classBuilder.addMethod(build_toString_method());
  }

  @Nonnull
  private MethodSpec buildConstructor() {
    return MethodSpec.constructorBuilder()
        .addParameter(RelationSchema.class, "schema")
        .addStatement("super(schema)")
        .addModifiers(Modifier.PUBLIC)
        .build();
    // TODO: verify that the given schema matches the one for which the class was generated
  }

  /**
   * Generates implementations of {@link AbstractRow#getValue(int)} and {@link AbstractRow#getValue(String)}
   */
  private void add_getValue_methods() {

    {
      /*
        public <V> V getValue(int colIndex) {
          switch (colIndex) {
            case 0: return (V)(Integer)field_0;
            case 1: return (V)(Double)field_1;
            case 2: return (V)field_2;
            // etc...
            default: throw new IllegalArgumentException("colIndex = " + colIndex);
          }
        }
      */
      ParameterSpec colIndexParam = ParameterSpec.builder(int.class, "colIndex").build();
      MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getValue")
          .addModifiers(Modifier.PUBLIC)
          .addTypeVariable(V)
          .returns(V)
          .addParameter(colIndexParam);

      CodeBlock.Builder code = CodeBlock.builder();
      code.beginControlFlow("switch ($N)", colIndexParam);

      // add a case for each col
      for (int i = 0; i < fields.size(); i++) {
        FieldInfo fieldInfo = fields.get(i);
        FieldSpec fieldSpec = fieldInfo.fieldSpec;
        code.beginControlFlow("case $L:", i);
        if (fieldSpec.type.isPrimitive()) {
          // we have to box the primitive fields before casting them to V
          if (nullBitField != null && fieldInfo.nullBitFieldIdx >= 0) {
            // and also have to check whether its value should be null (if (bitField & (1L << i)) != 0)
            code.beginControlFlow("if (($N & (1L << $L)) != 0)", nullBitField, fieldInfo.nullBitFieldIdx);
            code.addStatement("return null");
            code.endControlFlow();
          }
          code.addStatement("return ($T)($T)$N", V, fieldSpec.type.box(), fieldSpec);
        }
        else {
          code.addStatement("return ($T)$N", V, fieldSpec);
        }
        code.endControlFlow();
      }
      code.addStatement("default: throw new $T($S + $N)", IllegalArgumentException.class, colIndexParam.name + " = ", colIndexParam);
      code.endControlFlow();
      methodBuilder.addCode(code.build());
      getValueByIndexMethod = addMethodAnnotationsAndBuild(methodBuilder);
      classBuilder.addMethod(getValueByIndexMethod);
    }


    {
      MethodSpec getValueByName;
      /*
        @Override
        public <V> V getValue(String colName) {
          return getValue(getColIndex(colName));
        }
      */
      ParameterSpec colNameParam = ParameterSpec.builder(String.class, "colName").build();
      MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getValue")
          .addModifiers(Modifier.PUBLIC)
          .addTypeVariable(V)
          .returns(V)
          .addParameter(colNameParam);

      CodeBlock.Builder code = CodeBlock.builder();
      code.addStatement("return $N(getColIndex($N))", getValueByIndexMethod, colNameParam);
      methodBuilder.addCode(code.build());
      getValueByName = addMethodAnnotationsAndBuild(methodBuilder);
      classBuilder.addMethod(getValueByName);
    }
  }


  /**
   * Generates implementations of {@link MutableRow#setValue(int, Object)} and {@link MutableRow#setValue(String, Object)}
   */
  private void add_setValue_methods() {
    ParameterSpec valueParam = ParameterSpec.builder(V, "value").build();

    {
      /*
        public <V, T> T setValue(int colIndex, V value) {
          switch (colIndex) {
            case 0: field_0 = (Integer)value; break;
            case 1: field_1 = (Double)value; break;
            case 2: field_2 = (String)value; break;
            // etc...
            default: throw new IllegalArgumentException("colIndex = " + colIndex);
          }
        }
      */
      ParameterSpec colIndexParam = ParameterSpec.builder(int.class, "colIndex").build();
      MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("setValue")
          .addModifiers(Modifier.PUBLIC)
          .addTypeVariable(V)
          .addParameter(colIndexParam)
          .addParameter(valueParam);

      CodeBlock.Builder code = CodeBlock.builder();
      code.beginControlFlow("switch ($N)", colIndexParam);

      // add a case for each col
      for (int i = 0; i < fields.size(); i++) {
        FieldInfo fieldInfo = fields.get(i);
        FieldSpec fieldSpec = fieldInfo.fieldSpec;
        TypeName boxedType = fieldSpec.type.box();
        code.beginControlFlow("case $L:", i);
        if (fieldSpec.type.isPrimitive() && nullBitField != null && fieldInfo.nullBitFieldIdx >= 0) {
          // check for null if the original col type was a wrapper
          code.beginControlFlow("if ($N == null)", valueParam); {
            // set the null bit for this field, but don't actually assign null to the field (it's primitive)
            code.addStatement("$N |= ($T)1 << $L", nullBitField, nullBitField.type, fieldInfo.nullBitFieldIdx);
          }
          code.nextControlFlow("else"); {
            // clear the null bit for this field and assign the given value
            code.addStatement("$N &= ~(($T)1 << $L)", nullBitField, nullBitField.type, fieldInfo.nullBitFieldIdx);
            code.addStatement("$N = ($T)$N", fieldSpec, boxedType, valueParam);
          }
          code.endControlFlow();
        }
        else {
          code.addStatement("$N = ($T)$N", fieldSpec, boxedType, valueParam);
        }
        code.addStatement("break");
        code.endControlFlow();
      }
      code.addStatement("default: throw new $T($S + $N)", IllegalArgumentException.class, colIndexParam.name + " = ", colIndexParam);
      code.endControlFlow();
      methodBuilder.addCode(code.build());
      setValueByIndexMethod = addMethodAnnotationsAndBuild(methodBuilder);
      classBuilder.addMethod(setValueByIndexMethod);
    }


    {
      MethodSpec setValueByName;
      /*
        @Override
        public <V> void setValue(String colName, V value) {
          setValue(getColIndex(colName), value);
        }
      */
      ParameterSpec colNameParam = ParameterSpec.builder(String.class, "colName").build();
      MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("setValue")
          .addModifiers(Modifier.PUBLIC)
          .addTypeVariable(V)
          .addParameter(colNameParam)
          .addParameter(valueParam);

      CodeBlock.Builder code = CodeBlock.builder();
      code.addStatement("$N(getColIndex($N), $N)", setValueByIndexMethod, colNameParam, valueParam);
      methodBuilder.addCode(code.build());
      setValueByName = addMethodAnnotationsAndBuild(methodBuilder);
      classBuilder.addMethod(setValueByName);
    }
  }

  @Nonnull
  private MethodSpec addMethodAnnotationsAndBuild(MethodSpec.Builder methodBuilder) {
    return addMethodAnnotations(methodBuilder).build();
  }

  /**
   * Generates an implementation of {@link Object#toString()}
   */
  @Nonnull
  private MethodSpec build_toString_method() {
    // public String toString()

    CodeBlock.Builder code = CodeBlock.builder();
    // NOTE: can't use Guava's ToStringHelper because for some reason that makes the OpenHFT compiler attempt to recompile Guava
    // StringBuilder out = new StringBuilder("DynamicRowImpl{");
    code.addStatement("$1T out = new $1T($2S)", StringBuilder.class, classNamePrefix + "{");
    // List<String> colNames = getSchema().getColNames();
    code.addStatement("$T<String> colNames = getSchema().getColNames()", List.class);
    // add each field in a for-loop
    code.beginControlFlow("for (int i = 0; i < $L; i++)", fields.size()); {
      /*
      out.append(colNames.get(i)).append('=');
      Object value = getValue(i);
      if (value != null && value.getClass().isArray()) {
        Object[] objectArray = {value};
        String arrayString = Arrays.deepToString(objectArray);
        out.append(arrayString, 1, arrayString.length() - 1);
      } else {
        out.append(value);
      }
      if (i < 2)
        out.append(", ");
      */
      code.addStatement("out.append(colNames.get(i)).append('=')");
      code.addStatement("Object value = getValue(i)");
      code.beginControlFlow("if (value != null && value.getClass().isArray())"); {
        code.addStatement("Object[] objectArray = {value}");
        code.addStatement("String arrayString = $T.deepToString(objectArray)", Arrays.class);  // String arrayString = Arrays.deepToString(objectArray);
        code.addStatement("out.append(arrayString, 1, arrayString.length() - 1)");
      } code.nextControlFlow("else"); {
        code.addStatement("out.append(value)");
      }
      code.endControlFlow();
      code.beginControlFlow("if (i < $L)", fields.size()-1); {
        code.addStatement("out.append(\", \")");  // out.append(", ");
      }
      code.endControlFlow();
      code.addStatement("");
    }
    code.endControlFlow();
    code.addStatement("return out.append('}').toString()");

    return MethodSpec.methodBuilder("toString")
        .addModifiers(Modifier.PUBLIC)
        .returns(String.class)
        .addAnnotation(Override.class)
        .addCode(code.build())
        .build();
  }

  /**
   * Adds @{@link Override} and @{@link SuppressWarnings} annotations to the given method.
   */
  private MethodSpec.Builder addMethodAnnotations(MethodSpec.Builder methodBuilder) {
    methodBuilder.addAnnotation(Override.class);
    methodBuilder.addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
        .addMember("value", "$S", "unchecked").build());
    return methodBuilder;
  }

  JavaFile buildJavaFile() {
    return JavaFile.builder(packageName, classBuilder.build())
        .skipJavaLangImports(true)
        .build();
  }

}
