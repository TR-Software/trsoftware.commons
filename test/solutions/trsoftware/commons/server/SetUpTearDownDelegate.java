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

package solutions.trsoftware.commons.server;

import junit.framework.Assert;

/**
 * Since Java doesn't have multiple inheritance, it's not possible
 * to mix mulple TestCase superclasses each with a different setUp and and tearDown
 * behavior.
 *
 * This class, in conjunction with SuperTestCase solves that problem.
 *
 * @author Alex
 */
public abstract class SetUpTearDownDelegate extends Assert {

  /** sublcasses should override to provide customized setUp logic */
  public abstract void setUp() throws Exception;

  /** sublcasses should override to provide customized tearDown logic */
  public abstract void tearDown() throws Exception;

}