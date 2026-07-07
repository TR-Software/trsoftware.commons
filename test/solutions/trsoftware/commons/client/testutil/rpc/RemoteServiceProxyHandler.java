package solutions.trsoftware.commons.client.testutil.rpc;

import com.google.common.base.MoreObjects;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwtmockito.GwtMockito;
import solutions.trsoftware.commons.shared.util.ArrayUtils;
import solutions.trsoftware.commons.shared.util.ListUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static solutions.trsoftware.commons.client.testutil.rpc.RpcStatus.FAILURE;
import static solutions.trsoftware.commons.client.testutil.rpc.RpcStatus.SUCCESS;

/**
 * Helper for creating a {@linkplain Proxy dynamic proxy} for the clientside {@code Async} counterpart
 * of a {@link RemoteService} GWT-RPC interface,
 * which can be used with {@link GwtMockito} to mock the GWT-RPC mechanism for unit testing clientside code without
 * using {@code GWTTestCase} or servlets.
 * <p>
 * For example, given a hypothetical {@link RemoteService} named {@code MessageService}:
 * <pre>
 *   // test setUp:
 *   MessageServiceImpl messageServiceImpl = new MessageServiceImpl();  // implementation of MessageService
 *   {@link RemoteServiceProxyHandler} invocationHandler = new {@link RemoteServiceProxyHandler#RemoteServiceProxyHandler}(messageServiceImpl);
 *   {@link GwtMockito#useProviderForType}(MessageService.class, type -> {@link Proxy#newProxyInstance}(
 *         type.getClassLoader(), new Class[]{MessageServiceAsync.class, {@link ServiceDefTarget}.class}, invocationHandler));
 * </pre>
 * The above setup causes <code>{@link GWT#create}(MessageService.class)</code> to return a {@link Proxy} instance
 * that implements {@code MessageServiceAsync} and {@link ServiceDefTarget}, using an instance of this class as the
 * {@link InvocationHandler}.
 * The proxy will forward all {@code MessageServiceAsync} methods directly to the provided {@code MessageServiceImpl} instance
 * and invokes the {@link AsyncCallback} (passed as the last argument to the async method)
 * without any actual RPC or serialization.
 * <p>
 * By default, the actual execution of the service method is deferred to allow the test code to preview it and
 * possibly override the result (see {@link #getPendingRpcInvocations()}), but can be configured to execute
 * the service methods automatically by passing <code>{@link #autoExec} = true</code> to the
 * {@linkplain #RemoteServiceProxyHandler(RemoteService, boolean) constructor}.
 *
 * @author Alex
 * @since 4/10/2026
 * @see com.google.gwt.user.client.rpc.impl.RemoteServiceProxy
 * @see com.google.gwt.user.rebind.rpc.ProxyCreator
 */
@GwtIncompatible // Reflection
@SuppressWarnings("NonJREEmulationClassesInClientCode")
public class RemoteServiceProxyHandler implements InvocationHandler {

  private final RemoteService service;
  /**
   * Configures whether this instance will automatically execute the service methods and invoke callbacks;
   * ({@code false} to defer execution to the testing code)
   */
  private boolean autoExec;  // TODO(5/18/2026): experimental

  private final List<RpcInvocation> rpcInvocations = new ArrayList<>();

  public RemoteServiceProxyHandler(RemoteService service) {
    this.service = requireNonNull(service, "service");
  }

  /**
   * @param autoExec {@code true} to automatically execute the service methods and invoke callbacks; or
   *   {@code false} to defer execution to the testing code
   */
  public RemoteServiceProxyHandler(RemoteService service, boolean autoExec) {
    this.service = service;
    this.autoExec = autoExec;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    List<Class<?>> methodParamTypes = Arrays.asList(method.getParameterTypes());
    Class<?> callbackType = ListUtils.last(methodParamTypes);
    assert AsyncCallback.class.isAssignableFrom(callbackType);

    // get the service method corresponding to this async method
    Method serviceMethod = service.getClass().getMethod(method.getName(), methodParamTypes.subList(0, methodParamTypes.size() - 1).toArray(new Class[0]));
    Object[] serviceMethodArgs = Arrays.copyOfRange(args, 0, args.length - 1);
    AsyncCallback callback = (AsyncCallback)ArrayUtils.getLast(args);
    // the serviceMethod execution is deferred: to be performed at the discretion of the test code,
    // via RpcInvocation.execute (followed by RpcInvocation.complete to invoke the callback)
    RpcInvocation rpc = new RpcInvocation(method, service, serviceMethod, serviceMethodArgs, callback);
    rpcInvocations.add(rpc);
    if (autoExec)  // TODO(5/18/2026): doc this
      rpc.execute(true);
    return null;  // RemoteServiceAsync methods are void
  }

  public Stream<RpcInvocation> streamAllRpcInvocations() {
    return rpcInvocations.stream();
  }

  public Stream<RpcInvocation> streamAllRpcInvocations(boolean completed) {
    return streamAllRpcInvocations().filter(rpcInvocation -> rpcInvocation.isCompleted() == completed);
  }

  /**
   * @return all RPC invocations that have been recorded by this instance
   */
  public List<RpcInvocation> getRpcInvocations() {
    return rpcInvocations;
  }

