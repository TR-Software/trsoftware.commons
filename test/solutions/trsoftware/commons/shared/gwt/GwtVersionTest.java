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

package solutions.trsoftware.commons.shared.gwt;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author Alex
 * @since 4/23/2018
 */
public class GwtVersionTest extends TestCase {

  public void testCompareTo() throws Exception {
    List<GwtVersion> versions = Arrays.asList(
        new GwtVersion(null)
        , new GwtVersion("2.5.0")
        , new GwtVersion("2.8.1")
        , new GwtVersion("2.8.2")
        , new GwtVersion("2.8.0-rc3")
        , new GwtVersion("2.8.0-rc1")
        , new GwtVersion("2.8.0-rc2")
        , new GwtVersion("2.8.0")
    );
    System.out.println(CollectionUtils.printTotalOrdering(versions));
  }

}