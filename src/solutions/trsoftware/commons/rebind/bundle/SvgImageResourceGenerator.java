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
package solutions.trsoftware.commons.rebind.bundle;

import com.google.common.net.PercentEscaper;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.dev.util.Util;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource.DoNotEmbed;
import com.google.gwt.resources.client.DataResource.MimeType;
import com.google.gwt.resources.ext.AbstractResourceGenerator;
import com.google.gwt.resources.ext.ResourceContext;
import com.google.gwt.resources.ext.ResourceGeneratorUtil;
import com.google.gwt.resources.ext.SupportsGeneratorResultCaching;
import com.google.gwt.resources.gss.ResourceUrlFunction;
import com.google.gwt.resources.rg.DataResourceGenerator;
import com.google.gwt.resources.rg.GssResourceGenerator;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.StringSourceWriter;
import solutions.trsoftware.commons.client.bundle.SvgImageResource;
import solutions.trsoftware.commons.client.bundle.SvgImageResourcePrototype;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

// NOTE: this code was borrowed from com.google.gwt.resources.rg.DataResourceGenerator

/**
 * Generates implementations of {@link SvgImageResource} in a {@link ClientBundle}.
 *
 * @see DataResourceGenerator
 */
public final class SvgImageResourceGenerator extends AbstractResourceGenerator implements
    SupportsGeneratorResultCaching {

  public static final String DEFAULT_MIME_TYPE = "image/svg+xml";

  @Override
  public String createAssignment(TreeLogger logger, ResourceContext context, JMethod method)
      throws UnableToCompleteException {

    URL[] resources = ResourceGeneratorUtil.findResources(logger, context, method);

    if (resources.length != 1) {
      logger.log(TreeLogger.ERROR, "Exactly one resource must be specified", null);
      throw new UnableToCompleteException();
    }

    // Determine if a MIME Type has been specified
    MimeType mimeTypeAnnotation = method.getAnnotation(MimeType.class);
    String mimeType = mimeTypeAnnotation != null ? mimeTypeAnnotation.value() : DEFAULT_MIME_TYPE;

    // Determine if the resource should not be embedded
    DoNotEmbed doNotEmbed = method.getAnnotation(DoNotEmbed.class);
    boolean forceExternal = (doNotEmbed != null);

    URL resource = resources[0];
    String outputUrlExpression = context.deploy(resource, mimeType, forceExternal);

//    outputUrlExpression = maybeMinifyDataURI(logger, mimeType, resource, outputUrlExpression);

    SourceWriter sw = new StringSourceWriter();
    // Convenience when examining the generated code.
    if (!AbstractResourceGenerator.STRIP_COMMENTS) {
      sw.println("// " + resource.toExternalForm());
    }
    sw.println("new " + SvgImageResourcePrototype.class.getName() + "(");
    sw.indent();
    sw.println('"' + method.getName() + "\",");
    sw.println(UriUtils.class.getName() + ".fromTrustedString(" + outputUrlExpression + ")");
    sw.outdent();
    sw.print(")");

    return sw.toString();
  }

  /**
   * Experimental attempt to apply the technique described in
   * <a href="https://codepen.io/tigt/post/optimizing-svgs-in-data-uris">Optimizing SVGs in data URIs</a> to
   * reduce the size of a base64-encoded data URI generated for the given SVG resource by {@code InlineResourceContext}.
   * <p>
   * If this technique is able to produce a shorter string than the given base64 data URI returned by
   * {@link ResourceContext#deploy(URL, String, boolean)}, will return the shorter URI, otherwise will return
   * the given base64 URI.
   * <p></p>
   * <b>Note:</b> a non-base64 data URI must enclosed in double quotes when used with the {@code url()} function in CSS,
   * and is therefore not compatible with GWT's {@link GssResourceGenerator} and its {@link ResourceUrlFunction resourceUrl} function.
   */
  public String maybeMinifyDataURI(TreeLogger logger, String mimeType, URL svgResource, String base64DataUri) throws UnableToCompleteException {
    if (base64DataUri.startsWith("\"data:")) {
      /* context produced a base64-encoded data URI (see InlineResourceContext), but we might be able to shorten it
         by encoding the SVG as plain text instead of base64 (see https://codepen.io/tigt/post/optimizing-svgs-in-data-uris)
       */
      String plaintextDataURI = resourceToDataURI(logger, svgResource, mimeType);

      // use the shorter one of the 2 (base64 vs plaintext)
      if (plaintextDataURI != null) {
        int lengthDiff = plaintextDataURI.length() - base64DataUri.length();
        boolean isShorter = lengthDiff < 0;
        logger.log(TreeLogger.DEBUG,
            String.format("Plaintext SVG data URI (%,d bytes) is %,d bytes %s than base64 (%,d bytes): url(%s) vs. url(%s)",
                plaintextDataURI.length(), Math.abs(lengthDiff),
                isShorter ? "shorter" : "longer", base64DataUri.length(),
                plaintextDataURI, base64DataUri));
        if (isShorter)
          base64DataUri = plaintextDataURI;
      }
      // TODO: needs to be quoted in stylesheet (e.g. background-image: url("..."))
      /* TODO(2/24/2025): revert this experiment:
          - can't make it work with com.google.gwt.resources.gss.ResourceUrlFunction and com.google.gwt.resources.gss.CssPrinter:
            no way to get them to quote the argument to the CSS url() function); would have to create a whole new CssResourceGenerator implementation
          - if we quote outputUrlExpression here, it would work for generated CSS, but fail when using DataResource.getSafeUri in Java code (e.g. PauseResumeButton.getHtml)
          - even if could make it work, it would only save 3.4 KB in the TypingSnake project, so not a big deal
       */
    }
    return base64DataUri;
  }

  @Nullable
  private String resourceToDataURI(TreeLogger logger, URL resource, String mimeType) throws UnableToCompleteException {
    try {
      String content = Util.readURLAsString(resource);
      String finalMimeType = (mimeType != null)
          ? mimeType : resource.openConnection().getContentType();
      if (content != null && finalMimeType != null)
        return svgToDataURI(content, finalMimeType);
      else
        return null;
    } catch (IOException e) {
      logger.log(TreeLogger.ERROR,
          "Unable to determine mime type of resource", e);
      throw new UnableToCompleteException();
    }
  }

  /**
   * Creates a non-base64 {@code data:} URI for the given SVG, using the technique described in
   * <a href="https://codepen.io/tigt/post/optimizing-svgs-in-data-uris">Optimizing SVGs in data URIs</a>.
   * <p>
   * <b>Note:</b> this data URI must enclosed in double quotes if used with the {@code url()} function in CSS, and is
   * therefore not compatible with GWT's {@link GssResourceGenerator}.
   */
  public static String svgToDataURI(@Nonnull String svgString, @Nonnull String mimeType) {
    String singleQuoted = replaceQuotes(svgString);
//    System.out.println("singleQuoted:\n" + singleQuoted);

    // TODO: experimental: find a suitable safeChars value for PercentEscaper:
    String URL_PATH_OTHER_SAFE_CHARS_LACKING_PLUS =
        "-._~" // Unreserved characters.
            + "!$'()*,;&=" // The subdelim characters (excluding '+').
            + "@:"; // The gendelim characters permitted in paths.
    String safeChars = URL_PATH_OTHER_SAFE_CHARS_LACKING_PLUS + "+/? ";
    PercentEscaper percentEscaper = new PercentEscaper(safeChars, false);
    String escapedSvg = percentEscaper.escape(singleQuoted);
//    System.out.println("escapedSvg:\n" + escapedSvg);
    /* TODO: experimental: quote the string again b/c needs to be quoted in stylesheet (e.g. background-imange: url("..."))
        - cont here: this works for generated CSS, but fails when using DataResource.getSafeUri in Java code (e.g. PauseResumeButton.getHtml)
     */
    String dataUri = "\""
//        + "\\\""
        + "data:"
        + mimeType.replaceAll("\"", "\\\\\"")
        + "," + escapedSvg
//        + "\\\""
        + "\"";

//    System.out.println("dataUri:\n" + dataUri);
    return dataUri;
    // TODO: could improve speed by using StringBuilder in constructing the data URI and in replaceQuotes (via Matcher.appendReplacement)
  }

  /**
   * Replaces all double-quoted attributes with single quotes.
   * Example: {@code <tag a="foo", b="bar"/>} &rarr; {@code <tag a='foo', b='bar'/>}
   * @param markup XML/HTML fragment
   */
  public static String replaceQuotes(String markup) {
    Pattern pattern = Pattern.compile("(\\w+=)\"(.*?)\"");
    return pattern.matcher(markup).replaceAll("$1'$2'");
  }
}
