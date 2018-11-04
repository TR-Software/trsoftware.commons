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

package solutions.trsoftware.commons.shared.text;

import junit.framework.TestCase;

import java.util.EnumSet;

import static solutions.trsoftware.commons.shared.text.Language.*;

/**
 * Oct 2, 2009
 *
 * @author Alex
 */
public class LanguageTest extends TestCase {

  public void testFromGoogleTranslateCode() throws Exception {
    assertEquals(ENGLISH, fromGoogleTranslateCode("en"));
    assertEquals(RUSSIAN, fromGoogleTranslateCode("ru"));
    // hebrew is a special case because the GT code doesn't match the ISO code
    assertEquals(HEBREW, fromGoogleTranslateCode("iw"));
  }

  public void testGetGoogleTranslateCode() throws Exception {
    assertEquals("en", ENGLISH.getGoogleTranslateCode());
    assertEquals("ru", RUSSIAN.getGoogleTranslateCode());
    // hebrew is a special case because the GT code doesn't match the ISO code
    assertEquals("iw", HEBREW.getGoogleTranslateCode());
  }

  public void testGetOfficialModelId() throws Exception {
    assertEquals("play", ENGLISH.getOfficialModelId());
    assertEquals("lang_ru", RUSSIAN.getOfficialModelId());
    assertEquals("lang_he", HEBREW.getOfficialModelId());
  }

  public void testFromAcceptLanguageHeader() throws Exception {
    assertSame(ENGLISH, fromAcceptLanguageHeader(null));
    assertSame(ENGLISH, fromAcceptLanguageHeader(""));
    assertSame(ENGLISH, fromAcceptLanguageHeader("en"));
    assertSame(ENGLISH, fromAcceptLanguageHeader("e"));
    assertSame(ENGLISH, fromAcceptLanguageHeader("en-US"));
    assertSame(ENGLISH, fromAcceptLanguageHeader("en-UK"));
    assertSame(ENGLISH, fromAcceptLanguageHeader("blah"));
    assertSame(ENGLISH, fromAcceptLanguageHeader("blah blah"));
    assertSame(ENGLISH, fromAcceptLanguageHeader("blah-blah"));

    assertSame(PORTUGUESE, fromAcceptLanguageHeader("pt"));
    assertSame(PORTUGUESE, fromAcceptLanguageHeader("pt-BR"));
    assertSame(PORTUGUESE, fromAcceptLanguageHeader("pt-br"));
    assertSame(PORTUGUESE, fromAcceptLanguageHeader("pt-blah blah"));

    assertSame(RUSSIAN, fromAcceptLanguageHeader("ru"));
    assertSame(RUSSIAN, fromAcceptLanguageHeader("ru blah"));

    assertSame(HEBREW, fromAcceptLanguageHeader("he blah"));
    assertSame(HEBREW, fromAcceptLanguageHeader("he-blah "));

    // chinese is a special case - it has 2 variations depending on country
    assertSame(CHINESE, fromAcceptLanguageHeader("zh"));
    assertSame(CHINESE, fromAcceptLanguageHeader("zh-cn"));
    assertSame(CHINESE, fromAcceptLanguageHeader("zh-CN"));
    assertSame(CHINESE, fromAcceptLanguageHeader("zh-SG"));
    assertSame(CHINESE, fromAcceptLanguageHeader("zh-sg"));
    assertSame(CHINESE, fromAcceptLanguageHeader("zh-blah"));
    assertSame(CHINESE_TRADITIONAL, fromAcceptLanguageHeader("zh-tw"));
    assertSame(CHINESE_TRADITIONAL, fromAcceptLanguageHeader("zh-TW"));
    assertSame(CHINESE_TRADITIONAL, fromAcceptLanguageHeader("zh-hk"));
    assertSame(CHINESE_TRADITIONAL, fromAcceptLanguageHeader("zh-HK"));
    assertSame(CHINESE_TRADITIONAL, fromAcceptLanguageHeader("zh-HK "));
    assertSame(CHINESE_TRADITIONAL, fromAcceptLanguageHeader("zh-HK blah"));
    assertSame(CHINESE_TRADITIONAL, fromAcceptLanguageHeader("zh-MO"));
    assertSame(CHINESE_TRADITIONAL, fromAcceptLanguageHeader("zh-mo"));
    assertSame(CHINESE_TRADITIONAL, fromAcceptLanguageHeader("zh-mo "));
    assertSame(CHINESE_TRADITIONAL, fromAcceptLanguageHeader("zh-mo blah"));
    assertSame(CHINESE_TRADITIONAL, fromAcceptLanguageHeader("zh-MO "));
  }

  public void testIsLogographic() throws Exception {
    EnumSet<Language> expectedLogographic = EnumSet.of(CHINESE, CHINESE_TRADITIONAL, JAPANESE, THAI);
    for (Language language : Language.values()) {
      assertEquals(language.name(), expectedLogographic.contains(language), language.isLogographic());
    }
  }

}