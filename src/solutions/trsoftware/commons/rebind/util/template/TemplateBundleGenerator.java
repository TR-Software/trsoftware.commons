/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.rebind.util.template;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import solutions.trsoftware.commons.server.util.FileTemplateParser;
import solutions.trsoftware.commons.shared.util.template.*;
import solutions.trsoftware.commons.shared.util.template.TemplateBundle.Resource;

import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generates an implementation of a user-defined interface {@code T} that extends {@link TemplateBundle}.
 *
 * A {@link TemplateBundle} is similar to an {@link ImageBundle}
 * (in fact, much of this generator code was borrowed from {@link com.google.gwt.user.rebind.ui.ImageBundleGenerator}),
 * except each resource file defines a template instead of an image.
 *
 *<p>
 * Each method in {@code T} must be declared to return {@link Template}, take no parameters,
 * and optionally specify the filename as metadata (either a {@link Resource} annotation
 * or a <code>@gwt.resource</code> Javadoc tag giving the name of a template file that can be found on the classpath).
 * In the absence of the metadata, the file will be assumed to be located in the same package as {@code T} and
 * to have the same name as the method, with an extension of either {@code .txt} or {@code .html}.
 *</p>
 */
public class TemplateBundleGenerator extends Generator {

  /**
   * Simple wrapper around JMethod that allows for unit test mocking.
   */
  interface JMethodOracle {

    @SuppressWarnings("deprecation")
    Resource getAnnotation(Class<Resource> clazz);

    String getName();

    String getPackageName();
  }

  /**
   * Indirection around the act of looking up a resource that allows for unit
   * test mocking.
   */
  /* private */interface ResourceLocator {
    /**
     *
     * @param resName the resource name in a format that could be passed to
     *          <code>ClassLoader.getResource()</code>
     * @return <code>true</code> if the resource is present
     */
    boolean isResourcePresent(String resName);
  }

  private static class JMethodOracleImpl implements JMethodOracle {
    private final JMethod delegate;

    public JMethodOracleImpl(JMethod delegate) {
      this.delegate = delegate;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Resource getAnnotation(Class<Resource> clazz) {
      return delegate.getAnnotation(clazz);
    }

    @Override
    public String getName() {
      return delegate.getName();
    }

    @Override
    public String getPackageName() {
      return delegate.getEnclosingType().getPackage().getName();
    }
  }

  /* private */static final String MSG_JAVADOC_FORM_DEPRECATED = "Use of @gwt.resource in javadoc is deprecated; use the annotation TemplateBundle.@Resource instead";

  /* private */static final String MSG_MULTIPLE_ANNOTATIONS = "You are using both the @Resource annotation and the deprecated @gwt.resource in javadoc; @Resource will be used, and @gwt.resource will be ignored";

  /* private */static final String MSG_NO_FILE_BASED_ON_METHOD_NAME = "No matching template resource was found; any of the following filenames would have matched had they been present:";

  private static final String TEMPLATE_QNAME = "solutions.trsoftware.commons.shared.util.template.Template";
  private static final String TEMPLATEPART_QNAME = "solutions.trsoftware.commons.shared.util.template.TemplatePart";
  private static final String STRINGPART_QNAME = "solutions.trsoftware.commons.shared.util.template.StringPart";
  private static final String VARIABLEPART_QNAME = "solutions.trsoftware.commons.shared.util.template.VariablePart";

  private static final String GWT_QNAME = "com.google.gwt.core.client.GWT";

  private static final String[] TEMPLATE_FILE_EXTENSIONS = {"html", "txt"};

  private static final String TEMPLATEBUNDLE_QNAME = "solutions.trsoftware.commons.shared.util.template.TemplateBundle";

  /* private */static String msgCannotFindTemplateFromMetaData(String resName) {
    return "Unable to find template resource '" + resName + "'";
  }

  private final ResourceLocator resLocator;

  /**
   * Default constructor for template bundle. Locates resources using this class's
   * own class loader.
   */
  public TemplateBundleGenerator() {
    this(new ResourceLocator() {
      public boolean isResourcePresent(String resName) {
        URL url = this.getClass().getClassLoader().getResource(resName);
        return url != null;
      }
    });
  }

  /**
   * Default access so that it can be accessed by unit tests.
   */
  /* private */TemplateBundleGenerator(ResourceLocator resourceLocator) {
    assert (resourceLocator != null);
    this.resLocator = resourceLocator;
  }

  @Override
  public String generate(TreeLogger logger, GeneratorContext context,
      String typeName) throws UnableToCompleteException {

    TypeOracle typeOracle = context.getTypeOracle();

    // Get metadata describing the user's class.
    JClassType userType = getValidUserType(logger, typeName, typeOracle);

    // Write the new class.
    JMethod[] templateMethods = userType.getOverridableMethods();
    String resultName = generateImplClass(logger, context, userType, templateMethods);

    // Return the complete name of the generated class.
    return resultName;
  }

