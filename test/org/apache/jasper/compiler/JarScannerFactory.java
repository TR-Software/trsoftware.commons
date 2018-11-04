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
package org.apache.jasper.compiler;

import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.scan.StandardJarScanner;

import javax.servlet.ServletContext;

/**
 * Provide a mechanism for Jasper to obtain a reference to the JarScanner
 * implementation.
 *
 * <p style="color: #6495ed; font-weight: bold;">
 *   NOTE(alex.epshteyn): providing a different implementation of this class to avoid spurious exceptions logged
 *   due to JAR scanning (e.g. FileNotFoundExceptions from trying to load non-existent JARs just because
 *   they were mentioned in the manifest of another jar).
 *
 *   See {@link #configureJarScanner()}
 * </p>
 */
public class JarScannerFactory {

  private JarScannerFactory() {
    // Don't want any instances so hide the default constructor.
  }

  /**
   * Obtain the {@link JarScanner} associated with the specified {@link
   * ServletContext}. It is obtained via a context parameter.
   *
   * @param ctxt The Servlet context
   * @return a scanner instance
   */
  public static JarScanner getJarScanner(ServletContext ctxt) {
    JarScanner jarScanner =
        (JarScanner)ctxt.getAttribute(JarScanner.class.getName());
    if (jarScanner == null) {
      ctxt.log(Localizer.getMessage("jsp.warning.noJarScanner"));
      jarScanner = configureJarScanner();
    }
    return jarScanner;
  }

  /**
   * These are normally configured in the {@code /META-INF/context.xml} 
   * (under the <a href="https://tomcat.apache.org/tomcat-8.5-doc/config/jar-scanner.html">JarScanner component</a>),
   * but since we can't do that while running GWT tests (which use an embedded jetty server), the only thing we can
   * do is to put a copy of this class into our own project, and configure the settings programmatically.
   * We set all of these attributes to {@code false}, to speed up the unit test startup and avoid spurious exceptions.
   */
  private static StandardJarScanner configureJarScanner() {
    StandardJarScanner jarScanner = new StandardJarScanner();
    jarScanner.setScanAllDirectories(false);
    jarScanner.setScanAllFiles(false);
    jarScanner.setScanClassPath(false);
    jarScanner.setScanBootstrapClassPath(false);
    jarScanner.setScanManifest(false);
    return jarScanner;
  }

}
