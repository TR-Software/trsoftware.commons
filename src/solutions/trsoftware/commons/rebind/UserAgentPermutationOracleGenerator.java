/*
 * Copyright 2021 TR Software Inc.
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

import com.google.gwt.core.ext.*;
import com.google.gwt.core.ext.typeinfo.*;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.useragent.rebind.UserAgentPropertyGenerator;

import java.io.PrintWriter;

/**
 * <p>
 * Generates an implementation of {@link solutions.trsoftware.commons.client.useragent.UserAgentPermutationOracle}
 * based on the current value of the "user.agent" property.  Each method's body
 * is going to be simply a <code>return true;</code> or <code>return false;</code> statement so that
 * it may be inlined and then eliminated by {@link com.google.gwt.dev.jjs.impl.DeadCodeElimination}.
 * </p>
 * <p>
 * This code is based on {@link com.google.gwt.useragent.rebind.UserAgentGenerator}.
 * </p>
 * <p>
 *   The possible values of the {@code user.agent} property are specified in {@link UserAgentPropertyGenerator.UserAgent}
 * </p>
 * @see UserAgentPropertyGenerator
 */
public class UserAgentPermutationOracleGenerator extends Generator {

  static final String PROPERTY_USER_AGENT = "user.agent";

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

    String userAgentValue;
    try {
      userAgentValue = propertyOracle.getSelectionProperty(logger, PROPERTY_USER_AGENT).getCurrentValue();
    } catch (BadPropertyValueException e) {
      logger.log(TreeLogger.ERROR, "Unable to find value for '" + PROPERTY_USER_AGENT + "'", e);
      throw new UnableToCompleteException();
    }

    String userAgentValueInitialCap = userAgentValue.substring(0, 1).toUpperCase()
        + userAgentValue.substring(1);
    className = className + "Impl" + userAgentValueInitialCap;

    ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(
        packageName, className);
    composerFactory.addImplementedInterface(userType.getQualifiedSourceName());

    PrintWriter pw = context.tryCreate(logger, packageName, className);
    if (pw != null) {
      SourceWriter sw = composerFactory.createSourceWriter(context, pw);
      // generate the boolean methods (their names correspond to the property values)
      for (JMethod methodToImpl : userType.getMethods()) {
        if (methodToImpl.getReturnType() == JPrimitiveType.BOOLEAN) {
          String name = methodToImpl.getName();
          sw.println();
          sw.print("public boolean "); sw.print(name); sw.println("() {");
          sw.indent(); sw.print("return "); sw.print(Boolean.toString(name.equals(userAgentValue.trim()))); sw.println(";"); sw.outdent();
          sw.println("}");
        }
      }
      // generate the getValue method
      sw.println();
      sw.println("public String getValue() {");
      sw.indent(); sw.print("return \""); sw.print(userAgentValue.trim()); sw.println("\";"); sw.outdent();
      sw.println("}");
      // commit the writer
      sw.commit(logger);
    }
    return composerFactory.getCreatedClassName();
  }
}
