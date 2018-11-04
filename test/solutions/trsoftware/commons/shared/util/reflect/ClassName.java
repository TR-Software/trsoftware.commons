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

import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.text.Alphabet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex
 * @since 12/25/2017
 */
class ClassName {
  final String fullName;
  final String pkg;
  final String complex;
  final String simple;
  final String anonId;
  final boolean anon;

  ClassName(String fullName, String pkg, String complex, String simple, String anonId, boolean anon) {
    this.fullName = fullName;
    this.pkg = pkg;
    this.complex = complex;
    this.simple = simple;
    this.anonId = anonId;
    this.anon = anon;
  }


  static List<ClassName> randomExamples(int maxPkgDepth, int maxNestingDepth, boolean anon) {
    ArrayList<ClassName> ret = new ArrayList<ClassName>(maxPkgDepth * maxNestingDepth);
    for (int p = 0; p < maxPkgDepth; p++) {
      // generate a path containing p sub-packages
      String pkgName = randomPackageName(p);
      for (int c = 0; c < maxNestingDepth; c++) {
        String complexName = randomComplexName(c);
        String simpleName;
        String anonId;
        if (anon) {
          simpleName = "";
          anonId = RandomUtils.randString(Alphabet.NUMBERS.getChars(), 1, 5);
          complexName += "$" + anonId;
        }
        else {
          simpleName = complexName.substring(complexName.lastIndexOf('$') + 1);
          anonId = "";
        }
        String fullName = pkgName;
        if (!pkgName.isEmpty())
          fullName += ".";
        fullName += complexName;
        ret.add(new ClassName(fullName, pkgName, complexName, simpleName, anonId, anon));
      }
    }
    return ret;
  }

  /**
   * @return A package path containing the given number of sub-packages.
   */
  static String randomPackageName(int depth) {
    if (depth == 0)
      return "";
    return StringUtils.join(".", randomIdentifiers(depth));
  }

  /**
   * @return A "complex name" for a class nested at the given depth (if depth is 0, it's an upper-level class, otherwise inner)
   */
  static String randomComplexName(int depth) {
    if (depth == 0)
      return randomIdentifier();
    return StringUtils.join("$", randomIdentifiers(depth));
  }

  static String randomIdentifier() {
    return RandomUtils.randString(StringUtils.ASCII_LETTERS, 1, 5);
  }

  static List<String> randomIdentifiers(int n) {
    ArrayList<String> ret = new ArrayList<String>(n);
    for (int i = 0; i < n; i++)
      ret.add(randomIdentifier());
    return ret;
  }

}
