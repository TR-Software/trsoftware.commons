package solutions.trsoftware.commons.client.testutil.rpc;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.gwt.user.client.rpc.AsyncCallback;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkState;

/**
 * Implementation of {@link AsyncCallback} that records the
 * argument passed to {@link #onSuccess(Object)} or {@link #onFailure(Throwable)}.
 */
public class RecordingCallback<T> implements AsyncCallback<T> {
  private T result;
  private Throwable exception;
  @Nullable
  private RpcStatus status;

  @Override
  public void onFailure(Throwable caught) {
    checkState(status == null, "Status already set to %s with outcome %s", status, status == RpcStatus.SUCCESS ? result : exception);
    status = RpcStatus.FAILURE;
    this.exception = caught;
  }

  @Override
  public void onSuccess(T result) {
    checkState(status == null, "Status already set to %s with outcome %s", status, status == RpcStatus.SUCCESS ? result : exception);
    status = RpcStatus.SUCCESS;
    this.result = result;
  }

  public T getResult() {
    checkState(status == RpcStatus.SUCCESS, "status = %s", status);
    return result;
  }

  public Throwable getException() {
    checkState(status == RpcStatus.FAILURE, "status = %s", status);
    return exception;
  }

  @Nullable
  public RpcStatus getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return toStringHelper(MoreObjects.toStringHelper(this)).toString();
  }

  /**
   * Appends this object's fields to the given helper.
   * @return the same helper instance passed as the argument
   */
  public ToStringHelper toStringHelper(ToStringHelper helper) {
    helper.add("status", status);
    if (status == RpcStatus.SUCCESS) {
      helper.add("result", result);
    }
    else if (status == RpcStatus.FAILURE) {
      helper.add("exception", exception);
    }
    return helper;
  }
}