  /**
   * Gets the resource name of the template associated with the specified template
   * bundle method in a form that can be passed to
   * <code>ClassLoader.getResource()</code>.
   *
   * @param logger the main logger
   * @param method the template bundle method whose template name is being sought
   * @return a resource name that is suitable to be passed into
   *         <code>ClassLoader.getResource()</code>; never returns
   *         <code>null</code>
   * @throws UnableToCompleteException thrown if a resource was specified but
   *           could not be found on the classpath
   */
  /* private */String getTemplateResourceName(TreeLogger logger,
      JMethodOracle method) throws UnableToCompleteException {
    String templateName = tryGetFileNameFromMetaData(logger, method);
    if (templateName != null) {
      return templateName;
    } else {
      return getFileNameFromMethodName(logger, method);
    }
  }

  private String computeSubclassName(JClassType userType) {
    String baseName = userType.getName().replace('.', '_');
    return baseName + "_generatedBundle";
  }

  private void generateTemplateMethod(SourceWriter sw, JMethod method, String resourceName) {
    // build the template object right now (at compile time) then "serialize" it
    Template template = FileTemplateParser.getInstance().getTemplate(resourceName);
    Iterator<TemplatePart> partIterator = template.getParts().iterator();
    if (!partIterator.hasNext())
      return; // this template doesn't have any parts

    // Create a singleton that this method can return. There is no need to
    // create a new instance every time this method is called, since
    // Template is immutable; we just laziliy instantiate the field to amortize the cost
    String name = method.getName();
    String fieldName = name + "_field";
    sw.println();
    sw.println("private static Template " + fieldName + " = null;");
    sw.println();
    String decl = method.getReadableDeclaration(false, true, true, true, true);
    {
      sw.print(decl);
      sw.println(" {");
      {
        sw.indent();
        sw.println("if (" + fieldName + " == null) {");
        {
          sw.indent();
          sw.println(fieldName + " = new Template(java.util.Arrays.<TemplatePart>asList(");
          {
            sw.indent();
            while (partIterator.hasNext()) {
              TemplatePart part = partIterator.next();
              sw.print("new ");
              if (part instanceof VariablePart)
                sw.print("VariablePart(\"" + ((VariablePart)part).getVarName() + "\")");
              else if (part instanceof StringPart)
                sw.print("StringPart(\"" + escape(part.toString()) + "\")");
              if (partIterator.hasNext())
                sw.println(", ");
            }
            sw.println("));");  // close Arrays.asList
            sw.outdent();
          }
          sw.println("}");  // close Arrays.asList
          sw.outdent();
        }
        sw.println("return " + fieldName + ";");
        sw.outdent();
      }
      sw.println("}");
    }
  }

  /**
   * Generates the template bundle implementation class, checking each method for
   * validity as it is encountered.
   */
  private String generateImplClass(TreeLogger logger, GeneratorContext context,
      JClassType userType, JMethod[] templateMethods)
      throws UnableToCompleteException {
    // Lookup the type info for Template so that we can check for the proper return type on the TemplateBundle methods.
    final JClassType templateClass;
    try {
      templateClass = userType.getOracle().getType(
          TEMPLATE_QNAME);
    } catch (NotFoundException e) {
      logger.log(TreeLogger.ERROR, TEMPLATE_QNAME
          + " class is not available", e);
      throw new UnableToCompleteException();
    }

    // Compute the package and class names of the generated class.
    String pkgName = userType.getPackage().getName();
    String subName = computeSubclassName(userType);

    // Begin writing the generated source.
    ClassSourceFileComposerFactory f = new ClassSourceFileComposerFactory(
        pkgName, subName);
    f.addImport(TEMPLATE_QNAME);
    f.addImport(TEMPLATEPART_QNAME);
    f.addImport(STRINGPART_QNAME);
    f.addImport(VARIABLEPART_QNAME);
    f.addImport(GWT_QNAME);
    f.addImplementedInterface(userType.getQualifiedSourceName());

    PrintWriter pw = context.tryCreate(logger, pkgName, subName);
    if (pw != null) {
      SourceWriter sw = f.createSourceWriter(context, pw);

      // Store the computed template names so that we don't have to lookup them up again.
      List<String> templateResNames = new ArrayList<String>();

      for (JMethod method : templateMethods) {
        String branchMsg = "Analyzing method '" + method.getName()
            + "' in type " + userType.getQualifiedSourceName();
        TreeLogger branch = logger.branch(TreeLogger.DEBUG, branchMsg, null);

        // Verify that this method is valid on an template bundle.
        if (method.getReturnType() != templateClass) {
          branch.log(TreeLogger.ERROR, "Return type must be "
              + TEMPLATE_QNAME, null);
          throw new UnableToCompleteException();
        }

        if (method.getParameters().length > 0) {
          branch.log(TreeLogger.ERROR, "Method must have zero parameters", null);
          throw new UnableToCompleteException();
        }

        // Find the associated templated resource.
        String templateResName = getTemplateResourceName(branch,
            new JMethodOracleImpl(method));
        assert (templateResName != null);
        templateResNames.add(templateResName);
      }

      // create a cache for all the instances of Template (these are immutable,
      // and can be lazily instantiated on demand).  The keys for the map
      // will be the names of the template resources.
      sw.println();

      // Generate an implementation of each method.
      int templateResNameIndex = 0;
      for (JMethod method : templateMethods) {
        generateTemplateMethod(sw, method, templateResNames.get(templateResNameIndex++));
      }

      // Finish.
      sw.commit(logger);
    }

    return f.getCreatedClassName();
  }