  /**
   * @return all RPC invocation that have not been {@linkplain RpcInvocation#complete() completed} yet
   */
  public List<RpcInvocation> getPendingRpcInvocations() {
    return streamAllRpcInvocations(false).collect(Collectors.toList());
  }

  /**
   * Captures an invocation of a service method so that it can be previewed and executed asynchronously
   * by the testing code.
   * Normal execution can also be bypassed by setting the outcome with {@link #mockSuccess(Object)}
   * or {@link #mockFailure(Throwable)}.
   *
   * @see #execute(boolean)
   * @see #complete()
   */
  public static class RpcInvocation {
    private Method proxyMethod;
    private RemoteService service;
    private Method serviceMethod;
    private Object[] serviceMethodArgs;
    private AsyncCallback callback;

    private RpcStatus status;
    private Object outcome;

    /**
     * {@code true} if callback has been invoked with the result or exception from {@link #execute()}
     *
     * @see #complete()
     */
    private boolean completed;

    public RpcInvocation(Method proxyMethod, RemoteService service, Method serviceMethod, Object[] serviceMethodArgs, AsyncCallback callback) {
      this.proxyMethod = proxyMethod;
      this.service = service;
      this.serviceMethod = serviceMethod;
      this.serviceMethodArgs = serviceMethodArgs;
      this.callback = callback;
    }

    /**
     * Executes the service method and saves the outcome.  This should be followed by a call to {@link #complete()}
     * to invoke the callback.
     */
    public void execute() {
      checkNotExecuted();
      try {
        outcome = serviceMethod.invoke(service, serviceMethodArgs);
        status = SUCCESS;
      }
      catch (Throwable ex) {
        outcome = ex;
        status = FAILURE;
      }
      System.out.printf("Executed %s: status=%s, outcome=%s%n", toInvocationString(), status, outcome);
    }

    /**
     * Executes the service method and saves the outcome, then optionally invokes the {@link #callback}
     * (if {@code invokeCallback = true}), thereby {@linkplain #complete() completing} the RPC request.
     *
     * @param invokeCallback {@code true} complete the request by invoking the {@link #callback} with the outcome
     *   of the service method;  otherwise the callback can be invoked later via a separate call to {@link #complete()}
     */
    public void execute(boolean invokeCallback) {
      execute();
      if (invokeCallback)
        complete();
      /* TODO(5/14/2026): is there any reason to not invoke the callback right away?
          - the complete method is currently only used with mockSuccess/mockFailure to bypass execute
       */
    }

    private void checkNotExecuted() {
      checkState(status == null, "Status already set to %s with outcome %s", status, outcome);
    }

    /**
     * Bypasses {@linkplain #execute() execution} of the service method and sets the outcome
     * to {@link RpcStatus#SUCCESS} with the specified result.  This should be followed up
     * by calling {@link #complete()} to invoke the {@link #callback}.
     *
     * @param result value to pass to {@link AsyncCallback#onSuccess(Object)} on {@link #complete()}.
     * @return self, for chaining a call to {@link #complete()}
     * @throws IllegalStateException if the callback has already been invoked
     */
    public RpcInvocation mockSuccess(Object result) {
      checkNotCompleted();
      status = SUCCESS;
      outcome = result;
      return this;
    }
    
    /**
     * Bypasses {@linkplain #execute() execution} of the service method and sets the outcome
     * to {@link RpcStatus#FAILURE} with the specified exception.  This should be followed up
     * by calling {@link #complete()} to invoke the {@link #callback}.
     *
     * @param result value to pass to {@link AsyncCallback#onFailure(Throwable)} on {@link #complete()}.
     * @return self, for chaining a call to {@link #complete()}
     * @throws IllegalStateException if the callback has already been invoked
     */
    public RpcInvocation mockFailure(Throwable exception) {
      checkNotCompleted();
      status = FAILURE;
      outcome = exception;
      return this;
    }
    
    /**
     * Invokes the {@link #callback} with the outcome of {@link #execute()}, thereby completing the RPC request.
     */
    @SuppressWarnings("unchecked")
    public void complete() {
      if (status == null)
        throw new IllegalStateException("Service method hasn't been executed yet: " + toInvocationString());
      checkNotCompleted();
      try {
        if (status == SUCCESS)
          callback.onSuccess(outcome);
        else
          callback.onFailure((Throwable)outcome);
      }
      finally {
        completed = true;
      }
    }

    private void checkNotCompleted() {
      if (completed)
        throw new IllegalStateException(format("callback.%s has already been invoked with outcome=%s",
            status == SUCCESS ? "onSuccess" : "onFailure", outcome));
    }

    public boolean isCompleted() {
      return completed;
    }

    public RpcStatus getStatus() {
      return status;
    }

    public Object getOutcome() {
      return outcome;
    }

    public Method getProxyMethod() {
      return proxyMethod;
    }

    public RemoteService getService() {
      return service;
    }

    public Method getServiceMethod() {
      return serviceMethod;
    }

    public Object[] getServiceMethodArgs() {
      return serviceMethodArgs;
    }

    public AsyncCallback getCallback() {
      return callback;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("methodCall", toInvocationString())
          .add("status", status)
          .add("outcome", outcome)
          .add("completed", completed)
          .toString();
    }

    public String toInvocationString() {
      return StringUtils.methodCallToString(
          serviceMethod.getDeclaringClass(), serviceMethod.getName(), serviceMethodArgs);
    }

  }
}
