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

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource.DoNotEmbed;
import com.google.gwt.resources.client.DataResource.MimeType;
import com.google.gwt.resources.ext.AbstractResourceGenerator;
import com.google.gwt.resources.ext.ResourceContext;
import com.google.gwt.resources.ext.ResourceGeneratorUtil;
import com.google.gwt.resources.ext.SupportsGeneratorResultCaching;
import com.google.gwt.resources.rg.DataResourceGenerator;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.StringSourceWriter;
import solutions.trsoftware.commons.client.bundle.SvgImageResource;
import solutions.trsoftware.commons.client.bundle.SvgImageResourcePrototype;

import java.net.URL;

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
}
