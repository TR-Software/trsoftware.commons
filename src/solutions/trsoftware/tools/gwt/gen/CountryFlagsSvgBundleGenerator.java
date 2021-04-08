/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.tools.gwt.gen;

import solutions.trsoftware.commons.server.io.SplitterOutputStream;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Invokes {@link ClientBundleCodeGenerator} to generate
 * {@link solutions.trsoftware.commons.client.images.flags_svg.CountryFlagsSvgBundle}, using the SVG flag images
 * downloaded from the <a href="https://github.com/lipis/flag-icon-css/tree/master/flags/4x3">flag-icon-css project on GitHub</a>.
 * @author Alex
 * @since 2/10/2018
 */
public class CountryFlagsSvgBundleGenerator {

  public static void main(String[] args) throws Exception {
    String srcPath;
    if (args.length > 0)
      srcPath = args[0];
    else
      srcPath = "C:\\Users\\Alex\\Documents\\Projects\\TR Commons\\src"; // default value on my machine

    String pkgName = "solutions.trsoftware.commons.client.images.flags_svg";
    ClientBundleCodeGenerator generator = new ClientBundleCodeGenerator(
        srcPath, pkgName, "CountryFlagsSvgBundle", "\\w{2}.svg", true);
    generator.setJavadoc("<p>\n" +
        "  Bundles the SVG flag images downloaded from the \n" +
        "  <a href=\"https://github.com/lipis/flag-icon-css/tree/master/flags/4x3\">flag-icon-css project on GitHub</a>\n" +
        "</p>\n");
    File outputFile = generator.getOutputFile();
    try (Writer out = new OutputStreamWriter(SplitterOutputStream.teeToFile(outputFile))) {
      generator.generateBundleClass(out);
    }
    System.out.println("// Output written to " + outputFile);
  }

}
