package solutions.trsoftware.commons.server.servlet;

import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

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

  protected ThreadLocal<RPCRequest> threadLocalRPCRequest = new ThreadLocal<>();

  @Override
  protected void onAfterRequestDeserialized(RPCRequest rpcRequest) {
    super.onAfterRequestDeserialized(rpcRequest);
    threadLocalRPCRequest.set(rpcRequest);
  }

  /**
   * Delegates to {@link RemoteServiceServlet#doUnexpectedFailure(Throwable) super}
   * and logs the exception using {@link #logException(Throwable)}.
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

}