  /**
   * Attempts to get the template name from the name of the method itself by
   * speculatively appending various template-like file extensions in a prioritized
   * order. The first template found, if any, is used.
   *
   * @param logger if no matching template resource is found, an explanatory
   *          message will be logged
   * @param method the method whose name is being examined for matching template
   *          resources
   * @return a resource name that is suitable to be passed into
   *         <code>ClassLoader.getResource()</code>; never returns
   *         <code>null</code>
   * @throws UnableToCompleteException thrown when no template can be found based
   *           on the method name
   */
  private String getFileNameFromMethodName(TreeLogger logger,
                                           JMethodOracle method) throws UnableToCompleteException {
    String pkgName = method.getPackageName();
    String pkgPrefix = pkgName.replace('.', '/');
    if (pkgPrefix.length() > 0) {
      pkgPrefix += "/";
    }
    String methodName = method.getName();
    String pkgAndMethodName = pkgPrefix + methodName;
    List<String> testFileNames = new ArrayList<String>();
    for (int i = 0; i < TEMPLATE_FILE_EXTENSIONS.length; i++) {
      String testFileName = pkgAndMethodName + '.' + TEMPLATE_FILE_EXTENSIONS[i];
      if (resLocator.isResourcePresent(testFileName)) {
        return testFileName;
      }
      testFileNames.add(testFileName);
    }

    TreeLogger branch = logger.branch(TreeLogger.ERROR,
        MSG_NO_FILE_BASED_ON_METHOD_NAME, null);
    for (String testFileName : testFileNames) {
      branch.log(TreeLogger.ERROR, testFileName, null);
    }

    throw new UnableToCompleteException();
  }

  private JClassType getValidUserType(TreeLogger logger, String typeName,
      TypeOracle typeOracle) throws UnableToCompleteException {
    try {
      // Get the type that the user is introducing.
      JClassType userType = typeOracle.getType(typeName);

      // Get the type this generator is designed to support.
      JClassType magicType = typeOracle.findType(TEMPLATEBUNDLE_QNAME);

      // Ensure it's an interface.
      if (userType.isInterface() == null) {
        logger.log(TreeLogger.ERROR, userType.getQualifiedSourceName()
            + " must be an interface", null);
        throw new UnableToCompleteException();
      }

      // Ensure proper derivation.
      if (!userType.isAssignableTo(magicType)) {
        logger.log(TreeLogger.ERROR, userType.getQualifiedSourceName()
            + " must be assignable to " + magicType.getQualifiedSourceName(),
            null);
        throw new UnableToCompleteException();
      }

      return userType;

    } catch (NotFoundException e) {
      logger.log(TreeLogger.ERROR, "Unable to find required type(s)", e);
      throw new UnableToCompleteException();
    }
  }

  /**
   * Attempts to get the template name (verbatim) from an annotation.
   *
   * @return the string specified in in the {@link Resource}
   *         annotation, or <code>null</code>
   */
  private String tryGetFileNameFromAnnotation(JMethodOracle method) {
    Resource resAnn = method.getAnnotation(Resource.class);
    String fileName = null;
    if (resAnn != null) {
      fileName = resAnn.value();
    }
    return fileName;
  }

  /**
   * Attempts to get the template name from an annotation.
   *
   * @param logger if an annotation is found but the specified resource isn't
   *          available, an error is logged
   * @param method the image bundle method whose associated image resource is
   *          being sought
   * @return a resource name that is suitable to be passed into
   *         <code>ClassLoader.getResource()</code>, or <code>null</code>
   *         if metadata wasn't provided
   * @throws UnableToCompleteException thrown when metadata is provided but the
   *           resource cannot be found
   */
  private String tryGetFileNameFromMetaData(TreeLogger logger, JMethodOracle method) throws UnableToCompleteException {
    String fileName = tryGetFileNameFromAnnotation(method);
    if (fileName == null) {
      // Exit early because no annotation was found.
      return null;
    }

    if (fileName == null) {
      // Exit early because neither an annotation nor javadoc was found.
      return null;
    }

    // If the name has no slashes (that is, it isn't a fully-qualified resource
    // name), then prepend the enclosing package name automatically, being
    // careful about the default package.
    if (fileName.indexOf("/") == -1) {
      String pkgName = method.getPackageName();
      if (!"".equals(pkgName)) {
        fileName = pkgName.replace('.', '/') + "/" + fileName;
      }
    }

    if (!resLocator.isResourcePresent(fileName)) {
      // Not found.
      logger.log(TreeLogger.ERROR, msgCannotFindTemplateFromMetaData(fileName), null);
      throw new UnableToCompleteException();
    }

    // Success.
    return fileName;
  }

}
