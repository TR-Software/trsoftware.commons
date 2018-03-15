package solutions.trsoftware.tools.gwt.gen;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ResourcePrototype;
import com.squareup.javapoet.*;
import solutions.trsoftware.commons.server.io.FileSet;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;
import solutions.trsoftware.commons.shared.util.LogicUtils;
import solutions.trsoftware.commons.shared.util.MutableLazyReference;
import solutions.trsoftware.commons.shared.util.trees.BSTNode;
import solutions.trsoftware.commons.shared.util.trees.BalancedBinarySearchTree;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates Java source code for a {@link ClientBundle} interface, containing all the resource files in a given package.
 * Uses the <a href="https://github.com/square/javapoet">JavaPoet API</a> for code generation.
 *
 * <p>Usage: call {@link #generateBundleClass(Writer)} after instantiating this class with the required parameters.</p>
 * 
 * <p>For now, supports only SVG resources which map to {@link DataResource}, but other resource types maybe added in the future.</p>
 *
 * @since 2/12/2018
 * @author Alex
 * @see <a href="https://github.com/square/javapoet">JavaPoet API</a>
 */
public class ClientBundleCodeGenerator {
  private final String packageName;
  private final String bundleName;
  private final String resourceFilenamePattern;
  private boolean generateLookupTable;
  private final File packageDir;
  /** Optional javadoc string to add to the generated interface) */
  private String javadoc;

  private final ClassName bundleClassName;
  private FieldSpec factoryField;
  private final TypeSpec.Builder bundleClassBuilder;
  private final File outputFile;
  private final FileSet resourceFiles;
  private final Map<String, ResourceMethod> resourceMethodsByFilenamePrefix = new LinkedHashMap<>();
  private final Multimap<Class<? extends ResourcePrototype>, ResourceMethod> resourceMethodsByType = LinkedHashMultimap.create();

  public static void main(String[] args) {
    // TODO: impl invocation via command line
    throw new UnsupportedOperationException();
  }

  /**
   * @param srcPath the FS path of the base directory in the module's source tree (where the root packages are located)
   * @param packageName the FQ name of the package that contains the resource files (this will also be the package
   * of the generated interface)
   * @param bundleName the name of the generated interface that extends {@link ClientBundle}
   * @param resourceFilenamePattern regex that matches the resource filenames to be included
   * @param generateLookupTable pass {@code true} to generate an inner class that allows invoking bundle methods by filename.
   */
  public ClientBundleCodeGenerator(String srcPath, String packageName, String bundleName, String resourceFilenamePattern, boolean generateLookupTable) {
    this.packageName = packageName;
    this.bundleName = bundleName;
    this.resourceFilenamePattern = resourceFilenamePattern;
    this.generateLookupTable = generateLookupTable;
    packageDir = new File(srcPath + File.separatorChar + packageName.replace('.', File.separatorChar));
    assert this.packageDir.exists();
    assert this.packageDir.isDirectory();
    outputFile = new File(this.packageDir, bundleName + ".java");
    resourceFiles = new FileSet(this.packageDir, new FileSet.FilenameMatcher(resourceFilenamePattern));
    bundleClassName = ClassName.get(packageName, bundleName);
    factoryField = generateFactory();
    bundleClassBuilder = TypeSpec.interfaceBuilder(bundleClassName)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(ClientBundle.class)
        .addField(factoryField);
  }

  public String getJavadoc() {
    return javadoc;
  }

  public void setJavadoc(String javadoc) {
    this.javadoc = javadoc;
  }

  /**
   * Writes the bundle code to the given output device.  Call {@link #getOutputFile()} for the suggested output
   * target.
   */
  public void generateBundleClass(Writer out) throws IOException, NoSuchMethodException {
    for (File resourceFile : resourceFiles) {
      String filename = resourceFile.getName();
      String filenamePrefix = ServerIOUtils.filenamePrefix(filename);
      if (resourceMethodsByFilenamePrefix.containsKey(filenamePrefix))
        throw new IllegalStateException(String.format("Already have a method for '%s': %s", filenamePrefix, resourceMethodsByFilenamePrefix.get(filenamePrefix)));
      ResourceMethod resourceMethod = generateResourceMethod(filename, filenamePrefix);
      bundleClassBuilder.addMethod(resourceMethod.methodSpec);
      resourceMethodsByFilenamePrefix.put(filenamePrefix, resourceMethod);
      resourceMethodsByType.put(resourceMethod.resourceType, resourceMethod);
    }
    generateFactory();

    StackTraceElement[] stackTrace = new Exception().getStackTrace();
    if (javadoc != null)
      bundleClassBuilder.addJavadoc(javadoc);
    bundleClassBuilder.addJavadoc("<p>\n" +
        "  Use the {@link #$N} field to instantiate this bundle on-demand with {@link GWT#create},\n" +
        "  or to provide a different implementation.\n" +
        "</p>\n", factoryField.name);
    if (generateLookupTable) {
      TypeSpec lookupClass = generateLookupTable();
      bundleClassBuilder.addType(lookupClass);
      bundleClassBuilder.addJavadoc("<p>\n" +
          "  Use the nested {@link $N} class to invoke a method by the prefix of its resource filename.\n" +
          "</p>\n", lookupClass.name);
    }
    bundleClassBuilder.addJavadoc("\n<p style=\"font-style: italic; font-size: .9em;\">\n" +
            "  Generated on $L with {@link $T}\n" +
            "  (invoked from {@link $L})\n" +
            "</p>"
        , new Date(), getClass(), stackTrace[1].getClassName());


    JavaFile javaFile = JavaFile.builder(packageName, bundleClassBuilder.build())
        .build();

    javaFile.writeTo(out);
  }

  /**
   * Generate a factory for obtaining an instance of the bundle; we want to use a nested factory
   * (instead of an {@code INSTANCE} field that calls {@link GWT#create(Class)}), because we want to
   * defer injecting all these resources until they're actually needed (this also allows dead code elimination).
   * @return a field named {@code FACTORY} (of type {@link MutableLazyReference}) to be used as the factory.
  */
  private FieldSpec generateFactory() {
    ParameterizedTypeName factorySuperclass = ParameterizedTypeName.get(
        ClassName.get(MutableLazyReference.class), bundleClassName);
    TypeSpec factoryAnonClass = TypeSpec.anonymousClassBuilder("")
        .addSuperinterface(factorySuperclass)
        .addMethod(MethodSpec.methodBuilder("create")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PROTECTED)
            .returns(bundleClassName)
            .addStatement("return $T.create($N.class)", TypeName.get(GWT.class), bundleName)
            .build())
        .build();
    return FieldSpec.builder(factorySuperclass, "FACTORY")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .initializer("$L", factoryAnonClass)
        .addJavadoc("Instantiates this bundle on-demand with {@link GWT#create}.\n\n" +
            "Can call {@link MutableLazyReference#set(Object) set($T)} to provide a different implementation.\n",
            bundleClassName)
        .build();
  }

  /**
   * Generates an inner class for invoking bundle methods by filename prefix.
   * TODO: make this optional
   */
  private TypeSpec generateLookupTable() {
    CodeBlock.Builder javadocBuilder = CodeBlock.builder().add(
        "<p>\n  Provides resource lookup by filename prefix, which is useful because "
            + "GWT doesn't support reflection-based method invocation.\n</p>\n\n"
            + "<p>Contains a method for each resource type defined in the bundle.</p>\n\n"
    );
    TypeSpec.Builder lookupClassBuilder = TypeSpec.classBuilder("Lookup")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    for (Class<? extends ResourcePrototype> resourceType : resourceMethodsByType.keySet()) {
      // build a BST of code->method entries
      BalancedBinarySearchTree<String, ResourceMethod> bst = new BalancedBinarySearchTree<>();
      for (ResourceMethod resourceMethod : resourceMethodsByType.get(resourceType)) {
        bst.put(resourceMethod.filenamePrefix, resourceMethod);
      }
      // generate if-stmts that represent lookups in the BST
      CodeBlock.Builder methodCodeBuilder = CodeBlock.builder();
      LookupBuilder lookupBuilder = new LookupBuilder(methodCodeBuilder);
      lookupBuilder.visit(bst.getRoot());
      MethodSpec lookupMethod = MethodSpec.methodBuilder("get" + resourceType.getSimpleName())
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
          .returns(DataResource.class)
          .addParameter(String.class, "name")
          .addCode(methodCodeBuilder.build())
          .build();
      lookupClassBuilder.addMethod(lookupMethod);
      javadocBuilder.add("@see #$N\n", lookupMethod);
    }
    lookupClassBuilder.addJavadoc(javadocBuilder.build());
    return lookupClassBuilder.build();
  }

  private ResourceMethod generateResourceMethod(String filename, String filenamePrefix) {
    // make sure the method name is a valid Java identifier (otherwise append "_")
    String methodName = filenamePrefix;
    while (ReflectionUtils.isJavaKeyword(methodName))
      methodName += "_";
    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .addAnnotation(AnnotationSpec.builder(ClientBundle.Source.class)
            .addMember("value", "$1S", filename)
            .build()
        );
    String filenameExtension = ServerIOUtils.filenameExtension(filename);
    Class<? extends ResourcePrototype> resourceType;
    switch (filenameExtension) {
      case "svg":
        resourceType = DataResource.class;
        methodBuilder
            .addAnnotation(AnnotationSpec.builder(DataResource.MimeType.class)
                .addMember("value", "$1S", "image/svg+xml")
                .build())
            .returns(resourceType);
        break;
      default:
        // TODO: add support for other resource types
        throw new UnsupportedOperationException(String.format("%s doesn't support '%s' files", getClass().getSimpleName(), filenameExtension));
    }
    return new ResourceMethod(filename, filenamePrefix, filenameExtension, resourceType, methodBuilder.build());
  }

  /**
   * @return the file where the output <i>should</i> be written. The caller can decide whether to actually pass
   * a {@link Writer} for that file to {@link #generateBundleClass(Writer)}. This is optional because the caller
   * might want to simply print the output to {@link System#out}.
   */
  public File getOutputFile() {
    return outputFile;
  }


  private class LookupBuilder {
    private CodeBlock.Builder out;

    public LookupBuilder(CodeBlock.Builder out) {
      this.out = out;
    }

    public void visit(BSTNode<String, ResourceMethod> node) {
      // begin a new code block (so that we can reuse the decl "int cmp")
      String key = node.getKey();
      String cmpVarName = "cmp_" + key;
      out.addStatement("int $N = name.compareTo($S)", cmpVarName, key);
      out.beginControlFlow("if ($N == 0)", cmpVarName);
      // key found, obtain the instance from the factory field, and invoke the corresponding method on it
      out.addStatement("return $N.get().$N()", factoryField, node.getValue().methodSpec);
      out.endControlFlow();
      BSTNode<String, ResourceMethod> left = node.getLeft();
      BSTNode<String, ResourceMethod> right = node.getRight();
      if (left == null || right == null) {
        // this node doesn't have any children, or only one child, so we only need an "else", without an "else if"
        out.beginControlFlow("else");
        processSubtree(LogicUtils.firstNonNull(left, right));
        out.endControlFlow();
      }
      else {
        out.beginControlFlow("else if ($N < 0)", cmpVarName);
        processSubtree(left);
        out.endControlFlow();
        out.beginControlFlow("else");
        processSubtree(right);
        out.endControlFlow();
      }
    }

    public void processSubtree(BSTNode<String, ResourceMethod> node) {
      if (node == null)
        out.addStatement("return null"); // subtree is null (i.e. key not found), so return null
      else {
        visit(node);  // recur on the subtree (target is < node)
      }
    }
  }

  private class ResourceMethod {
    private String filename;
    private String filenamePrefix;
    private String filenameExtension;
    private MethodSpec methodSpec;
    private Class<? extends ResourcePrototype> resourceType;

    public ResourceMethod(String filename, String filenamePrefix, String filenameExtension, Class<? extends ResourcePrototype> resourceType, MethodSpec methodSpec) {
      this.filename = filename;
      this.filenamePrefix = filenamePrefix;
      this.filenameExtension = filenameExtension;
      this.methodSpec = methodSpec;
      this.resourceType = resourceType;
    }
  }

}
