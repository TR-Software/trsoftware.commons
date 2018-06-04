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

package solutions.trsoftware.commons.server.testutil;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.WebXml;
import org.apache.tomcat.util.descriptor.web.WebXmlParser;
import solutions.trsoftware.commons.server.io.file.FileUtils;
import solutions.trsoftware.commons.server.servlet.filters.CachePolicyFilterTest;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Skeleton implementation of an embedded Tomcat server.
 *
 * <h3>Usage</h3>
 * Refer to the code in {@link CachePolicyFilterTest#testIntegration()} and the webapp defined in
 * {@code /test-resources/CachePolicyFilterTest_webapp}.
 * <p>
 * <strong>NOTE</strong>: the {@code Context} element (defined in your {@code context.xml}) should have the attribute
 * {@code containerSciFilter="jetty"}, otherwise the {@link ServletContainerInitializer}s found in the jetty jars
 * (on the classpath of this project) will throw exceptions during server startup.
 *
 * @see Tomcat
 * @see CachePolicyFilterTest#testIntegration()
 * @see <a href="http://blog.sortedset.com/embedded-tomcat-jersey/">Example given in this blog post</a>
 * @author Alex
 * @since 5/18/2018
 */
public class EmbeddedTomcatServer {

  private Tomcat tomcat;

  // config settings:
  private final int portNumber;
  private final String baseDir;

  public EmbeddedTomcatServer(int portNumber) {
    this.portNumber = portNumber;
    tomcat = new Tomcat();
    tomcat.setPort(portNumber);
    baseDir = initBaseDir();
    tomcat.setBaseDir(baseDir);
  }

  /**
   * Computes the argument to use for {@link Tomcat#setBaseDir(String)}.  This is the directory which the embedded
   * Tomcat will use for its temp files ({@code [user.dir]/tomcat.$PORT} by default).
   * <p>
   * Since we don't want to pollute our {@code user.dir}, we create a temp dir (in the system temp dir) for this purpose.
   * <p>
   * Subclasses may override this method to provide a different directory, or
   * <i>return {@code null} to use {@link Tomcat}'s default ({@code [user.dir]/tomcat.$PORT} will be created)</i>.
   *
   * @return the argument to use for {@link Tomcat#setBaseDir(String)}
   *
   * @see Tomcat#setBaseDir(String)
   * @see Tomcat#initBaseDir()
   */
  protected String initBaseDir() {
    try {
      Path tempDir = Files.createTempDirectory(String.format("%s_%s_", getClass().getSimpleName(), Globals.CATALINA_HOME_PROP));
      FileUtils.deleteOnExit(tempDir);
      return tempDir.toAbsolutePath().toString();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public int getPortNumber() {
    return portNumber;
  }

  public Tomcat getTomcat() {
    return tomcat;
  }

  /**
   * This is equivalent to adding a web application to Tomcat's webapps
   * directory. The equivalent of the default web.xml will be applied  to the
   * web application and any WEB-INF/web.xml and META-INF/context.xml packaged
   * with the application will be processed normally. Normal web fragment and
   * {@link javax.servlet.ServletContainerInitializer} processing will be
   * applied.
   *
   * <p style="font-style: italic;">
   *   NOTE: Instead of creating a static exploded war dir for each unit test, it might be possible to generate it
   *   dynamically in a temp dir. See {@link WebXml#toXml()} and {@link WebXmlParser}.
   * </p>
   * @param contextPath The context mapping to use, "" for root context.
   * @param docBase Base directory for the context, for static files.
   *  Must exist, relative to the server home
   * @return the deployed context (can be used to obtain the {@link ServletContext} for the app)
   * @throws ServletException if a deployment error occurs
   * @see Tomcat#addWebapp(String, String)
   */
  public Context addWebapp(String contextPath, String docBase) throws ServletException {
    return tomcat.addWebapp(contextPath, docBase);
  }

  public void start() throws LifecycleException {
    System.out.printf("Starting embedded Tomcat on port %d (using baseDir = %s)%n", portNumber, baseDir);
    tomcat.start();
  }

  public void stop() throws LifecycleException {
    tomcat.stop();
  }

  public Server getServer() {
    return tomcat.getServer();
  }
}
