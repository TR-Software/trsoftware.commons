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

package solutions.trsoftware.commons.server.memquery;

import solutions.trsoftware.commons.server.memquery.schema.ColSpec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Methods marked with this annotation will be treated as projected fields with the specified name, and their
 * names can be passed to {@link QueryBuilder#createORMSchema} or {@link QueryBuilder#createORM} in order to
 * add automatically-generated {@link ColSpec}s to the query.
 *
 * @author Alex, 1/6/14
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProjectedField {
  /** If not specified, the name of the method will be used */
  String name() default "";
}
