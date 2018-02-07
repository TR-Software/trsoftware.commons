package solutions.trsoftware.commons.server.servlet;

import com.google.gwt.rpc.server.RpcServlet;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import solutions.trsoftware.commons.server.gwt.GwtPermutationsIndex;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import static com.google.gwt.user.client.rpc.RpcRequestBuilder.MODULE_BASE_HEADER;

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
  private GwtPermutationsIndex permutationsIndex;

  protected RPCRequest getThreadLocalRPCRequest() {
    return threadLocalRPCRequest.get();
  }

  @Override
  protected void onAfterRequestDeserialized(RPCRequest rpcRequest) {
    super.onAfterRequestDeserialized(rpcRequest);
    threadLocalRPCRequest.set(rpcRequest);
  }

  /**
   * Delegates to {@link RemoteServiceServlet#doUnexpectedFailure(Throwable) super}
   * and logs the exception using {@link #logException(Throwable)}.
   *
   * TODO: this override is probably redundant, because the super calls {@link RPCServletUtils#writeResponseForUnexpectedFailure(ServletContext, HttpServletResponse, Throwable)}, which logs the exception the same way
   */
  @Override
  protected void doUnexpectedFailure(Throwable e) {
    super.doUnexpectedFailure(e);
    // log the exception (GWT won't do it unless we override this method)
    logException(e);
  }

  /**
   * Logs the exception using {@link javax.servlet.ServletContext#log(String, Throwable)}.
   * This method is called from {@link #doUnexpectedFailure(Throwable)}.
   * Subclasses may override to provide a different method of logging exception (or to suppress logging entirely).
   */
  protected void logException(Throwable e) {
    getServletContext().log("Exception while processing RPC request: " + threadLocalRPCRequest.get().toString(), e);
  }

  /**
   * Extract the module's base path from the current request.
   *
   * This method was borrowed from {@link RpcServlet#getRequestModuleBasePath()}
   *
   * @return the module's base path, modulo protocol and host, as reported by
   *         {@link com.google.gwt.core.client.GWT#getModuleBaseURL()} or
   *         <code>null</code> if the request did not contain the
   *         {@value com.google.gwt.user.client.rpc.RpcRequestBuilder#MODULE_BASE_HEADER} header
   */
  protected final String getRequestModuleBasePath() {
    try {
      String header = getThreadLocalRequest().getHeader(MODULE_BASE_HEADER);
      if (header == null) {
        return null;
      }
      String path = new URL(header).getPath();
      String contextPath = getThreadLocalRequest().getContextPath();
      if (!path.startsWith(contextPath)) {
        return null;
      }
      return path.substring(contextPath.length());
    } catch (MalformedURLException e) {
      return null;
    }
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

}
