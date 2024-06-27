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

package solutions.trsoftware.tools.util;

import junit.framework.TestCase;
import solutions.trsoftware.commons.server.io.StringPrintStream;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static solutions.trsoftware.commons.server.io.SplitterOutputStream.teeTo;
import static solutions.trsoftware.tools.util.TablePrinter.printMenu;

/**
 * @author Alex
 * @since 5/13/2018
 */
public class TablePrinterTest extends TestCase {

  public void testPrintMenu() throws Exception {
    // print a table of all the available locales in Java
    Locale[] availableLocales = Locale.getAvailableLocales();
    List<String> lines = new ArrayList<>();
    for (Locale locale : availableLocales) {
      lines.add(format("%s: displayName=%s, country=%s, language=%s",
          locale,
          locale.getDisplayName(),
          locale.getDisplayCountry(),
          locale.getDisplayLanguage()
      ));
    }
    printMenu(System.out, "All Available Locales:", lines);
    // TODO: verify some assertions?
  }

  public void testTablePrinter() throws Exception {
    // TODO: temp experiment
    // 1) simple table
    {
      TablePrinter printer = new TablePrinter();
      StringPrintStream out = new StringPrintStream();
      List<List<String>> expectedLineTokens = new ArrayList<>();
      expectedLineTokens.add(Arrays.asList("x", "sin(x)", "cos(x)"));  // col headings
      for (int i = 0; i < 10; i++) {
        printer.newRow();
        ArrayList<String> tokens = new ArrayList<>();
        printer
            .addCol("x", "%d", i)
            .addCol("sin(x)", "%f", Math.sin(i))
            .addCol("cos(x)", "%.2f", Math.cos(i))
        ;
        expectedLineTokens.add(Arrays.asList(
            format("%d", i),
            format("%f", Math.sin(i)),
            format("%.2f", Math.cos(i))
        ));
      }
      printer.printTable(teeTo(out));
      validateTableOutput(out.toString(), expectedLineTokens);
    }
    // 2) sparse table
    {
      // TODO: extract dup code in preceding block
      TablePrinter printer = new TablePrinter();
      StringPrintStream out = new StringPrintStream();
      List<List<String>> expectedLineTokens = new ArrayList<>();
      expectedLineTokens.add(Arrays.asList("x", "sin(x)", "cos(x)", "tan(x)"));  // col headings
      for (int i = 0; i < 10; i++) {
        List<String> tokens = new ArrayList<>();
        printer.newRow();
        printer.addCol("x", "%d", i);
        tokens.add(format("%d", i));
        if (i % 2 == 0) {
          printer.addCol("sin(x)", "%f", Math.sin(i));
          tokens.add(format("%f", Math.sin(i)));
        }
        if (i % 2 == 1) {
//        if (i % 2 == 1) {
          printer.addCol("cos(x)", "%.2f", Math.cos(i));
          tokens.add(format("%.2f", Math.cos(i)));
        }
        if (i % 3 == 1) {
          printer.addCol("tan(x)", "%.2f", Math.tan(i));
          tokens.add(format("%.2f", Math.tan(i)));
        }
        expectedLineTokens.add(tokens);
      }
      printer.printTable(teeTo(out));
      validateTableOutput(out.toString(), expectedLineTokens);
    }
  }

  private void validateTableOutput(String output, List<List<String>> expectedLineTokens) throws IOException {
    List<Pattern> linePatterns = expectedLineTokens.stream()
        .map(tokenList -> tokenList.stream()
            .map(Pattern::quote).collect(Collectors.joining("\\W+", "\\W+", "\\W+"))
        )
        .map(Pattern::compile)
        .collect(Collectors.toList());
    /*System.out.println("lineRegexes:");
    linePatterns.stream().map(Object::toString).map(StringUtils.indenting(2)).forEach(System.out::println);*/
    int linesMatched = 0;
    int trailingLines = 0;
    BufferedReader br = new BufferedReader(new StringReader(output));
    int i = 0;
    for (String line = br.readLine(); line != null; line = br.readLine(), i++) {
      if (linesMatched == linePatterns.size()) {
        // matched all the lines with tokens: the rest of the input should be either empty or formatting chars (e.g. bottom border)
        trailingLines++;
        assertTrue(line.matches("\\W*"));
        continue;
      }
      Pattern pattern = linePatterns.get(linesMatched);
      if (pattern.matcher(line).matches()) {
//        System.out.printf("Line %d matches tokens %s ->%n %s%n", i, expectedLineTokens.get(linesMatched), line);
        linesMatched++;
      }
    }
    assertEquals("Output did not contain all of the expected lines",
        linePatterns.size(), linesMatched);
    AssertUtils.assertThat(trailingLines).isLessThanOrEqualTo(1);
  }
}