/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.server.testutil;

import solutions.trsoftware.commons.shared.testutil.InvocationRecorder;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * An invocation handler that can be used in conjunction with {@link Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)}
 * to record all method invocations on a target instance.
 * <h3>Example:</h3>
 * <pre>{@code
 * MockScheduledExecutorService executor = new MockScheduledExecutorService();
 * RecordingInvocationHandler<MockScheduledExecutorService> invocationRecorder = new RecordingInvocationHandler<>(executor, true);
 * ScheduledExecutorService executorProxy = (ScheduledExecutorService)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ScheduledExecutorService.class}, invocationRecorder);
 * }</pre>
 *
 * @see InvocationRecorder
 * @see java.lang.reflect.Proxy
 * @author Alex
 * @since 10/17/2022
 */
public class RecordingInvocationHandler<T> implements InvocationHandler {
  private final T target;
  private final InvocationRecorder recorder;
  private final boolean verbose;

  public RecordingInvocationHandler(T target) {
    this(target, false);
  }

  public RecordingInvocationHandler(T target, boolean verbose) {
    this.target = target;
    recorder = new InvocationRecorder(target);
    this.verbose = verbose;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object result = null;
    try {
      result = method.invoke(target, args);
      if (verbose)
        System.out.printf("Invoked %s on %s%n", StringUtils.methodCallToString(method.getName(), args), target);
      return result;
    }
    finally {
      recorder.recordInvocation(method.getName(), args);
    }
  }

  public T getTarget() {
    return target;
  }

  public InvocationRecorder getRecorder() {
    return recorder;
  }

  public boolean isVerbose() {
    return verbose;
  }
}
