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

package solutions.trsoftware.commons.shared.util.reflect;

import junit.framework.TestCase;

import java.util.List;

/**
 * @author Alex
 * @since 12/25/2017
 */
public class ClassNameParserTest extends TestCase {

  public void testNormalClasses() throws Exception {
    assertFalse(validateParseResult("Foo", "", "Foo", "Foo", "").isAnonymous());
    assertFalse(validateParseResult("Foo$Bar", "", "Foo$Bar", "Bar", "").isAnonymous());
    assertFalse(validateParseResult("com.Foo", "com", "Foo", "Foo", "").isAnonymous());
    assertFalse(validateParseResult("com.Foo$Bar", "com", "Foo$Bar", "Bar", "").isAnonymous());
    assertFalse(validateParseResult("com.example.Foo$Bar", "com.example", "Foo$Bar", "Bar", "").isAnonymous());
    assertFalse(validateParseResult("com.example.util.Foo$Bar", "com.example.util", "Foo$Bar", "Bar", "").isAnonymous());
    assertFalse(validateParseResult("com.example.Foo$Bar$Baz", "com.example", "Foo$Bar$Baz", "Baz", "").isAnonymous());
    assertFalse(validateParseResult("com.example.util.Foo$Bar$Baz", "com.example.util", "Foo$Bar$Baz", "Baz", "").isAnonymous());
    checkRandomExamples(false);
  }

  public void testAnonymousClasses() throws Exception {
    assertTrue(validateParseResult("Foo$1", "", "Foo$1", "", "1").isAnonymous());
    assertTrue(validateParseResult("Foo$Bar$12", "", "Foo$Bar$12", "", "12").isAnonymous());
    assertTrue(validateParseResult("com.Foo$123", "com", "Foo$123", "", "123").isAnonymous());
    assertTrue(validateParseResult("com.Foo$Bar$1234", "com", "Foo$Bar$1234", "", "1234").isAnonymous());
    assertTrue(validateParseResult("com.example.Foo$Bar$1", "com.example", "Foo$Bar$1", "", "1").isAnonymous());
    assertTrue(validateParseResult("com.example.util.Foo$Bar$1234", "com.example.util", "Foo$Bar$1234", "", "1234").isAnonymous());
    assertTrue(validateParseResult("com.example.Foo$Bar$Baz$1", "com.example", "Foo$Bar$Baz$1", "", "1").isAnonymous());
    assertTrue(validateParseResult("com.example.util.Foo$Bar$Baz$1234", "com.example.util", "Foo$Bar$Baz$1234", "", "1234").isAnonymous());
    checkRandomExamples(true);
  }

  private static void checkRandomExamples(boolean anon) {
    List<ClassName> testData = ClassName.randomExamples(10, 10, anon);
    for (ClassName x : testData) {
      validateParseResult(x);
    }
  }

  private static ClassNameParser validateParseResult(String clsName, String pkg, String complex, String simple, String anonId) {
    ClassNameParser parser = new ClassNameParser(clsName);
    System.out.println(clsName);
    System.out.println("  --> " + parser);
    assertEquals(clsName, parser.getFullName());
    assertEquals(complex, parser.getComplexName());
    assertEquals(pkg, parser.getPackageName());
    assertEquals(simple, parser.getSimpleName());
    assertEquals(anonId, parser.getAnonymousId());
    return parser;
  }

  private static void validateParseResult(ClassName c) {
    ClassNameParser parser = validateParseResult(c.fullName, c.pkg, c.complex, c.simple, c.anonId);
    assertEquals(c.anon, parser.isAnonymous());
  }



}