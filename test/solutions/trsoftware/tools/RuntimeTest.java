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

package solutions.trsoftware.tools;

import junit.framework.TestCase;

import java.sql.Timestamp;
import java.util.Date;

/**
 * This class can be used to test the run-time behavior of various Java features that might be ambiguous.
 *
 * @author Alex
 * @since 11/29/2017
 */
public class RuntimeTest extends TestCase {

  /**
   * Tests what happens when there is an overloaded method for a more general type and one for a more specific type.
   */
  public void testPolymorphism() throws Exception {
    class PolyTester {
      private Date date;

      public void setDate(Date date) {
        Class<? extends Date> dateClass = date.getClass();
        System.out.printf("setter for %s invoked with instance of %s%n", Date.class.getName(), dateClass.getName());
        this.date = date;
      }

      public void setDate(Timestamp date) {
        Class<? extends Timestamp> dateClass = date.getClass();
        System.out.printf("setter for %s invoked with instance of %s%n", Timestamp.class.getName(), dateClass.getName());
        this.date = date;
      }
    }

    Date d = new Date();
    Timestamp t = new Timestamp(System.currentTimeMillis());
    PolyTester polyTester = new PolyTester();
    polyTester.setDate(d);  // the Date method should be invoked
    polyTester.setDate(t);  // the Timestamp method should be invoked
    polyTester.setDate((Date)t);  // the Date method should be invoked if we up-cast a Timestamp to Date
  }

}
