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

package solutions.trsoftware.commons.server.util;


import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.shared.util.Levenshtein;

import javax.lang.model.SourceVersion;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static solutions.trsoftware.commons.shared.util.StringUtils.isBlank;
import static solutions.trsoftware.commons.shared.util.StringUtils.repeat;

/**
 * Date: May 13, 2008 Time: 6:18:12 PM
 *
 * @author Alex
 */
public class ServerStringUtils {

  /**
   * Returns the base64 encoding of the input encrypted with the desired number of
   * iterations of the SHA256 one-way hash.
   */
  public static String hashSHA256(String plaintext, int iterations) {
    try {
      byte[] hashBytes = stringToBytesUtf8(plaintext);  // allows non-ASCII passwords
      for (int i = 0; i < iterations; i++) {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(hashBytes);
        hashBytes = md.digest();
      }
      return new String(Base64.encodeBase64(hashBytes));
    }
    catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
  }

  /** Returns the UTF-8 encoding of the given string. */
  public static byte[] stringToBytesUtf8(String str) {
    try {
      return str.getBytes(ServerIOUtils.UTF8_CHARSET_NAME);
    }
    catch (UnsupportedEncodingException e) {
      // will never happen - all Java VMs support UTF-8
      throw new RuntimeException(e);
    }
  }

  /** Returns a String represented by the give UTF-8 bytes. */
  public static String bytesToStringUtf8(byte[] utf8Bytes) {
    try {
      return new String(utf8Bytes, ServerIOUtils.UTF8_CHARSET_NAME);
    }
    catch (UnsupportedEncodingException e) {
      // will never happen - all java VM's support UTF-8
      throw new RuntimeException(e);
    }
  }

  /**
   * Encodes the given bytes with {@link UrlSafeBase64}.
   */
  public static String urlSafeBase64Encode(byte[] bytes) {
    return bytesToStringUtf8(UrlSafeBase64.encodeBase64(bytes));
  }

  /**
   * @param str binary data encoded with {@link UrlSafeBase64}.
   * @return the original binary data.
   */
  public static byte[] urlSafeBase64Decode(String str) {
    return UrlSafeBase64.decodeBase64(stringToBytesUtf8(str));
  }


  /**
   * @return the base64 encoding of the input encrypted with SHA one-way hash.
   */
  public static String hashSHA256(String plaintext) {
    return hashSHA256(plaintext, 1);
  }

  /** URL-decodes the given string as UTF-8 */
  public static String urlDecode(String str) {
    try {
      return URLDecoder.decode(str, ServerIOUtils.UTF8_CHARSET_NAME);
    }
    catch (UnsupportedEncodingException e) {
      // will never happen - all Java VMs support UTF-8
      throw new RuntimeException(e);
    }
    catch (IllegalArgumentException e) {
      return str; // return the original string if the decoding fails
    }
  }

  /** URL-encodes the given string as UTF-8 */
  public static String urlEncode(String str) {
    try {
      return URLEncoder.encode(str, ServerIOUtils.UTF8_CHARSET_NAME);
    }
    catch (UnsupportedEncodingException e) {
      // will never happen - all Java VMs support UTF-8
      throw new RuntimeException(e);
    }
  }

