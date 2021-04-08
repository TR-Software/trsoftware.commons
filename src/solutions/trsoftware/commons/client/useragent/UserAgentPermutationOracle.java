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

package solutions.trsoftware.commons.client.useragent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.useragent.rebind.UserAgentPropertyGenerator;

/**
 * Provides a lightweight mechanism for implementing browser-specific functionality based
 * on the {@code user.agent} property defined in {@code UserAgent.gwt.xml}
 * (the possible values of the {@code user.agent} property are specified in {@link UserAgentPropertyGenerator#VALID_VALUES})
 * <p>
 *   Instead of writing XML to implement browser-specific functionality, you can just call one of the methods defined
 *   by this singleton.  Because each method will return a {@code boolean}, the compiler should be able to perform dead
 *   code elimination.  Therefore using this oracle shouldn't increase the compiled code size any more than
 *   a "replace-with" declaration in the module XML. TODO: test that dead code elimination will work here indeed
 * </p>
 *
 * @author Alex
 * @since Jun 5, 2013
 * @see UserAgentPropertyGenerator
 */
public interface UserAgentPermutationOracle {

  // TODO: this class duplicates functionality provided by GWT 2.6+ with the public com.google.gwt.useragent.client.UserAgent class (which was formerly package-local in com.google.gwt.useragent.client.UserAgentAsserter as UserAgentProperty)

  UserAgentPermutationOracle INSTANCE = GWT.create(UserAgentPermutationOracle.class);

  // NOTE: this code should be manually kept in sync with UserAgent.gwt.xml.
  boolean ie6();
  boolean ie8();
  boolean gecko1_8();
  boolean safari();
  boolean opera();
  boolean ie9();
  boolean ie10();

  /**
   * @return The value of the "user.agent" GWT property for the current permutation.
   */
  String getValue();

}
