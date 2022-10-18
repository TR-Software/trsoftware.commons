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

package solutions.trsoftware.commons.shared.testutil;

import com.google.common.base.MoreObjects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Can be used to record method invocations for testing.
 *
 * @see java.lang.reflect.Proxy
 * @see java.lang.reflect.InvocationHandler
 *
 * @author Alex
 * @since 9/28/2022
 */
public class InvocationRecord {

  /**
   * The instance on which the method was invoked
   */
  private final Object target;
  /**
   * The name of the invoked method
   */
  private final String methodName;
  /**
   * The args passed to the method (possibly empty)
   */
  private final Object[] args;

  /**
   * Epoch time (in millis) when this instance was created
   */
  private final long timestamp;

  /**
   * @param target The instance on which the method was invoked
   * @param methodName The name of the invoked method
   * @param args The args passed to the method
   */
  public InvocationRecord(@Nullable Object target, @Nonnull String methodName, Object... args) {
    this(System.currentTimeMillis(), target, methodName, args);
  }

  /**
   * @param timestamp The time (epoch millis) when the invocation occurred
   * @param target The instance on which the method was invoked
   * @param methodName The name of the invoked method
   * @param args The args passed to the method
   */
  public InvocationRecord(long timestamp, @Nullable Object target, @Nonnull String methodName, Object... args) {
    this.target = target;
    this.methodName = Objects.requireNonNull(methodName, "methodName");
    this.args = args;
    this.timestamp = timestamp;
  }

  public Object getTarget() {
    return target;
  }

  @Nonnull
  public String getMethodName() {
    return methodName;
  }

  public Object[] getArgs() {
    return args;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("target", target)
        .add("methodName", methodName)
        .add("args", args)
        .add("timestamp", timestamp)
        .toString();
  }
}
