package solutions.trsoftware.commons.client.controller;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Provides a default implementation of {@link AsyncCallback#onFailure(Throwable)}, to enable defining a callback
 * as a lambda expression for {@link #onSuccess(Object)}.
 * <p>
 * The default implementation of {@link #onFailure(Throwable)} simply invokes {@link GWT#reportUncaughtException(Throwable)}.
 *
 * @author Alex
 * @since 5/5/2026
 */
public interface SimpleCallback<T> extends AsyncCallback<T> {

  @Override
  default void onFailure(Throwable caught) {
    GWT.reportUncaughtException(caught);
  }
}
