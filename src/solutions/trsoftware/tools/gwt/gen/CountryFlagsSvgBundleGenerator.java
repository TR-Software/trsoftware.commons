package solutions.trsoftware.tools.gwt.gen;

import solutions.trsoftware.commons.server.io.SplitterOutputStream;

import java.io.File;
import java.io.FileOutputStream;
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
    try (Writer out = new OutputStreamWriter(
        new SplitterOutputStream(
            System.out,
            new FileOutputStream(outputFile, false)
        ))) {
      generator.generateBundleClass(out);
    }
    System.out.println("// Output written to " + outputFile);
  }

}
