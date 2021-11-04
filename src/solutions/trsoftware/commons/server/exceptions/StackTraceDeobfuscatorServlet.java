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

package solutions.trsoftware.commons.server.exceptions;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import solutions.trsoftware.commons.client.exceptions.StackTraceDeobfuscatorService;
import solutions.trsoftware.commons.server.servlet.ServletUtils;
import solutions.trsoftware.commons.server.servlet.config.WebConfigParser;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.HashMap;

/**
 * @author Alex
 * @since Jul 22, 2013
 */
public class StackTraceDeobfuscatorServlet extends RemoteServiceServlet implements StackTraceDeobfuscatorService {

  /**
   * Stores instances of {@link StackTraceDeobfuscator}, keyed by symbolMaps path.
   */
  private HashMap<String, StackTraceDeobfuscator> deobfuscators = new HashMap<>();

  public StackTraceDeobfuscator getDeobfuscator(String moduleName) {
    String symbolMapsPath = getSymbolMapsPath(moduleName);
    StackTraceDeobfuscator deobfuscator = deobfuscators.get(symbolMapsPath);
    // lazy init TODO: can we replace this with DefaultMap?
    if (deobfuscator == null) {
      synchronized (this) {
        // double-checked locking
        deobfuscator = deobfuscators.get(symbolMapsPath);
        if (deobfuscator == null) {
          String fullSymbolMapsFilePath = getServletContext().getRealPath(symbolMapsPath);
          // make sure the symbol maps directory exists (print a warning if not; don't throw an exception because this might be running in a unit test where no symbol maps are available)
          File symbolMapsDir = new File(fullSymbolMapsFilePath);
          if (!symbolMapsDir.exists() || !symbolMapsDir.isDirectory()) {
            String errorMsg = "WARNING: can't find the symbol maps needed by StackTraceDeobfuscator: " + fullSymbolMapsFilePath + " not found or not a directory";
            System.err.println(errorMsg);
            getServletContext().log(errorMsg, new IllegalArgumentException(errorMsg));
          }
          deobfuscator = StackTraceDeobfuscator.fromFileSystem(fullSymbolMapsFilePath);
          deobfuscator.setLazyLoad(true);  // only loads the symbols as needed (saves lots of memory at the expense of more filesystem reads)
          deobfuscators.put(symbolMapsPath, deobfuscator);
        }
      }
    }
    return deobfuscator;
  }

  /**
   * {@inheritDoc}
   *
   * De-obfuscates the given stack trace.
   *
   * Subclasses may override to take additional actions with the de-obfuscated stack trace (e.g. to log it).
   * If overriding, don't forget to call {@code super.}{@link #deobfuscateStackTrace(StackTraceElement[], String, String)}
   *
   * @see StackTraceDeobfuscatorService#deobfuscateStackTrace(StackTraceElement[], String, String)
   */
  @Override
  public String deobfuscateStackTrace(StackTraceElement[] obfStackTrace, String exceptionMessage, String moduleName) {
    StringBuilder str = new StringBuilder(1024);
    StackTraceElement[] stackTrace = getDeobfuscator(moduleName).resymbolize(obfStackTrace, getPermutationStrongName());
    /*
      TODO(11/3/2021):
        allow customizing the format of the stringified trace (for logging purposes); for example
        to match the Throwable.printStackTrace format instead of just joining lines with "\n" chars;
        (see Throwable.printStackTrace(PrintStreamOrWriter), on line java/lang/Throwable.java:658)

        Here's how traces are formatted by Throwable.printStackTrace:
          java.lang.RuntimeException: Dummy Exception
            at solutions.trsoftware.commons.server.exceptions.StackTraceDeobfuscatorServlet.deobfuscateStackTrace(StackTraceDeobfuscatorServlet.java:79)
            at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
            ...

        Also:
          - figure out how to display multiline exception messages
          - what about causes (Throwable.getCause())?
     */
    for (StackTraceElement ste : stackTrace) {
      str.append(ste).append("\n");
    }
    String result = str.toString();
    logStackTrace(exceptionMessage, result);
    return result;
  }

  /**
   * Called by {@link #deobfuscateStackTrace(StackTraceElement[], String, String)} to log the de-obfuscated stack trace
   * to the servlet context log.
   * <p>
   * Subclasses may override to provide a different logging mechanism (or to suppress logging).
   *
   * @see ServletContext#log(String)
   */
  protected void logStackTrace(String exceptionMessage, String deobfuscatedStackTrace) {
    // record the stack trace before returning it
    StringBuilder logMsgBuilder = ServletUtils.appendGwtRequestInfo(new StringBuilder("Client-side stack trace for "),
        getThreadLocalRequest(), getPermutationStrongName());
    logMsgBuilder.append('\n').append(exceptionMessage).append(":\n").append(deobfuscatedStackTrace);
    getServletContext().log(logMsgBuilder.toString());
  }

  /**
   * Subclasses may override to provide the path where the compiler-emitted deobfuscation resources
   * (e.g. symbol maps, obfuscated filenames, source maps) are located in the webapp.
   *
   * If not overridden, this method returns {@code "WEB-INF/deploy/" + moduleName + "/symbolMaps"}
   *
   * <p style="color: #6495ed; font-weight: bold;">
   *   TODO: allow specifying the path using {@code init-param} values defined for this servlet in web.xml
   *   see {@link WebConfigParser}
   * </p>
   *
   * @param moduleName The name of the GWT module that's making this RPC call, to be used in figuring out
   * where the symbol maps are located on the server. This value can be obtained client-side by calling {@code GWT.getModuleName()}
   *
   * @return the path where the deobfuscation resources emitted by the compiler are deployed for the given module
   * (e.g. symbol maps, obfuscated filenames, source maps) relative to the webapp root.
   * Example: {@code "WEB-INF/deploy/MyModule/symbolMaps"}
   */
  protected static String getSymbolMapsPath(String moduleName) {
    return "WEB-INF/deploy/" + moduleName + "/symbolMaps";
  }
}
