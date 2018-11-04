/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.server.servlet.config;

import solutions.trsoftware.commons.server.util.reflect.ReflectionUtils;

import java.lang.reflect.Field;

import static solutions.trsoftware.commons.server.servlet.config.InitParameters.Param;
import static solutions.trsoftware.commons.server.servlet.config.InitParameters.ParameterParser;

/**
 * @author Alex
 * @since 1/3/2018
 */
public class InitParameterParserNotAvailable extends WebConfigException {
  private Field field;

  public InitParameterParserNotAvailable(Field field, String paramName, String paramValue, Class<?> expectedType, HasInitParameters config) {
    super(ReflectionUtils.toString(field) + " must contain a @" + Param.class.getSimpleName()
            + " annotation specifying a " + ParameterParser.class.getSimpleName()
            + "<" + expectedType.getSimpleName() + "> subclass",
        paramName, paramValue, expectedType, config);
    this.field = field;
  }

  public Field getField() {
    return field;
  }

}
