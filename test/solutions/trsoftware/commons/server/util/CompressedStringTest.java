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

package solutions.trsoftware.commons.server.util;

import junit.framework.TestCase;

/**
 * @author Alex
 * @since Apr 5, 2013
 */
public class CompressedStringTest extends TestCase {

  public void testCompressedString() throws Exception {
    checkString("Hello, hello, hello, is there anybody in there? Just nod if you can hear me, is there anyone home?");
    String st1 = "id,,com.google.gwt.aria.client.PrimitiveValueAttribute,,com/google/gwt/aria/client/PrimitiveValueAttribute.java,23,-1";
    String st2 = "jd,com.google.gwt.aria.client.PrimitiveValueAttribute::PrimitiveValueAttribute(Ljava/lang/String;Ljava/lang/String;)V,com.google.gwt.aria.client.PrimitiveValueAttribute,PrimitiveValueAttribute,com/google/gwt/aria/client/PrimitiveValueAttribute.java,28,0";
    checkString(st1);
    checkString(st2);
    checkString(st1+"\n"+st2); // see if get better compression on the concatenation of these two strings

  }

  private void checkString(String str) {
    System.out.println("Testing string: " + str);
    CompressedString cstr = new CompressedString(str);
    assertEquals(str, cstr.toString());  // make sure the compression was lossless
    System.out.println("cstr.getOriginalSize() = " + cstr.getOriginalSize());
    System.out.println("cstr.getCompressedSize() = " + cstr.getCompressedSize());
  }
}