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

package solutions.trsoftware.commons.shared.text;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.util.ArrayUtils;
import solutions.trsoftware.commons.shared.util.Levenshtein;

import java.util.Arrays;
import java.util.List;

import static solutions.trsoftware.commons.shared.text.TypingLogFormatV1.formatTypingLog;
import static solutions.trsoftware.commons.shared.text.TypingLogFormatV1.parseTypingLog;

/**
 * Nov 23, 2012
 *
 * @author Alex
 */
public class TypingLogFormatV1Test extends TestCase {

  public void testParsing() throws Exception {
    // 1) test parsing of an artificial dummy log
    {
      // user typed just the first char in the text string "Other "
      String repr = "TLv1,en,6,O679t-679h0e0r0 0|0,1,679,0+O,";
      TypingLog parsedLog = parseTypingLog(repr);
      assertEquals("Other ", parsedLog.getText());
      assertEquals(Language.ENGLISH, parsedLog.getTextLanguage());
      assertEquals(Arrays.asList(679, 0, 0, 0, 0, 0), ArrayUtils.asList(parsedLog.getCharTimings()));
      assertEquals(
          Arrays.asList(new TypingEdit(0, Arrays.<Levenshtein.EditOperation>asList(new Levenshtein.Insertion(0, 'O')), 679)),
          parsedLog.getEditLog());
      assertEquals(repr, formatTypingLog(parsedLog));
    }
    // 2) test parsing of an actual typing log recorded in a race
    {
      String repr = "TLv1,en,396,I849m13957a0g4674i0n0e987 0a240 108v380a120s455t0 166s188h150e289e0t196 99o181f189 0p180a103p221e116r195 0o367n229 257w0h151i212c0h83 126s483t124r239a104i288g1072h152t132 360L116i224n201e358s0,153 105T448r271i217a0n346g0l161e342s0,198 104S837q406u119a136r147e412s0,150 122P465e206n0t137a149g420o0n221s105,1016 149H437e174x273a184g422o433n0s98,310 120a127n169d160 0o339t146h155e91r75 499f0i206g254u179r104e113s398,393 0i271n201s92t157e94a248d106 359o0f83 82r491e96m256a368i0n222i305n128g0 76f453i0x246e287d291 307i0n197 171t108h169e79i2098r96 151p251l261a81c230e95s329,137 91m196o191v64e90 89f400r270e96e249l231y329 0a453b171o104u248t1005,277 0o878n336 0o430r0 114i277n185 98t231h94e131 82s1045u152r1121f0a100c316e697,1270 160b719u0t154 139w186i124t191h126o156u91t141 935t0h383e216 177p559o266w150e0r106 205o221f173 0r664i129s304i0n485g223 0a506b950o0v786e159 385o484r253 0s665i117n248k475i233n96g0 72b310e94l384o232w129 862i0t218,936 0v374e193r0y214 139m287u322c224h197 906l478i276k236e247 0s804h197a546d277o337w402s0 1182\b-409 173o308n200l216y190 130h233a127r78d297 121a254n163d103 92w388i0t155h77 113l302u185m335i689n0o198u92s175 181e278d285g288e0s352 180\b-298 136a196n180d123 103y358o177u130 81w197i147l237l183 441t0h114e286n0 113h246a73v182e84 272a0 131p733r94e321t0t200y207 0c337o0r192r168e122c612t0 92n379o138t0i225o0n120 152o295f96 356m0y275 137c312o105u112n243t109r111y120 145a234n187d188 0c623o224u0n256t120r94y197m990e0n614.1336|0,8,849,0+I,13957,1+m2+a,1500,3+r,433,4+g,207,5+i,257,6+n,2277,3-r,987,6+e7+ ,8,2,240,0+a,108,1+ ,10,4,380,0+v,120,1+a,455,2+s3+t,166,4+ ,15,5,188,0+s,150,1+h,289,2+e3+e,196,4+t,99,5+ ,21,2,181,0+o,189,1+f2+ ,24,5,180,0+p,103,1+a,221,2+p,116,3+e,195,4+r5+ ,30,3,367,0+o,229,1+n,257,2+ 3+w,33,4,151,1+h,212,2+i3+c,83,4+h,126,5+ ,39,13,483,0+s,124,1+t,239,2+r,104,3+a,288,4+i,218,5+n,118,6+g,435,6-g,166,5-n,135,5+g,152,6+h,132,7+t,360,8+ ,48,6,116,0+L,224,1+i,201,2+n,358,3+e4+s,153,5+,,105,6+ ,55,8,448,0+T,271,1+r,217,2+i3+a,346,4+n5+g,161,6+l,342,7+e8+s,198,9+,,104,10+ ,66,8,837,0+S,406,1+q,119,2+u,136,3+a,147,4+r,412,5+e6+s,150,7+,,122,8+ ,75,11,465,0+P,206,1+e2+n,137,3+t,149,4+a,420,5+g6+o,221,7+n,105,8+s,248,9+.,463,9-.,305,9+,,149,10+ ,86,9,437,0+H,174,1+e,273,2+x,184,3+a,422,4+g,433,5+o6+n,98,7+s,310,8+,,120,9+ ,96,3,127,0+a,169,1+n,160,2+d3+ ,100,6,339,0+o,146,1+t,155,2+h,91,3+e,75,4+r,499,5+ 6+f,106,7,206,1+i,254,2+g,179,3+u,104,4+r,113,5+e,398,6+s,393,7+,8+ ,115,8,271,0+i,201,1+n,92,2+s,157,3+t,94,4+e,248,5+a,106,6+d,359,7+ 8+o,123,2,83,1+f,82,2+ ,126,8,491,0+r,96,1+e,256,2+m,368,3+a4+i,222,5+n,305,6+i,128,7+n8+g,76,9+ ,136,5,453,0+f1+i,246,2+x,287,3+e,291,4+d,307,5+ 6+i,142,2,197,1+n,171,2+ ,145,11,108,0+t,169,1+h,79,2+e,441,3+ 4+i,82,5+r,878,5-r,192,4-i,143,3- ,362,3+i,96,4+r,151,5+ ,151,8,251,0+p,261,1+l,81,2+a,230,3+c,95,4+e,329,5+s,137,6+,,91,7+ ,159,5,196,0+m,191,1+o,64,2+v,90,3+e,89,4+ ,164,6,400,0+f,270,1+r,96,2+e,249,2+e,231,4+l,329,5+y6+ ,171,6,453,0+a,171,1+b,104,2+o,248,3+u,1005,4+t,277,5+,6+ ,178,2,878,0+o,336,1+n2+ ,181,2,430,0+o1+r,114,2+ ,184,3,277,0+i,185,1+n,98,2+ ,187,4,231,0+t,94,1+h,131,2+e,82,3+ ,191,11,1045,0+s,152,1+u,233,2+f,436,2-f,452,2+r3+f,100,4+a,316,5+c,697,6+e7+ ,845,7- ,425,7+,,160,8+ ,200,3,719,0+b1+u,154,2+t,139,3+ ,204,8,186,0+w,124,1+i,191,2+t,126,3+h,156,4+o,91,5+u,141,6+t,935,6+t7+ ,212,3,383,1+h,216,2+e,177,3+ ,216,5,559,0+p,266,1+o,150,2+w3+e,106,4+r,205,5+ ,222,2,221,0+o,173,1+f2+ ,225,5,664,0+r,129,1+i,304,1+i2+s,485,4+n,223,5+g6+ ,232,5,506,0+a,950,1+b2+o,786,3+v,159,4+e,385,5+ ,238,2,484,0+o,253,1+r2+ ,241,7,665,0+s,117,1+i,248,2+n,475,3+k,233,4+i,96,5+n6+g,72,7+ ,249,6,310,0+b,94,1+e,384,2+l,232,3+o,129,4+w,862,5+ 6+i,255,2,218,1+t,936,2+,3+ ,259,4,374,0+v,193,1+e2+r,214,3+y,139,4+ ,264,5,287,0+m,322,1+u,224,2+c,197,3+h,906,4+ ,269,4,478,0+l,276,1+i,236,2+k,247,3+e4+ ,274,7,804,0+s,197,1+h,546,2+a,277,3+d,337,4+o,402,5+w6+s,1182,7+ ,282,2,409,0+-,173,1+ ,284,5,308,0+o,200,1+n,216,2+l,190,3+y,130,4+ ,289,5,233,0+h,127,1+a,78,2+r,297,3+d,121,4+ ,294,4,254,0+a,163,1+n,103,2+d,92,3+ ,298,4,388,0+w1+i,155,2+t,77,3+h,113,4+ ,303,8,302,0+l,185,1+u,335,2+m,689,3+i4+n,198,5+o,92,6+u,175,7+s,181,8+ ,312,5,278,0+e,285,1+d,288,2+g3+e,352,4+s,180,5+ ,318,2,298,0+-,136,1+ ,320,4,196,0+a,180,1+n,123,2+d,103,3+ ,324,4,358,0+y,177,1+o,130,2+u,81,3+ ,328,5,197,0+w,147,1+i,237,2+l,183,2+l,441,4+ 5+t,333,3,114,1+h,286,2+e3+n,113,4+ ,338,5,246,0+h,73,1+a,182,2+v,84,3+e,272,4+ 5+a,343,1,131,1+ ,345,5,733,0+p,94,1+r,321,2+e3+t,200,3+t,207,5+y6+ ,352,6,337,0+c1+o,192,2+r,168,2+r,122,4+e,612,5+c6+t,92,7+ ,360,5,379,0+n,138,1+o2+t,225,3+i4+o,120,5+n,152,6+ ,367,3,295,0+o,96,1+f,356,2+ 3+m,370,2,275,1+y,137,2+ ,373,8,312,0+c,105,1+o,112,2+u,243,3+n,109,4+t,111,5+r,120,6+y,145,7+ ,381,3,234,0+a,187,1+n,188,2+d3+ ,385,13,623,0+c,224,1+o2+u,256,3+n,120,4+t,94,5+r,197,6+y,137,7+ ,490,7- ,363,7+m8+e,614,9+n,163,10+t,707,10-t,466,10+.,";
      TypingLog parsedLog = parseTypingLog(repr);
      System.out.println("The length of a realistic TypingLog over a text of length " + parsedLog.getText().length() + " is " + repr.length());
      assertEquals(repr, formatTypingLog(parsedLog));
    }
  }

  public void testFormatting() throws Exception {
    List<String> dummyTexts = Arrays.asList(
        // 1) test a very basic typing log
        "What should",
        // 2) test a typing log for a text that contains numbers (to make sure the formatting/parsing is able to distinguish
        // between number chars in the underlying text from time values
        "Testing 123, testing - 123"
    );
    for (String textTyped : dummyTexts) {
      String repr = formatTypingLog(TypingLogUtils.dummyTypingLog(Language.ENGLISH, textTyped, 200));
      System.out.println("String repr of a dummy typing log over the text \"" + textTyped + "\":");
      System.out.println(repr);
      TypingLog parsedLog = parseTypingLog(repr);
      assertEquals(repr, formatTypingLog(parsedLog));
    }
  }
}