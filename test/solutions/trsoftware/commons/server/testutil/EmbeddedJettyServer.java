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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import solutions.trsoftware.commons.server.net.NetUtils;

import javax.servlet.http.HttpServlet;

/**
 * A wrapper around an embedded Jetty instance to facilitate testing servlets.
 * <p>
 * Can be either instantiated on a specific port and operated via the {@link #start()}/{@link #stop()} methods,
 * or can be used in a "resource management block" by calling static methods
 * {@link #with(Class, String, RunnableWithWebServer)} and {@link #with(Class, int, String, RunnableWithWebServer)}
 * <p>
 * Servlet mappings can be added to the container with {@link #addServlet(Class, String)}
 *
 * @see <a href="http://www.eclipse.org/jetty/documentation/9.3.x/embedding-jetty.html">Embedding Jetty (docs)</a>
 *
 * @author Alex
 */
public class EmbeddedJettyServer {
  /*
   NOTE: Caucho Resin was used in the past but it was slower, more cumbersome to set up, and produced
   strange mbean context errors.
   */

  private static final int DEFAULT_PORT_NUMBER = 40000;
  private final ServletHandler servletHandler;

  private Server server;
  private final int portNumber;
  //  private final ServletContextHandler rootContext;

  public EmbeddedJettyServer(int portNumber) {
    this.portNumber = portNumber;
    server = new Server(portNumber);
//    rootContext = new ServletContextHandler();
    servletHandler = new ServletHandler();
    server.setHandler(servletHandler);
  }

  public ServletHolder addServlet(Class<? extends HttpServlet> servletClass, String uri) {
//    return rootContext.addServlet(servletClass, uri);
    return servletHandler.addServletWithMapping(servletClass, uri);
  }

  /** Starts the server */
  public void start() {
    System.out.println("Embedded Jetty instance starting on port " + portNumber);
//    System.out.println("--------------------------------------------------------------------------------");
    long startTime = System.currentTimeMillis();
    try {
      server.start();
//      server.dump(System.out);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
//    System.out.println("--------------------------------------------------------------------------------");
    System.out.println("Embedded Jetty instance running on port " + portNumber + "; startup took " + (System.currentTimeMillis() - startTime) + " ms.");
  }

  /** Tears down the server */
  public void stop() {
    if (server != null) {
      try {
        server.stop();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
      server.destroy();
    }
  }

  /**
   * Makes the current thread wait until the server is done executing.
   * @see Server#join()
   * @see Thread#join()
   */
  public void join() throws InterruptedException {
    server.join();
  }

  /**
   * Executes a block of code against a new servlet at the desired URL, then
   * tears down the servlet after the code has been executed.
   * 
   * @param servletClass The servlet to run in the container
   * @param uri  The URI path to this servlet (e.g. "/foo/bar"
   */
  public static void with(Class<? extends HttpServlet> servletClass, String uri, RunnableWithWebServer code) throws Exception {
    with(servletClass, NetUtils.findAvailableLocalPort(DEFAULT_PORT_NUMBER, NetUtils.MAX_VALID_PORT), uri, code);
  }

  /**
   * Executes a block of code against a new servlet at the desired URL, then
   * tears down the servlet after the code has been executed.
   *
   * @param servletClass The servlet to run in the container
   * @param uri  The URI path to this servlet (e.g. "/foo/bar"
   */
  public static void with(Class<? extends HttpServlet> servletClass, int portNumber, String uri, RunnableWithWebServer code) throws Exception {
    EmbeddedJettyServer instance = new EmbeddedJettyServer(portNumber);
    instance.addServlet(servletClass, uri);
    try {
      instance.start();
      // run the code
      code.run(portNumber);
    }
    finally {
      instance.stop();
    }
  }


  public interface RunnableWithWebServer {
    public void run(int port) throws Exception;
  }
}
