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

package solutions.trsoftware.commons.server.servlet.config;

import com.google.common.primitives.Primitives;
import solutions.trsoftware.commons.server.servlet.config.InitParameters.Param;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.servlet.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static solutions.trsoftware.commons.server.servlet.config.InitParameters.ParameterParser;


/**
 * Populates the fields of an {@link InitParameters} instance from the {@code init-param} values in a
 * webapp config object such as {@link FilterConfig}, {@link ServletContext}, or {@link ServletConfig}.
 *
 * <p>
 * Each field of the {@link InitParameters} instance will be assigned the value of the {@code init-param} having
 * the same name as the field, parsed using the field type's {@code static valueOf(String)} method (which is already
 * defined by all primitive wrapper types as well as {@link String} and {@link Enum} types).
 * </p>
 * 
 * <p>
 * The {@link Param @Param} annotation can be used to override the default parsing behavior for any field of the
 * {@link InitParameters} instance.
 * <em>(This annotation is mandatory iff the field's type does not have a {@code static valueOf(String)} method).</em>
 * </p>
 *
 * @author Alex
 * @since 1/2/2018
 */
public class WebConfigParser {

  /**
   * This class contains only static methods, hence it's not to be instantiated
   */
  private WebConfigParser() {

  }

  /**
   * Populates the fields of the given {@link InitParameters} instance from the {@code init-param} values
   * in a webapp config object such as {@link FilterConfig}, {@link ServletContext}, or {@link ServletConfig}.
   * @param config adapter for the webapp config object (e.g. {@link FilterConfigWrapper}, {@link ServletConfigWrapper},
   * or {@link ServletContextWrapper})
   * @param parseResult a new instance of the class which declares the fields
   * to be populated from the {@code init-param} values
   * @return the same {@code parseResult} instance that was passed in, after its fields have been
   * populated with the {@code init-param} values from the config object
   * @throws WebConfigException if any {@code init-param} value could not be bound to a field
   * in the given {@link InitParameters} instance, or if a required {@code init-param} is missing
   */
  public static <P extends InitParameters> P parse(HasInitParameters config, P parseResult) throws WebConfigException {
    try {
      return bindParamsToFields(config, parseResult);
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);  // should never happen
    }
  }

  /**
   * Populates the fields of the given {@link InitParameters} instance from the {@code init-param} values
   * in the given {@link FilterConfig}.
   * @param config the object received by {@link Filter#init(FilterConfig)}, containing the {@code init-param} values
   * @param parseResult a new instance of the class which declares the fields
   * to be populated from the {@code init-param} values
   * @return the same {@code parseResult} instance that was passed in, after its fields have been
   * populated with the {@code init-param} values from the {@link FilterConfig}
   * @throws WebConfigException if any {@code init-param} value could not be bound to a field
   * in the given {@link InitParameters} instance, or if a required {@code init-param} is missing
   */
  public static <P extends InitParameters> P parse(FilterConfig config, P parseResult) throws WebConfigException {
    return parse(new FilterConfigWrapper(config), parseResult);
  }
  
  /**
   * Populates the fields of the given {@link InitParameters} instance from the {@code init-param} values
   * in the given {@link ServletConfig}.
   * @param config the object received by {@link Servlet#init(ServletConfig)}, containing the {@code init-param} values
   * @param parseResult a new instance of the class which declares the fields
   * to be populated from the {@code init-param} values
   * @return the same {@code parseResult} instance that was passed in, after its fields have been
   * populated with the {@code init-param} values from the {@link ServletConfig}
   * @throws WebConfigException if any {@code init-param} value could not be bound to a field
   * in the given {@link InitParameters} instance, or if a required {@code init-param} is missing
   */
  public static <P extends InitParameters> P parse(ServletConfig config, P parseResult) throws WebConfigException {
    return parse(new ServletConfigWrapper(config), parseResult);
  }
  
  /**
   * Populates the fields of the given {@link InitParameters} instance from the {@code context-param} values
   * in the given {@link ServletContext}.
   * @param config the webapp's configuration, containing the {@code context-param} values
   * @param parseResult a new instance of the class which declares the fields
   * to be populated from the {@code context-param} values
   * @return the same {@code parseResult} instance that was passed in, after its fields have been
   * populated with the {@code context-param} values from the {@link ServletContext}
   * @throws WebConfigException if any {@code context-param} value could not be bound to a field
   * in the given {@link InitParameters} instance, or if a required {@code context-param} is missing
   */
  public static <P extends InitParameters> P parse(ServletContext config, P parseResult) throws WebConfigException {
    return parse(new ServletContextWrapper(config), parseResult);
  }

  private static <P extends InitParameters> P bindParamsToFields(HasInitParameters webConfig, P parseResult) throws IllegalAccessException, WebConfigException {
    Field[] declaredFields = parseResult.getClass().getDeclaredFields();
    for (Field field : declaredFields) {
      String name = field.getName();
      if (name.startsWith("this$")) {
        continue;  // the parseResult class must be an inner class that's not static, and this field is a reference to its parent class instance
      }
      new FieldParser(field, webConfig, parseResult).parseAndSetValue();
    }
    return parseResult;
  }

  protected static class FieldParser {
    private final Field field;
    private final InitParameters parseResult;
    private final Param paramAnnotation;
    private final Class<?> type;
    private HasInitParameters config;

    protected FieldParser(Field field, HasInitParameters config, InitParameters parseResult) {
      this.field = field;
      this.parseResult = parseResult;
      field.setAccessible(true);
      paramAnnotation = field.getAnnotation(Param.class);
      Class<?> type = field.getType();
      if (type.isPrimitive())
        type = Primitives.wrap(type);
      this.type = type;
      this.config = config;
    }

    String getParamName() {
      if (paramAnnotation != null) {
        String nameFromAnn = paramAnnotation.name();
        if (StringUtils.notBlank(nameFromAnn))
          return nameFromAnn;
      }
      return field.getName();
    }

    boolean isRequired() {
      return paramAnnotation != null && paramAnnotation.required();
    }

    private void setValue(Object value) throws IllegalAccessException {
      field.set(parseResult, value);
    }

    void parseAndSetValue() throws IllegalAccessException, WebConfigException {
      String name = getParamName();
      String strValue = config.getInitParameter(name);
      if (strValue == null) {
        if (isRequired())
          throw new RequiredInitParameterMissing(name, field.getType(), config);
        return;  // don't set a value for a field whose parameter is missing
      }
      // 1) if this is a string field, we just set a string value of the param
      if (type == String.class) {
        setValue(strValue);
        return;
      }
      // 2) otherwise, attempt to use the type's static valueOf(String) method, if available (this is the case for all primitive wrappers)
      {
        Method valueOfMethod = null;
        try {
          valueOfMethod = type.getMethod("valueOf", String.class);
        }
        catch (NoSuchMethodException e) {
          // ignore this exception; will attempt to use the parser defined by annotation
        }
        if (valueOfMethod != null && Modifier.isStatic(valueOfMethod.getModifiers())) {
          try {
            valueOfMethod.setAccessible(true);
            setValue(valueOfMethod.invoke(null, strValue));
            return;
          }
          catch (InvocationTargetException e) {
            throw new InitParameterParseException(name, strValue, field.getType(), e, config);
          }
        }
      }
      // 3) attempt to use the parser defined by annotation
      {
        Class<? extends ParameterParser> parserClass = null;
        if (paramAnnotation != null) {
          // check that parserClass is not the default value of the annotation member
          if (paramAnnotation.parser() != ParameterParser.class) {
            parserClass = paramAnnotation.parser();
          }
        }
        if (parserClass == null)
          throw new InitParameterParserNotAvailable(field, name, strValue, field.getType(), config);
        else {
          try {
            // attempt to instantiate the parser
            ParameterParser parser = parserClass.newInstance();
            setValue(parser.parse(strValue));
          }
          catch (Exception e) {
            throw new InitParameterParseException(name, strValue, field.getType(), e, config);
          }
        }
      }
    }
  }

}