  /** Debugging util method that prints a w3c DOM Document to a string */
  public static String dumpDocument(Document doc, boolean indent) {
    // TODO: WARNING: this method has not been tested
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      if (indent)
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      StringWriter swriter = new StringWriter();
      transformer.transform(new DOMSource(doc), new StreamResult(swriter));
      swriter.flush();
      return swriter.getBuffer().toString();
    }
    catch (TransformerException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /** Returns all the matches of the given regex in the given string */
  public static List<String> findAll(String str, String regex) {
    return findAll(str, regex, 0);
  }

  /** Returns all the matches of the given regex in the given string */
  public static List<String> findAll(String str, Pattern regex) {
    return findAll(str, regex, 0);
  }

  /** Returns all the matches of the given capturing group in the given regex in the given string */
  public static List<String> findAll(String str, String regex, int group) {
    return findAll(str, Pattern.compile(regex), group);
  }

  /** Returns all the matches of the given capturing group in the given regex in the given string */
  public static List<String> findAll(String str, Pattern regex, int group) {
    ArrayList<String> results = new ArrayList<String>();
    Matcher matcher = regex.matcher(str);
    while (matcher.find()) {
      results.add(matcher.group(group));
    }
    return results;
  }

  /**
   * Similar to the Javascript regex match method. This is useful when using a
   * debugger's expression evaluator.
   * @return an array of all the capturing groups if the given regex matches
   * the string, otherwise null.
   */
  public static String[] match(String str, String regex) {
    return match(str, Pattern.compile(regex));
  }

  /**
   * Similar to the Javascript regex match method. This is useful when using a
   * debugger's expression evaluator.
   * @return an array of all the capturing groups if the given regex matches
   * the string, otherwise null.
   */
  public static String[] match(String str, Pattern regex) {
    Matcher matcher = regex.matcher(str);
    if (!matcher.matches())
      return null;
    String[] groups = new String[matcher.groupCount()+1];
    for (int i = 0; i < groups.length; i++) {
      groups[i] = matcher.group(i);
    }
    return groups;
  }

  /** Compresses the string using java.zip.DeflaterOutputStream */
  public static byte[] deflateString(String str) {
    // NOTE: DeflaterOutputStream provides better compression than GZIPOutputStream because the latter writes an additional 10-byte header
    DeflaterOutputStream zipOut = null;
    try {
      ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(str.length());
      zipOut = new DeflaterOutputStream(outputBuffer);
      zipOut.write(stringToBytesUtf8(str));
      zipOut.finish();  // must remember to call finish when using DeflaterOutputStream
      zipOut.flush();
      return outputBuffer.toByteArray();
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    finally {
      if (zipOut != null)
        try {
          zipOut.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
    }
  }

  /** Reverses the action of deflateString */
  public static String inflateString(byte[] gzippedBytes) {
    try {
      return ServerIOUtils.readCharactersIntoString(new InputStreamReader(
          new InflaterInputStream(new ByteArrayInputStream(gzippedBytes)),
          ServerIOUtils.UTF8_CHARSET_NAME));
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /**
   * Iterates over the given strings and returns those that are within
   * maxEditDistance from the query, sorted by edit distance, in ascending order.
   */
  public static List<String> searchByEditDistance(Collection<String> strings, String query, int maxEditDistance) {
    SortedMap<Integer, List<String>> stringsByDistace = new TreeMap<Integer, List<String>>();
    List<String> results = new ArrayList<String>();
    for (String str : strings) {
      int dist = Levenshtein.editDistance(str, query);
      if (dist <= maxEditDistance) {
        ServerMapUtils.getOrInsert(stringsByDistace, dist, (Class<? extends List<String>>)results.getClass()).add(str);
      }
    }
    for (List<String> stringList : stringsByDistace.values()) {
      results.addAll(stringList);
    }
    return results;
  }


  /**
   * Quotes string as Java Language string literal. Returns string
   * <code>"null"</code> if <code>s</code> is <code>null</code>.
   *
   * This code is borrowed from freemarker.template.utility.StringUtil (see
   * http://freemarker.org)
   */
  public static String jQuote(String s) {
    if (s == null) {
      return "null";
    }
    int ln = s.length();
    StringBuilder b = new StringBuilder(ln + 4);
    b.append('"');
    for (int i = 0; i < ln; i++) {
      char c = s.charAt(i);
      if (c == '"') {
        b.append("\\\"");
      }
      else if (c == '\\') {
        b.append("\\\\");
      }
      else if (c < 0x20) {
        if (c == '\n') {
          b.append("\\n");
        }
        else if (c == '\r') {
          b.append("\\r");
        }
        else if (c == '\f') {
          b.append("\\f");
        }
        else if (c == '\b') {
          b.append("\\b");
        }
        else if (c == '\t') {
          b.append("\\t");
        }
        else {
          b.append("\\u00");
          int x = c / 0x10;
          b.append((char)(x < 0xA ? x + '0' : x - 0xA + 'A'));
          x = c & 0xF;
          b.append((char)(x < 0xA ? x + '0' : x - 0xA + 'A'));
        }
      }
      else {
        b.append(c);
      }
    } // for each characters
    b.append('"');
    return b.toString();
  }

  /**
   * Escapes the <code>String</code> with the escaping rules of Java
   * language string literals, so it is safe to insert the value into a
   * string literal. The resulting string will not be quoted.
   *
   * <p>In additional, all characters under UCS code point 0x20, that has no
   * dedicated escape sequence in Java language, will be replaced with
   * UNICODE escape (<tt>\<!-- -->u<i>XXXX</i></tt>).
   *
   * This code is borrowed from freemarker.template.utility.StringUtil (see
   * http://freemarker.org)
   *
   * @see #jQuote(String)
   */
  public static String javaStringEnc(String s) {
    int ln = s.length();
    for (int i = 0; i < ln; i++) {
      char c = s.charAt(i);
      if (c == '"' || c == '\\' || c < 0x20) {
        StringBuilder b = new StringBuilder(ln + 4);
        b.append(s.substring(0, i));
        while (true) {
          if (c == '"') {
            b.append("\\\"");
          }
          else if (c == '\\') {
            b.append("\\\\");
          }
          else if (c < 0x20) {
            if (c == '\n') {
              b.append("\\n");
            }
            else if (c == '\r') {
              b.append("\\r");
            }
            else if (c == '\f') {
              b.append("\\f");
            }
            else if (c == '\b') {
              b.append("\\b");
            }
            else if (c == '\t') {
              b.append("\\t");
            }
            else {
              b.append("\\u00");
              int x = c / 0x10;
              b.append((char)
                  (x < 0xA ? x + '0' : x - 0xA + 'a'));
              x = c & 0xF;
              b.append((char)
                  (x < 0xA ? x + '0' : x - 0xA + 'a'));
            }
          }
          else {
            b.append(c);
          }
          i++;
          if (i >= ln) {
            return b.toString();
          }
          c = s.charAt(i);
        }
      } // if has to be escaped
    } // for each characters
    return s;
  }

  /**
   * Escapes a <code>String</code> according the JavaScript string literal
   * escaping rules. The resulting string will not be quoted.
   *
   * <p>It escapes both <tt>'</tt> and <tt>"</tt>. In additional it escapes
   * <tt>></tt> as <tt>\></tt> (to avoid <tt>&lt;/script></tt>).
   * Furthermore, all characters under UCS code point 0x20, that has no
   * dedicated escape sequence in JavaScript language, will be replaced with
   * hexadecimal escape (<tt>\x<i>XX</i></tt>).
   *
   * This code is borrowed from freemarker.template.utility.StringUtil (see
   * http://freemarker.org)
   */
  public static String javaScriptStringEnc(String s) {
    int ln = s.length();
    for (int i = 0; i < ln; i++) {
      char c = s.charAt(i);
      if (c == '"' || c == '\'' || c == '\\' || c == '>' || c < 0x20) {
        StringBuilder b = new StringBuilder(ln + 4);
        b.append(s.substring(0, i));
        while (true) {
          if (c == '"') {
            b.append("\\\"");
          }
          else if (c == '\'') {
            b.append("\\'");
          }
          else if (c == '\\') {
            b.append("\\\\");
          }
          else if (c == '>') {
            b.append("\\>");
          }
          else if (c < 0x20) {
            if (c == '\n') {
              b.append("\\n");
            }
            else if (c == '\r') {
              b.append("\\r");
            }
            else if (c == '\f') {
              b.append("\\f");
            }
            else if (c == '\b') {
              b.append("\\b");
            }
            else if (c == '\t') {
              b.append("\\t");
            }
            else {
              b.append("\\x");
              int x = c / 0x10;
              b.append((char)
                  (x < 0xA ? x + '0' : x - 0xA + 'A'));
              x = c & 0xF;
              b.append((char)
                  (x < 0xA ? x + '0' : x - 0xA + 'A'));
            }
          }
          else {
            b.append(c);
          }
          i++;
          if (i >= ln) {
            return b.toString();
          }
          c = s.charAt(i);
        }
      } // if has to be escaped
    } // for each characters
    return s;
  }

  /**
   * Converts the given string to Java string literal form (surrounded by quotes),
   * where all the non-ASCII chars are escaped with their unicode sequence.
   */
  public static String toUnicodeLiteral(String input) {
    // 1) first, turn the string into a literal
    input = jQuote(input);
    // 2) now, escape all the unicode chars
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      int codePoint = Character.codePointAt(input, i);
      if (codePoint > 127) {
        out.append("\\u");
        String hexString = Integer.toHexString(codePoint);
        int len = hexString.length();
        if (len > 4)
          throw new IllegalStateException("Weren't expecting the hex string for any character to be longer than 4 chars: " + hexString);
        else if (len < 4)
          out.append(repeat('0', 4 - len));  // pad with 0s
        out.append(hexString);
      }
      else
        out.append(input.charAt(i));  // this is an ASCII-range char, output it as-is
    }
    return out.toString();
  }

  /**
   * @return the closest string resembling str that can be used as a valid
   * Java variable, method, etc. name in source code.
   */
  public static String toJavaNameIdentifier(String str) {
    if (isBlank(str))
      return "_";
    if (SourceVersion.isName(str))
      return str;  // already valid

    StringBuilder builder = new StringBuilder(str);
    // we prepend a valid starting char "_" if the first char isn't valid
    if (!Character.isJavaIdentifierStart(builder.charAt(0)))
        builder.setCharAt(0, '_');
    // and replace all the invalid chars in the name
    for (int i = 1; i < builder.length(); i++) {
      char c = builder.charAt(i);
      if (!Character.isJavaIdentifierPart(c))
        builder.setCharAt(i, '_');
    }

    // now, if we still don't have a valid name, we probably have a reserved
    // keyword or something, so we just prepend it with a '_'
    String result = builder.toString();
    if (SourceVersion.isName(result))
      return result;
    return "_" + result;
  }
}
