package solutions.trsoftware.commons.client.testutil.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Outcome of an RPC request.
 *
 * @author Alex
 * @since 4/10/2026
 */
public enum RpcStatus {
  /**
   * RPC completed successfully
   * @see AsyncCallback#onSuccess(Object)
   */
  SUCCESS,
  /**
   * RPC threw an exception
   * @see AsyncCallback#onFailure(Throwable)
   */
  FAILURE;
}
