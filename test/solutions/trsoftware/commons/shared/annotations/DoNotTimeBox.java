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

package solutions.trsoftware.commons.shared.annotations;

import solutions.trsoftware.junit.TestTimeboxDecorator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test methods that should not be surrounded with {@link TestTimeboxDecorator}
 * should be marked with this annotation.  This is useful for tests
 *
 * @author Alex
 *
 * @deprecated TODO: get rid of this class, since it's logically equivalent to @Slow
 */
@Retention(RetentionPolicy.RUNTIME)  // the JVM won't have access usages of @Slow without this
@Target(ElementType.METHOD)  // only methods may be annotated with @Slow
public @interface DoNotTimeBox {
}