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

import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.WebXml;
import org.apache.tomcat.util.descriptor.web.WebXmlParser;
import solutions.trsoftware.commons.server.io.file.FileUtils;
import solutions.trsoftware.commons.server.net.NetUtils;
import solutions.trsoftware.commons.server.servlet.filters.CachePolicyFilterTest;
import solutions.trsoftware.commons.shared.util.ArrayUtils;
import solutions.trsoftware.commons.shared.util.CollectionUtils;

import javax.servlet.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Skeleton implementation of an embedded Tomcat server.
 *
 * <h3>Usage</h3>
 * Refer to the code in {@link CachePolicyFilterTest#testIntegration()} and the webapp defined in
 * {@code test/resources/CachePolicyFilterTest_webapp}.
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
public class EmbeddedTomcatServer implements AutoCloseable {

  private static final int MIN_PORT = 9000;
  private static final int MAX_PORT = 9999;

  private Tomcat tomcat;

  // config settings:
  private final int portNumber;

  private final Path baseDir;
  private final Path webappsDir;

  public EmbeddedTomcatServer(int portNumber) throws IOException {
    this.portNumber = portNumber;
    tomcat = new Tomcat();
    tomcat.setPort(portNumber);
    baseDir = createBaseDir().toAbsolutePath();
    tomcat.setBaseDir(baseDir.toString());
    webappsDir = baseDir.resolve("webapps");
  }

  /**
   * Uses the first available port between {@value MIN_PORT} and {@value MAX_PORT}
   * @see NetUtils#findAvailableLocalPort(int, int)
   * @see #EmbeddedTomcatServer(int)
   */
  public EmbeddedTomcatServer() throws IOException {
    this(NetUtils.findAvailableLocalPort(MIN_PORT, MAX_PORT));
  }

  /**
   * Computes the argument to use for {@link Tomcat#setBaseDir(String)}.  This is the directory which the embedded
   * Tomcat will use for its temp files ({@code [user.dir]/tomcat.$PORT} by default).
   * <p>
   * Since we don't want to pollute our {@code user.dir}, we create a temp dir (in the system temp dir) for this purpose.
   * <p>
   * Subclasses may override this method to provide a different directory
   *
   * @return the argument to use for {@link Tomcat#setBaseDir(String)}
   *
   * @see Tomcat#setBaseDir(String)
   * @see Tomcat#initBaseDir()
   */
  protected Path createBaseDir() throws IOException {
    return FileUtils.deleteOnExit(Files.createTempDirectory(getClass().getName()));
  }

  public int getPortNumber() {
    return portNumber;
  }

  public Path getBaseDir() {
    return baseDir;
  }

  public Path getWebappsDir() {
    return webappsDir;
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

  /**
   * Calls {@link #addContext(String, String)} with a {@code null} value for {@code docBase}
   *
   * @return the added context
   * @see #addContext(String, String)
   */
  public Context addContext(String contextPath) {
    return addContext(contextPath, null);
  }

  /**
   * @see Tomcat#addContext(String, String)
   */
  public Context addContext(String contextPath, String docBase) {
    return tomcat.addContext(contextPath, docBase);
  }

  /**
   * @return the {@link Context} registered at the given path, or {@code null} if no such context has been added
   *
   * @see Container#findChild(String)
   */
  public Context getContext(String contextPath) {
    return (Context)tomcat.getHost().findChild(contextPath);
  }

  /**
   * @return all the deployed {@linkplain Context contexts}
   *
   * @see Container#findChild(String)
   */
  public Context[] getContexts() {
    return getContextsAsStream().toArray(Context[]::new);
  }

  /**
   * @return a stream of all the deployed {@linkplain Context contexts}
   *
   * @see Container#findChild(String)
   */
  public Stream<Context> getContextsAsStream() {
    return Arrays.stream(tomcat.getHost().findChildren()).map(Context.class::cast);
  }

  /**
   * Finds the {@link Context} registered at the given path, optionally creating it if no such context has been added yet.
   * @param create whether to automatically create a new context if not found (by calling {@link #addContext(String)})
   * @return the {@link Context} registered at the given path, or {@code null} if no such context has been added
   * @see #addContext(String)
   */
  public Context getContext(String contextPath, boolean create) {
    Context context = getContext(contextPath);
    if (context == null && create) {
      context = addContext(contextPath);
    }
    return context;
  }

  /**
   * Adds the given servlet to the "root" context ({@code "/"}).
   * The servlet name will be the name of its class.
   *
   * @return an object that provides useful methods for using and managing the servlet
   *     (e.g. {@link ServletHandle#getUrlBuilder()})
   * @see #addContext(String, String)
   * @see #addContext(String)
   */
  public ServletHandle addServlet(Servlet servlet, String... urlPatterns) {
    return addServlet(servlet.getClass().getName(), servlet, urlPatterns);
  }

  /**
   * Adds the given servlet to the "root" context ({@code "/"}).
   * The servlet name will be the name of its class.
   *
   * @return an object that provides useful methods for using and managing the servlet
   *     (e.g. {@link ServletHandle#getUrlBuilder()})
   * @see #addContext(String, String)
   * @see #addContext(String)
   */
  public ServletHandle addServlet(String servletClass, String... urlPatterns) {
    return addServlet(servletClass, servletClass, urlPatterns);
  }

  /**
   * Adds the given servlet to the "root" context ({@code "/"}).
   *
   * @return an object that provides useful methods for using and managing the servlet
   *         (e.g. {@link ServletHandle#getUrlBuilder()})
   * @see #addContext(String, String)
   * @see #addContext(String)
   */
  public ServletHandle addServlet(String servletName, Servlet servlet, String... urlPatterns) {
    return addServlet(getContext("/", true), servletName, servlet, urlPatterns);
  }

  /**
   * Adds the given servlet to the "root" context ({@code "/"}).
   *
   * @return an object that provides useful methods for using and managing the servlet
   *         (e.g. {@link ServletHandle#getUrlBuilder()})
   * @see #addContext(String, String)
   * @see #addContext(String)
   */
  public ServletHandle addServlet(String servletName, String servletClass, String... urlPatterns) {
    return addServlet(getContext("/", true), servletName, servletClass, urlPatterns);
  }

  /**
   * Adds the given servlet to the given context.
   *
   * @param context a context that was previously created using {@link #addContext(String, String)} or {@link #addContext(String)}
   * @return an object that provides useful methods for using and managing the servlet
   *         (e.g. {@link ServletHandle#getUrlBuilder()})
   * @see #addContext(String, String)
   * @see #addContext(String)
   */
  public ServletHandle addServlet(Context context, String servletName, Servlet servlet, String... urlPatterns) {
    Wrapper wrapper = Tomcat.addServlet(context, servletName, servlet);
    return createServletHandle(context, wrapper, servletName, urlPatterns);
  }

  /**
   * Adds the given servlet to the given context.
   * 
   * @return an object that provides useful methods for using and managing the servlet
   *         (e.g. {@link ServletHandle#getUrlBuilder()})
   */
  public ServletHandle addServlet(Context context, String servletName, String servletClass, String... urlPatterns) {
    Wrapper wrapper = Tomcat.addServlet(context, servletName, servletClass);
    return createServletHandle(context, wrapper, servletName, urlPatterns);
  }

  private ServletHandle createServletHandle(Context context, Wrapper wrapper, String servletName, String[] urlPatterns) {
    ServletHandle servletHandle = new ServletHandle(context, wrapper, servletName);
    if (ArrayUtils.isEmpty(urlPatterns))
      System.err.printf("WARNING: no url-pattern mappings provided for servlet (%s)%n", servletName);
    else
      servletHandle.addMapping(urlPatterns);
    return servletHandle;
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

  /**
   * @return a new {@link StringBuilder} initialized to the base URL for this server.
   * For example: {@code http://localhost:1234}
   */
  public StringBuilder getUrlBuilder() {
    return new StringBuilder("http://localhost:").append(portNumber);
  }

  /**
   * @return a new {@link StringBuilder} initialized to the base URL for the given context on this server.
   * For example: {@code http://localhost:1234/myWebapp}
   */
  public StringBuilder getUrlBuilder(String contextPath) {
    return getUrlBuilder().append(contextPath);
  }

  /**
   * @return a new {@link StringBuilder} initialized to the base URL for the given context on this server.
   * For example: {@code http://localhost:1234/myWebapp}
   */
  public StringBuilder getUrlBuilder(Context context) {
    return getUrlBuilder(context.getPath());
  }

  @Override
  public void close() throws Exception {
    stop();
  }

  /**
   * Can be used to access a servlet added with {@link #addServlet}
   */
  public class ServletHandle {
    private Context context;
    private Wrapper wrapper;
    private String servletName;

    public ServletHandle(Context context, Wrapper wrapper, String servletName) {
      this.context = context;
      this.wrapper = wrapper;
      this.servletName = servletName;
    }

    public Context getContext() {
      return context;
    }

    public Wrapper getWrapper() {
      return wrapper;
    }

    public String getServletName() {
      return servletName;
    }

    public ServletContext getServletContext() {
      return context.getServletContext();
    }

    public ServletRegistration getServletRegistration() {
      return getServletContext().getServletRegistration(servletName);
    }

    public Set<String> addMapping(String... urlPatterns) {
      return getServletRegistration().addMapping(urlPatterns);
    }

    /**
     * @return the url-patterns mapped to this servlet
     */
    public Collection<String> getMappings() {
      return getServletRegistration().getMappings();
    }

    /**
     * @return a new {@link StringBuilder} initialized to a base URL that can be used to access this servlet.
     * For example: {@code http://localhost:1234/myWebapp/myServlet}
     * @throws IllegalStateException if no url-pattern mappings defined for servlet
     */
    public StringBuilder getUrlBuilder() {
      Collection<String> mappings = getMappings();
      if (mappings.isEmpty())
        throw new IllegalStateException(String.format("No url-pattern mappings defined for servlet '%s' (%s)",
            servletName, getWrapper().getServletClass()));
      String urlPattern = CollectionUtils.first(mappings);
      // TODO: what if it's a wildcard pattern (e.g. "/foo/*" or "*.html")?
      return EmbeddedTomcatServer.this.getUrlBuilder(context).append(urlPattern);
    }
  }

}
