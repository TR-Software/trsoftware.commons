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

import solutions.trsoftware.commons.shared.data.CountryCodes;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.text.LogographicTokenizer;
import solutions.trsoftware.commons.shared.util.text.TextTokenizer;
import solutions.trsoftware.commons.shared.util.text.WhitespaceTokenizer;

import java.util.*;

/**
 * @since May 12, 2007
 * @author Alex
 */
public enum Language {
  // English is the original language supported by TypeRacer
  ENGLISH("en") {
    @Override
    public String getOfficialModelId() {
      return "play";
    }},
  // these were added when Google Translate was introduced
  AFRIKAANS("af"),
  ALBANIAN("sq"),
  ARABIC("ar"),
  BELARUSIAN("be"),
  BULGARIAN("bg"),
  CATALAN("ca"),
  /**
   * The "simplified" Chinese script used in People's Republic of China and
   * Singapore Windows Vista sends these Accept-Language headers for it: zh-cn,
   * zh-sg NOTE: Google Translate has both "zh" and "zh-CN" items for this,
   * which are identical
   */
  CHINESE("zh"),
  /**
   * The "traditional" Chinese "script" still used in Taiwan, Hong Kong, Macao,
   * and some Chinese communities outside the mainland.  This script was used
   * before the communists took over in China. Windows Vista sends these
   * Accept-Language headers for it: zh-tw, zh-hk, zh-mo
   */
  CHINESE_TRADITIONAL("zh-tw") {
    @Override
    public String getGoogleTranslateCode() {
      return "zh-TW";
    }
    @Override
    public String getEnglishPrettyName() {
      return "Chinese";  // we'll call both versions of Chinese the same, hoping the user can disambiguate from the context (e.g. from flag icon or native name) - because the string "Traditional Chinese" is too long for the UI
    }},
  CROATIAN("hr"),
  CZECH("cs"),
  DANISH("da"),
  DUTCH("nl"),
  ESTONIAN("et"),
  FILIPINO("tl"),
  FINNISH("fi"),
  FRENCH("fr"),
  GALACIAN("gl"),
  GERMAN("de"),
  GREEK("el"),
  HEBREW("he") {
    @Override
    public String getGoogleTranslateCode() {
      return "iw";  // Google Translate uses the old code for hebrew, which changed in the 1980s
    }},
  HINDI("hi"),
  HUNGARIAN("hu"),
  INDONESIAN("id"),
  IRISH("ga"),
  ITALIAN("it"),
  JAPANESE("ja"),
  /**
   * Korean is an alphabetic language despite its letters looking blocky - it
   * has spaces between words. The reason its strings are shorter is because
   * most characters represent a syllable rather than a letter (see:
   * http://en.wikipedia.org/wiki/Korean_alphabet) "In contrast to Chinese and
   * Japanese, Korean is typed in a similar way to Western languages."
   * (http://en.wikipedia.org/wiki/Keyboard_layout)
   */
  KOREAN("ko") {
    @Override
    public double charsPerWord() {
      return 2.5;  // based on observation that English text translated into Thai uses 50% of the number of chars the English text had (run TextFileTranslationPruner.java to get the actual numbers)
    }},
  LATVIAN("lv"),
  LITHUANIAN("lt"),
  MACEDONIAN("mk"),
  MALAY("ms"),
  MALTESE("mt"),
  NORWEGIAN("no"),
  PERSIAN("fa"),
  POLISH("pl"),
  PORTUGUESE("pt"),
  ROMANIAN("ro"),
  RUSSIAN("ru") {
    @Override
    public String getAlphabet() {
      return "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    }
  },
  SERBIAN("sr") {
    @Override
    public String getEnglishPrettyName() {
      return "Serbian (Cyrillic)";
    }
  },
  /** Serbian is often written using a Latin transliteration system, so we have to support both */
  SERBIAN_LATIN("sr-latn") { // NOTE: changed latn to lowercase since modelIds can't have uppercase letters
    @Override
    public String getEnglishPrettyName() {
      return "Serbian (Latin)";
    }
  },
  SLOVAK("sk"),
  SLOVENIAN("sl"),
  SPANISH("es"),
  SWAHILI("sw"),
  SWEDISH("sv"),
  /**
   * Thai is an alphabetic language but has no spaces between words, hence its
   * strings are a bit shorter than English. However, we lump Thai with
   * logographic languages because the UI support and tokenization needed for it
   * is the same as these, but it overrides charsPerWord to provide more
   * accurate WPM calculations (if we used 1 char per word for it, people would
   * get kicked out for cheating).
   */
  THAI("th") {
    @Override
    public double charsPerWord() {
      // since we can't tokenize Thai properly into words without serious machine
      // learning, we'll tokenize it into individual characters like Chinese,
      // but will return a different charsPerWord value from Chinese, to not throw off the WPM calculations
      return 3.75;  // based on observation that English text translated into Thai uses 75% of the number of chars the English text had (run TextFileTranslationPruner.java to get the actual numbers)
    }},
  TURKISH("tr"),
  UKRAINIAN("uk"),
  VIETNAMESE("vi"),
  WELSH("cy"),
  YIDDISH("yi");

  private String isoCode;

  public static final String MODEL_ID_PREFIX = "lang_";

  private static final transient WhitespaceTokenizer WHITESPACE_TOKENIZER = new WhitespaceTokenizer();
  private static final transient LogographicTokenizer LOGOGRAPHIC_TOKENIZER = new LogographicTokenizer();

  Language(String isoCode) {
    this.isoCode = isoCode;
  }

  public double charsPerWord() {
    // we tokenize Chinese, and the other logographic languages to one char per word.
    // this helps to make sure the bots aren't too fast and enough time is given for the race
    if (isLogographic())
      return 1;
    return 5;  // this is the standard used for English (although the actual average is 4.26 by my estimate)
  }

  public String getIsoCode() {
    return isoCode;
  }

  public String getGoogleTranslateCode() {
    return isoCode;  // HEBREW should override this
  }

  /** @return The modelId corresponding to this language */
  public String getOfficialModelId() {
    return MODEL_ID_PREFIX + isoCode;  // ENGLISH and CODE should override this
  }

  public TextTokenizer getTokenizer() {
    if (isLogographic())
      return LOGOGRAPHIC_TOKENIZER;
    else
      return WHITESPACE_TOKENIZER;
  }


  public static Language fromGoogleTranslateCode(String googleTranslateCode) {
    // special case: both "zh" and "zh-CN" are valid for google translate
    if ("zh-CN".equals(googleTranslateCode))
      return CHINESE;

    // this lookup doesn't need to be fast because it won't be used in production
    for (Language language : values()) {
      if (language.getGoogleTranslateCode().equals(googleTranslateCode))
        return language;
    }
    return null;
  }

  private static final Map<String, Language> isoCodes = new HashMap<String, Language>();

  static {
    for (Language language : values()) {
      isoCodes.put(language.getIsoCode(), language);
    }
  }

  private static final Map<String, Language> chineseCountryToLanguageMap = MapUtils.hashMap(
      "cn", CHINESE, "sg", CHINESE,
      "tw", CHINESE_TRADITIONAL, "hk", CHINESE_TRADITIONAL, "mo", CHINESE_TRADITIONAL);


  public static Language fromIsoCode(String isoCode) {
    return isoCodes.get(isoCode);
  }

  /**
   * @param headerValue The value of the HTTP Accept-Language header.
   * @return The closest matching language, or ENGLISH if no match is found.
   */
  public static Language fromAcceptLanguageHeader(String headerValue) {
    if (StringUtils.notBlank(headerValue)) {
      headerValue = headerValue.trim().toLowerCase();
      if (headerValue.length() >= 2) {
        String isoCode = headerValue.substring(0, 2);
        if (isoCode.equals("zh") && headerValue.length() >= 5) {
          // Chinese is a special case - whether to use simplified or traditional script depends on country
          String country = headerValue.substring(3, 5);
          Language chineseType = chineseCountryToLanguageMap.get(country);
          if (chineseType == null)
            chineseType = CHINESE;
          return chineseType;
        }
        Language language = fromIsoCode(isoCode);
        if (language != null)
          return language;
      }
    }
    return ENGLISH;  // default
  }

  /**
   * Most (but not all) words in these languages are written using one
   * character
   */
  private static final Set<Language> logographicLanguages = EnumSet.of(CHINESE, CHINESE_TRADITIONAL, JAPANESE, THAI);
  // Thai is a special case because it's alphabetic but has no spaces between characters
  // We lump Thai with logographic languages because the UI support and tokenization needed for it is the same as these, but it overrides charsPerWord to provide better WPM calculations
  // NOTE: Korean is an alphabetic language despite its letters looking blocky - it has spaces between words.  The reason its strings are shorter is because most characters represent a syllable rather than a letter (see: http://en.wikipedia.org/wiki/Korean_alphabet )

  public boolean isLogographic() {
    return logographicLanguages.contains(this);
  }

  public boolean isForeign() {
    return getForeignLanguages().contains(this);
  }

  public static Set<Language> getForeignLanguages() {
    Set<Language> languages = EnumSet.allOf(Language.class);
    languages.remove(Language.ENGLISH);  // we already have english :)
    return languages;
  }

  public String getEnglishPrettyName() {
    return StringUtils.capitalize(name().toLowerCase());
  }

  // Map generated with LanguageModelPrettyNamesToStringLiterals.java:
  private static final Map<Language, String> nativePrettyNames = MapUtils.enumMap(Language.class,
      Language.ENGLISH, "English",
      Language.AFRIKAANS, "Afrikaans",
      Language.ALBANIAN, "Shqip",
      Language.ARABIC, "\u0627\u0644\u0639\u0631\u0628\u064a\u0629",
      Language.BELARUSIAN, "\u0411\u0435\u043b\u0430\u0440\u0443\u0441\u043a\u0430\u044f",
      Language.BULGARIAN, "\u0411\u044a\u043b\u0433\u0430\u0440\u0441\u043a\u0438 \u0435\u0437\u0438\u043a",
      Language.CATALAN, "Catal\u00e0",
      Language.CHINESE, "\u4e2d\u6587",
      Language.CHINESE_TRADITIONAL, "\u6b63\u9ad4\u5b57 / \u7e41\u9ad4\u5b57",
      Language.CROATIAN, "Hrvatski",
      Language.CZECH, "\u010ce\u0161tina",
      Language.DANISH, "Dansk",
      Language.DUTCH, "Nederlands",
      Language.ESTONIAN, "Eesti",
      Language.FILIPINO, "Wikang Tagalog",
      Language.FINNISH, "Suomi",
      Language.FRENCH, "Fran\u00e7ais",
      Language.GALACIAN, "Galego",
      Language.GERMAN, "Deutsch",
      Language.GREEK, "\u0395\u03bb\u03bb\u03b7\u03bd\u03b9\u03ba\u03ac",
      Language.HEBREW, "\u05e2\u05d1\u05e8\u05d9\u05ea",
      Language.HINDI, "\u0939\u093f\u0928\u094d\u0926\u0940 / \u0939\u093f\u0902\u0926\u0940",
      Language.HUNGARIAN, "Magyar",
      Language.INDONESIAN, "Bahasa Indonesia",
      Language.IRISH, "Gaeilge",
      Language.ITALIAN, "Italiano",
      Language.JAPANESE, "\u65e5\u672c\u8a9e",
      Language.KOREAN, "\ud55c\uad6d\uc5b4 / \uc870\uc120\ub9d0",
      Language.LATVIAN, "Latvie\u0161u valoda",
      Language.LITHUANIAN, "Lietuvi\u0173 kalba",
      Language.MACEDONIAN, "\u041c\u0430\u043a\u0435\u0434\u043e\u043d\u0441\u043a\u0438 \u0458\u0430\u0437\u0438\u043a",
      Language.MALAY, "Bahasa Melayu / \u0628\u0647\u0627\u0633 \u0645\u0644\u0627\u064a\u0648\u200e",
      Language.MALTESE, "Malti",
      Language.NORWEGIAN, "Norsk",
      Language.PERSIAN, "\u0641\u0627\u0631\u0633\u06cc",
      Language.POLISH, "Polski",
      Language.PORTUGUESE, "Portugu\u00eas",
      Language.ROMANIAN, "Rom\u00e2n\u0103",
      Language.RUSSIAN, "\u0420\u0443\u0441\u0441\u043a\u0438\u0439 \u044f\u0437\u044b\u043a",
      Language.SERBIAN, "\u0421\u0440\u043f\u0441\u043a\u0438 \u0458\u0435\u0437\u0438\u043a",
      Language.SERBIAN_LATIN, "Srpski",
      Language.SLOVAK, "Sloven\u010dina",
      Language.SLOVENIAN, "Sloven\u0161\u010dina",
      Language.SPANISH, "Espa\u00f1ol",
      Language.SWAHILI, "Kiswahili",
      Language.SWEDISH, "Svenska",
      Language.THAI, "\u0e44\u0e17\u0e22",
      Language.TURKISH, "T\u00fcrk\u00e7e",
      Language.UKRAINIAN, "\u0423\u043a\u0440\u0430\u0457\u043d\u0441\u044c\u043a\u0430",
      Language.VIETNAMESE, "Ti\u1ebfng Vi\u1ec7t",
      Language.WELSH, "Cymraeg",
      Language.YIDDISH, "\u05d9\u05d9\u05b4\u05d3\u05d9\u05e9");

  public String getNativePrettyName() {
    return nativePrettyNames.get(this);
  }

  private static final List<Language> top25 = Arrays.asList(
      CHINESE,
      CHINESE_TRADITIONAL,
      SPANISH,
      ENGLISH,
      HINDI,
      ARABIC,
      PORTUGUESE,
      RUSSIAN,
      JAPANESE,
      GERMAN,
      KOREAN,
      FRENCH,
      VIETNAMESE,
      ITALIAN,
      TURKISH,
      POLISH,
      UKRAINIAN,
      MALAY,
      ROMANIAN,
      INDONESIAN,
      DUTCH,
      THAI,
      CROATIAN,
      HUNGARIAN,
      GREEK);

  /** @return n of the most widely spoken languages (up to 25) */
  public static Language[] getMostPopularLanguages(int n) {
    Language[] result = new Language[Math.min(n, top25.size())];
    for (int i = 0; i < result.length; i++) {
      result[i] = top25.get(i);
    }
    return result;
  }

  // partially generated by solutions.trsoftware.tools.translate.CountryFlagsDownloader on Fri Nov 06 15:14:24 EST 2009
  // then human-disambiguated for some languages spoken in multiple countries and wherever no country match was found by algorithm
  private static final Map<Language, String> languageToCountryMap = MapUtils.enumMap(Language.class,
      ENGLISH, "us"/* also [US, ZA, NZ, PH, SG, IN, AU, GB, CA, MT, IE] */,
      AFRIKAANS, "za",
      ALBANIAN, "al",
      ARABIC, "eg"/* Egypt is the most populous; also [SD, JO, QA, LY, IQ, KW, TN, YE, DZ, OM, BH, LB, SY, AE, EG, MA, SA] */,
      BELARUSIAN, "by",
      BULGARIAN, "bg",
      CATALAN, "es",
      CHINESE, "cn"/* also [HK, SG, CN, TW] */,
      CHINESE_TRADITIONAL, "tw",
      CROATIAN, "hr",
      CZECH, "cz",
      DANISH, "dk",
      DUTCH, "nl"/* also [NL, BE] */,
      ESTONIAN, "ee",
      FILIPINO, "ph",
      FINNISH, "fi",
      FRENCH, "fr"/* also [FR, LU, CH, BE, CA] */,
      GALACIAN, "es" /* Galicia is an autonmous region of Spain */,
      GERMAN, "de"/* also [AT, LU, CH, DE] */,
      GREEK, "gr"/* also [CY, GR] */,
      HEBREW, "il",
      HINDI, "in",
      HUNGARIAN, "hu",
      INDONESIAN, "id",
      IRISH, "ie",
      ITALIAN, "it"/* also [CH, IT] */,
      JAPANESE, "jp",
      KOREAN, "kr",
      LATVIAN, "lv",
      LITHUANIAN, "lt",
      MACEDONIAN, "mk",
      MALAY, "my",
      MALTESE, "mt",
      NORWEGIAN, "no",
      PERSIAN, "ir",
      POLISH, "pl",
      PORTUGUESE, "pt"/* also [PT, BR] */,
      ROMANIAN, "ro",
      RUSSIAN, "ru",
      SERBIAN, "rs"/* also [RS, ME, CS, BA] */,
      SERBIAN_LATIN, "rs"/* also [RS, ME, CS, BA] */,
      SLOVAK, "sk",
      SLOVENIAN, "si",
      SPANISH, "es"/* also [AR, US, MX, HN, EC, VE, SV, GT, DO, NI, ES, PR, BO, PE, CR, CO, UY, PY, PA, CL] */,
      SWAHILI, "ke" /* also [TZ, UG, KE] */,
      SWEDISH, "se",
      THAI, "th",
      TURKISH, "tr",
      UKRAINIAN, "ua",
      VIETNAMESE, "vn",
      WELSH, "gb",
      YIDDISH, "il"
  );

  /**
   * @return The 2-char ISO code for the main country where the language
   * is spoken or originated, with the definition somewhat arbitrary.
   * @see CountryCodes for resolving the full
   * English names of these countries.
   */
  public String getMainCountry() {
    return languageToCountryMap.get(this);
  }

  /**
   * @return the lowercase chars of the official alphabet for this language
   * @throws UnsupportedOperationException if this language doesn't override this method, which could indicate that the
   * alphabet hasn't been provided for this language yet, or not representable as a string of normal Java {@code char}s
   * (i.e. it contains <a href="http://stn.audible.com/abcs-of-unicode/#java-and-unicode">supplementary characters</a>:
   * Unicode code points above <i>U+FFFF</i>)
   */
  public String getAlphabet() {
    throw new UnsupportedOperationException("No alphabet configured for " + name());
  }
}
