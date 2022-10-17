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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedListMultimap;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

/**
 * Can be used to record method invocations on a particular object instance.
 *
 * @see java.lang.reflect.Proxy
 * @see java.lang.reflect.InvocationHandler
 *
 * @author Alex
 * @since 9/28/2022
 */
public class InvocationRecorder implements Iterable<InvocationRecord> {

  /**
   * The instance for which method calls are being recorded.
   */
  private final Object target;

  private final LinkedListMultimap<String, InvocationRecord> invocationRecords = LinkedListMultimap.create();


  /**
   * @param target The instance for which the method calls are being recorded
   */
  public InvocationRecorder(@Nullable Object target) {
    this.target = target;
  }

  public Object getTarget() {
    return target;
  }

  /**
   * Records a new method call on the {@link #target} instance,
   * timestamped with the current value of {@link System#currentTimeMillis()}.
   *
   * @param methodName The name of the invoked method
   * @param args The args passed to the method
   */
  public void recordInvocation(@Nonnull String methodName, Object... args) {
    recordInvocation(System.currentTimeMillis(), methodName, args);
  }

  /**
   * Records a new method call on the {@link #target} instance.
   *
   * @param timestamp The time (epoch millis) when the invocation occurred
   * @param methodName The name of the invoked method
   * @param args The args passed to the method
   */
  public void recordInvocation(long timestamp, @Nonnull String methodName, Object... args) {
    Preconditions.checkNotNull(methodName, "methodName");
    // TODO: can extract this to a util class (e.g. "InvocationRecorder")
    invocationRecords.put(methodName, new InvocationRecord(timestamp, target, methodName, args));
  }

  /**
   * @return an immutable chronologically-ordered list of invocation records for the given method
   */
  public List<InvocationRecord> getInvocationRecords(@Nonnull String methodName) {
    return ImmutableList.copyOf(invocationRecords.get(methodName));
  }

  /**
   * @return an immutable chronologically-ordered list of all recorded method invocations
   */
  public List<InvocationRecord> getInvocationRecords() {
    return ImmutableList.copyOf(invocationRecords.values());
  }

  /**
   * @return the number of recorded invocations for the given method
   */
  public int count(@Nonnull String methodName) {
    return invocationRecords.get(methodName).size();
  }

  /**
   * @return the total number of all the recorded method invocations
   */
  public int count() {
    return invocationRecords.size();
  }

  /**
   * Removes all invocation records.
   */
  public void clear() {
    invocationRecords.clear();
  }

  @NotNull
  @Override
  public Iterator<InvocationRecord> iterator() {
    return Iterators.unmodifiableIterator(invocationRecords.values().iterator());
  }
}
