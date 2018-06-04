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

package solutions.trsoftware.commons.server.servlet;

import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import solutions.trsoftware.commons.server.gwt.GwtPermutationsIndex;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

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
