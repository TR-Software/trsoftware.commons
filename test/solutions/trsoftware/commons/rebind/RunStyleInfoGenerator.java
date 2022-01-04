/*
 * Copyright 2022 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.rebind;

import com.google.common.collect.Iterables;
import com.google.gwt.core.ext.*;
import com.google.gwt.core.ext.Generator.RunsLocal;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import solutions.trsoftware.commons.client.BaseGwtTestCase;
import solutions.trsoftware.commons.client.testutil.RunStyleInfo;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.io.PrintWriter;
import java.util.List;

/**
 * Generator for {@link RunStyleInfo}, using the {@value RunStyleInfoGenerator#PROPERTY_HTML_UNIT} and
 * {@value RunStyleInfoGenerator#PROPERTY_RUN_STYLE} properties defined in {@code TestCommons.gwt.xml}.
 * <p>
 * These properties are set by {@link BaseGwtTestCase} based on the {@code -runStyle} value in {@code gwt.args}.
 */
@RunsLocal(requiresProperties = {RunStyleInfoGenerator.PROPERTY_HTML_UNIT, RunStyleInfoGenerator.PROPERTY_RUN_STYLE})
public class RunStyleInfoGenerator extends Generator {
  public static final String PROPERTY_RUN_STYLE = "junit.runStyle";
  public static final String PROPERTY_HTML_UNIT = "runStyleHtmlUnit";

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName)
      throws UnableToCompleteException {
    TypeOracle typeOracle = context.getTypeOracle();

    JClassType userType;
    try {
      userType = typeOracle.getType(typeName);
    } catch (NotFoundException e) {
      logger.log(TreeLogger.ERROR, "Unable to find metadata for type: " + typeName, e);
      throw new UnableToCompleteException();
    }
    String packageName = userType.getPackage().getName();
    String className = userType.getName();
    className = className.replace('.', '_');

    if (userType.isInterface() == null) {
      logger.log(TreeLogger.ERROR, userType.getQualifiedSourceName() + " is not an interface", null);
      throw new UnableToCompleteException();
    }

    PropertyOracle propertyOracle = context.getPropertyOracle();

    String runStyleValue = "";
    try {
      ConfigurationProperty configurationProperty = propertyOracle.getConfigurationProperty(PROPERTY_RUN_STYLE);
      List<String> values = configurationProperty.getValues();
      runStyleValue = Iterables.getOnlyElement(values, "");
      assert runStyleValue != null;
    }
    catch (BadPropertyValueException e) {
      logger.log(TreeLogger.ERROR, "Unable to find value for '" + PROPERTY_RUN_STYLE + "'", e);
      throw new UnableToCompleteException();
    }

    boolean isHtmlUnit = false;
    try {
      SelectionProperty selectionProperty = propertyOracle.getSelectionProperty(logger, PROPERTY_HTML_UNIT);
      String currentValue = selectionProperty.getCurrentValue();
      if (StringUtils.notBlank(currentValue))
        isHtmlUnit = Boolean.parseBoolean(currentValue);
    } catch (BadPropertyValueException e) {
      logger.log(TreeLogger.ERROR, "Unable to find value for '" + PROPERTY_HTML_UNIT + "'", e);
      throw new UnableToCompleteException();
    }

    className = className + "Impl";
    ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(
        packageName, className);
    composerFactory.addImplementedInterface(userType.getQualifiedSourceName());

    PrintWriter pw = context.tryCreate(logger, packageName, className);
    if (pw != null) {
      SourceWriter sw = composerFactory.createSourceWriter(context, pw);


      sw.println();
      sw.println("public boolean isHtmlUnit() {");
      sw.indent();
      sw.println("return " + isHtmlUnit + ";");
      sw.outdent();
      sw.println("}");

      sw.println();
      sw.println("public String getRunStyleString() {");
      sw.indent();
      sw.println("return \"" + runStyleValue.trim() + "\";");
      sw.outdent();
      sw.println("}");

      sw.commit(logger);
    }
    return composerFactory.getCreatedClassName();
  }
}
