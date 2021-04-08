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

package solutions.trsoftware.commons.server.servlet.gwt;

import com.google.gwt.user.server.rpc.AbstractRemoteServiceServlet;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import solutions.trsoftware.commons.server.gwt.GwtPermutationsIndex;
import solutions.trsoftware.commons.server.io.InputStreamTooLongException;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.server.util.reflect.ExceptionUtils;

import javax.annotation.Nullable;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static solutions.trsoftware.commons.server.servlet.gwt.RPCServletUtils.*;

/**
 * Provides some useful overrides from {@link RemoteServiceServlet}, such as logging for unexpected exceptions and
 * saving the {@link RPCRequest} in a {@link ThreadLocal}.
 *
 * @author Alex
 * @since 11/20/2017
 *
 * @see #doUnexpectedFailure(Throwable)
 */
public class BaseRpcServlet extends RemoteServiceServlet {

  private final ThreadLocal<RPCRequest> threadLocalRPCRequest = new ThreadLocal<>();
  private volatile GwtPermutationsIndex permutationsIndex;

  /**
   * Name of the servlet {@code init-param} specifying the value for {@link #maxContentLength}
   */
  public static final String INIT_PARAM_MAX_CONTENT_LENGTH = "maxContentLength";

  /**
   * RPC requests whose payload exceeds this number of bytes will trigger an exception in the
   * {@link #readContent(HttpServletRequest)} method.
   * <p>
   * This optional setting can be used to prevent malicious requests with huge payloads
   * from tying up server resources that would be used for reading and parsing the RPC request body.
   * <p>
   * A value <= 0 disables this check.
   */
  private long maxContentLength;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    String maxContentLengthVal = config.getInitParameter(INIT_PARAM_MAX_CONTENT_LENGTH);
    if (maxContentLengthVal != null) {
      try {
        this.maxContentLength = Long.parseLong(maxContentLengthVal);
      }
      catch (NumberFormatException e) {
        config.getServletContext().log("Unable to parse value of init-param " + INIT_PARAM_MAX_CONTENT_LENGTH);
        throw e;
      }
    }
  }

  /**
   * @return the deserialized RPC request or {@code null} if invoked before the request has been deserialized
   */
  @Nullable
  protected RPCRequest getThreadLocalRPCRequest() {
    return threadLocalRPCRequest.get();
  }

  @Override
  protected void onAfterRequestDeserialized(RPCRequest rpcRequest) {
    super.onAfterRequestDeserialized(rpcRequest);
    threadLocalRPCRequest.set(rpcRequest);
  }

  /**
   * We override this method for the following reasons:
   * <ol>
   *   <li>
   *     to catch any {@link InputStreamTooLongException} exception that might have been thrown
   *     by our {@link #readContent(HttpServletRequest)} implementation.
   *   </li>
   *   <li>
   *     to set a more appropriate status code in response to our {@link InputStreamTooLongException}, i.e.
   *     {@value HttpServletResponse#SC_REQUEST_ENTITY_TOO_LARGE} instead of the
   *     {@value HttpServletResponse#SC_INTERNAL_SERVER_ERROR} that would normally be sent by GWT in response to any
   *     exception that wasn't declared by a service method.
   *   </li>
   *   <li>
   *     to provide a hook point for additional logging (see {@link #logException(Throwable)}).
   *   </li>
   * </ol>
   * Our implementation first delegates to the superclass ({@link AbstractRemoteServiceServlet#doUnexpectedFailure(Throwable)}),
   * which resets the response, logs the exception (using {@link ServletContext#log(String, Throwable)}), sets the
   * status code to {@value HttpServletResponse#SC_INTERNAL_SERVER_ERROR}, and writes a {@code text/plain} response
   * containing the generic message <i>"The call failed on the server; see server log for details"</i>.
   * <p>
   * The only thing we do differently is change the status code to {@value HttpServletResponse#SC_REQUEST_ENTITY_TOO_LARGE}
   * if the exception was caused by an {@link InputStreamTooLongException}.
   * <p>
   * We also call our logging hook, {@link #logException(Throwable)}, which provides additional debugging info in the form
   * of the parsed RPC request, if available (see {@link #getThreadLocalRPCRequest()}).
   *
   * <b>NOTE:</b> unless you override {@link #logException(Throwable)}, this is likely to result in the same exception
   * appearing in the servlet context log twice: first logged by GWT with a generic message
   * (<i>"Exception while dispatching incoming RPC call"</i>), and then again by our {@link #logException(Throwable)} method,
   * which also logs the parsed {@link RPCRequest} (if available).
   *
   * @see com.google.gwt.user.server.rpc.RPCServletUtils#writeResponseForUnexpectedFailure
   */
  @Override
  protected void doUnexpectedFailure(Throwable e) {
    super.doUnexpectedFailure(e);
    /*
     Call our logging hook.
       - NOTE: to avoid the logging duplication with super.doUnexpectedFailure,
         could try wrapping the ServletContext (and overriding #getServletContext() to return this wrapper)
         with a class that checks whether a particular exception instance has already been logged.
         However, this would be ugly and probably not worth the trouble.
    */
    logException(e);
    /*
     Check if the failure was caused by our InputStreamTooLongException (our readContent method would have
     wrapped it in a ServletException, so we use ExceptionUtils.getFirstByType to unwrap it)
     If that's the case, just change the response status code from 500 (which gets set by super.doUnexpectedFailure)
     to 413 ("Payload Too Large" or "Request Entity Too Large")
    */
    InputStreamTooLongException inputStreamTooLongException =
        ExceptionUtils.getFirstByType(e, InputStreamTooLongException.class, 2);
    if (inputStreamTooLongException != null) {
      getThreadLocalResponse().setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
    }
  }

  /**
   * Logging hook called from {@link #doUnexpectedFailure(Throwable)}: logs additional debugging info in the form
   * of the parsed RPC request, if available (see {@link #getThreadLocalRPCRequest()}.
   * <p>
   * <strong>NOTE:</strong> unless overridden, this method is likely to result in the same exception appearing
   * in the servlet context log twice:
   * <ol>
   *   <li>logged by GWT with the generic message <i>"Exception while dispatching incoming RPC call"</i></li>
   *   <li>logged again by this method, which prints the parsed {@link RPCRequest} if available.</li>
   * </ol>
   * Subclasses may override this method to use a different logging mechanism
   * (instead of {@link ServletContext#log(String, Throwable)}), or to suppress the additional logging entirely.
   */
  protected void logException(Throwable e) {
    RPCRequest rpcRequest = getThreadLocalRPCRequest();
    if (rpcRequest != null) {
      log("Additional debugging for the failed RPC request (see above): " + rpcRequest.toString(), e);
    }
    // if the request has not been deserialized yet, we just let super.doUnexpectedFailure log a generic exception message
  }

  /**
   * We override this method simply to null out {@link #threadLocalRPCRequest} before the request goes out of scope.
   * <p>
   * NOTE: it would probably make more sense to override either {@link #doPost(HttpServletRequest, HttpServletResponse)}
   * or {@link #processPost(HttpServletRequest, HttpServletResponse)} instead, but unfortunately {@link RemoteServiceServlet}
   * declares them both {@code final}.
   */
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      super.service(req, resp);
    }
    finally {
      onRequestFinished();
    }
  }

  /**
   * This method is invoked from {@link #service(HttpServletRequest, HttpServletResponse)} to allow cleaning
   * up any thread-local values before the request goes out of scope in the container.
   * <p>
   * <strong>NOTE:</strong> if overriding this method, be sure to call {@code super}
   */
  protected void onRequestFinished() {
    threadLocalRPCRequest.remove();
  }

  /**
   * @return The set of all available permutation strong names derived from the *.cache.html files present
   * at the request's module path (given by {@link #getRequestModuleBasePath()}).
   */
  protected Set<String> getAvailablePermutations() {
    if (permutationsIndex == null) {
      // lazy init with double-checked locking
      synchronized (this) {
        if (permutationsIndex == null)
          permutationsIndex = new GwtPermutationsIndex();
      }
    }
    return permutationsIndex.getAvailablePermutations(getRequestModuleBasePath(), getServletContext());
  }

  /**
   * We override this method to throw an exception if the POST body size exceeds the limit specified by
   * {@link #getMaxContentLength()}.
   * <p>
   * If no limit is specified, we simply delegate to the superclass, otherwise, we read the input stream
   * using code similar to the superclass, but throw an exception once we've read more bytes than the limit.
   *
   * @throws ServletException if more than the limit bytes have been read from the input stream, or if there's a problem
   * with the request headers.
   *
   * @see ServerIOUtils#copyInputToOutput(java.io.InputStream, java.io.OutputStream, int, long)
   * @see <a href="https://github.com/csviri/bodylimiter">The "bodylimiter" project on GitHub for a more comprehensive solution</a>
   */
  @Override
  protected String readContent(HttpServletRequest request) throws ServletException, IOException {
    long maxContentLength = getMaxContentLength();
    if (maxContentLength <= 0) {
      // don't need to limit the POST body size; so just let the superclass take care of reading the content
      return super.readContent(request);
    } else {
      /* Need to limit the POST body size.
        NOTE: this code is basically the same as to com.google.gwt.user.server.rpc.RPCServletUtils.readContentAsGwtRpc(),
        except it uses solutions.trsoftware.commons.server.io.ServerIOUtils.copyInputToOutput(java.io.InputStream, java.io.OutputStream, int, long)
        to read the input stream into a string, such that it will throw an exception if the input stream is too long

        TODO: this code should be kept up-to-date with the logic in com.google.gwt.user.server.rpc.RPCServletUtils for any new GWT releases
              (we're maintaining a separate version in solutions.trsoftware.commons.server.servlet.gwt.RPCServletUtils )
              - perhaps add a unit test that checks the GWT version?
      */
      RPCServletUtils.checkContentTypeIgnoreCase(request, GWT_RPC_CONTENT_TYPE);
      String expectedCharSet = CHARSET_UTF8_NAME;
      RPCServletUtils.checkCharacterEncodingIgnoreCase(request, expectedCharSet);
      try (InputStream in = request.getInputStream()) {
        ByteArrayOutputStream out = new  ByteArrayOutputStream(BUFFER_SIZE);
        ServerIOUtils.copyInputToOutput(in, out, BUFFER_SIZE, maxContentLength);
        // NOTE: although we could use ByteArrayOutputStream.toString(charsetName) here, we use the following
        // construct because GWT does it that way (RPCServletUtils.getCharset allegedly avoids a concurrency bottleneck in the JVM)
        return new String(out.toByteArray(), RPCServletUtils.getCharset(expectedCharSet));
      }
      catch (InputStreamTooLongException e) {
        throw new ServletException(e);
      }
    }
  }

  protected long getMaxContentLength() {
    // NOTE: subclasses may override this method to provide a different mechanism for specifying this value (i.e. not from an init-param)
    return maxContentLength;
  }
}
