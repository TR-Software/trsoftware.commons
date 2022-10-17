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

package solutions.trsoftware.commons.shared.util;

import com.google.common.util.concurrent.SettableFuture;
import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.testutil.InvocationRecorder;
import solutions.trsoftware.commons.shared.util.CachedValue.ValueLoader;

import javax.annotation.Nonnull;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static solutions.trsoftware.commons.shared.util.ListUtils.last;

/**
 * @author Alex
 * @since 10/4/2022
 */
public class CachedValueTest extends TestCase {

  private final int maxAgeMillis = 100;
  private TestValueLoader loader;
  private CachedValue<Integer> cachedValue;
  private int expectedLoadCount;
  private int expectedReloadCount;
  private int expectedFailCount;
  private int expectedTimeoutCount;
  private Integer expectedValue;
  private long currentTimeMillis;
  private long lastUpdateTime;

  public void setUp() throws Exception {
    super.setUp();
    currentTimeMillis = System.currentTimeMillis();
    loader = new TestValueLoader(null);
    cachedValue = new CachedValue<Integer>(maxAgeMillis, loader) {
      @Override
      protected long currentTimeMillis() {
        return currentTimeMillis;
      }
    };
  }

  public void tearDown() throws Exception {
    loader = null;
    cachedValue = null;
    super.tearDown();
  }

  private long elapsedMillis(long since) {
    return currentTimeMillis - since;
  }
    
  public void testGet() throws Exception {
    assertFalse(cachedValue.hasValue());
    assertFalse(cachedValue.isExpired());  // an empty value shouldn't be considered expired

    // the first read should load the initial value and keep returning it until it expires
    expectedValue = 1;
    expectedLoadCount = 1;
    expectedReloadCount = 0;
    lastUpdateTime = currentTimeMillis;
    for (int i = 0; i <= maxAgeMillis; i++) {
      getAndVerify();
      assertTrue(cachedValue.hasValue());
      assertFalse(cachedValue.isExpired());
      currentTimeMillis++;
    }

    // after it expires, ValueLoader.reload should be called once to return a Future
    // and the CachedValue should keep returning the old value until the future is done
    {
      SettableFuture<Integer> reloadFuture = expireAndVerifyReload();
      // when that future is done, should start returning the new value until it expires again
      reloadFuture.set(expectedValue = 2);  // sets the Future's status to "done"
      lastUpdateTime = currentTimeMillis;
      getAndVerify();
      assertTrue(cachedValue.hasValue());
      assertFalse(cachedValue.isExpired());
      currentTimeMillis = lastUpdateTime + maxAgeMillis;  // not expired yet
      getAndVerify();
      assertFalse(cachedValue.isExpired());
    }

    {
      // expire it again but this time use a mock Future that throws an exception
      simulateReloadFailure(
          future -> future.cancel(true),
          throwable -> assertTrue(throwable instanceof CancellationException),
          ValueLoader.FailedReloadAction.ignore(), 1);  // don't retry the reload);
      // since we're returning "ignore" from onReloadFailure the value should be marked as fresh and not be reloaded until it expires again
      assertFalse(cachedValue.isExpired());  // should be considered "fresh"
    }

    {
      // expire it again using another use a mock Future that throws an exception,
      // but this time return a new Future from onReloadFailure in order to retry the refresh
      SettableFuture<Integer> retryFuture = simulateReloadFailureAndRetry(1);
      // complete the retry future
      retryFuture.set(expectedValue = 3);
      lastUpdateTime = currentTimeMillis;
      getAndVerify();
    }

    // TODO: extract method for simulating future exception
    {
      // expire it again using another use a mock Future that throws an exception,
      // but this time tell it to invalidate the cached value
      simulateReloadFailure(new Exception("Simulated exception (will invalidate)"), ValueLoader.FailedReloadAction.remove(), 1);
      // the failed get() should have returned the old value, but removed it from cache
      assertFalse(cachedValue.hasValue());  // should've been invalidated
      assertFalse(cachedValue.isExpired());  // an empty value shouldn't be considered expired

      // the next call to get() should call loader.load() again
      expectedValue = loader.valueToLoad = 4;
      expectedLoadCount++;
      lastUpdateTime = currentTimeMillis;
      getAndVerify();
      assertTrue(cachedValue.hasValue());
      assertFalse(cachedValue.isExpired());
    }

    {
      // test a sequence of failed reloads, with a mix of exceptions and timeouts,
      // to see what happens if the reload future doesn't finish before maxAgeMillis expires
      SettableFuture<Integer> retryFuture = simulateReloadFailureAndRetry(1);
      // instead of completing the reload future, we let it time out
      currentTimeMillis = last(loader.callHistory.getInvocationRecords("onReloadFailure")).getTimestamp()
          + cachedValue.getReloadTimeoutMillis() + 1;
      SettableFuture<Integer> retryFuture2 = SettableFuture.create();
      loader.delegate = new MockValueLoader() {
        @Nonnull
        @Override
        public FailedReloadAction<Integer> onReloadTimeout(long valueAge, int failCount) {
          System.out.println("Reload timed out (failCount = " + failCount + ")");
          assertEquals(elapsedMillis(lastUpdateTime), valueAge);  // still the same
          assertEquals(2, failCount);  // this is the 2nd failure in the reload attempt sequence
          return FailedReloadAction.retry(retryFuture2);
        }
      };
      expectedTimeoutCount++;
      getAndVerify();
      // fail the timeout retry as well
      SettableFuture<Integer> retryFuture3 = SettableFuture.create();
      getWithFailedFuture(retryFuture2, new Exception("Simulated exception after timeout"),
          ValueLoader.FailedReloadAction.retry(retryFuture3), 3);
      // complete the 3rd one successfully
      lastUpdateTime = ++currentTimeMillis;
      retryFuture3.set(expectedValue = 5);
      getAndVerify();
    }
  }

  private SettableFuture<Integer> simulateReloadFailureAndRetry(int expectedFailCount) {
    SettableFuture<Integer> retryFuture = SettableFuture.create();
    simulateReloadFailure(new Exception("Simulated exception (will retry)"),
        ValueLoader.FailedReloadAction.retry(retryFuture), expectedFailCount);
    assertTrue(cachedValue.isExpired());  // should still be considered expired
    return retryFuture;
  }

  private void simulateReloadFailure(Throwable exception, ValueLoader.FailedReloadAction<Integer> failedReloadAction, int expectedFailCount) {
    simulateReloadFailure(future -> future.setException(exception),
        t -> assertSame(exception, t),
        failedReloadAction, expectedFailCount);
  }

  private void simulateReloadFailure(Consumer<SettableFuture<Integer>> exceptionTrigger,
                                     Consumer<Throwable> exceptionVerifier,
                                     ValueLoader.FailedReloadAction<Integer> failedReloadAction,
                                     final int expectedFailCount) {
    SettableFuture<Integer> reloadFuture = expireAndVerifyReload();
    getWithFailedFuture(reloadFuture, exceptionTrigger, exceptionVerifier, failedReloadAction, expectedFailCount);
  }

  private void getWithFailedFuture(SettableFuture<Integer> futureToFail, Throwable exception, ValueLoader.FailedReloadAction<Integer> failedReloadAction, int expectedFailCount) {
    getWithFailedFuture(futureToFail,
        future -> future.setException(exception),
        t -> assertSame(exception, t),
        failedReloadAction, expectedFailCount);
  }

  private void getWithFailedFuture(SettableFuture<Integer> futureToFail, Consumer<SettableFuture<Integer>> exceptionTrigger, Consumer<Throwable> exceptionVerifier, ValueLoader.FailedReloadAction<Integer> failedReloadAction, int expectedFailCount) {
    exceptionTrigger.accept(futureToFail);
    currentTimeMillis++;
    long expectedValueAge = elapsedMillis(lastUpdateTime);
    loader.delegate = new MockValueLoader() {
      @Nonnull
      @Override
      public FailedReloadAction<Integer> onReloadFailure(Throwable cause, long valueAge, int failCount) {
        cause.printStackTrace(System.out);
        exceptionVerifier.accept(cause);
        assertEquals(expectedValueAge, valueAge);
        assertEquals(expectedFailCount, failCount);
        return failedReloadAction;
      }
    };
    this.expectedFailCount++;
    if (failedReloadAction.getAction() == ValueLoader.FailedReloadAction.Action.IGNORE) {
      // since we're returning "ignore" from onReloadFailure the value should be marked as fresh
      lastUpdateTime = currentTimeMillis;
    }
    getAndVerify();
  }

  /**
   * Calls {@link CachedValue#get()} and verifies our expectations about the current state
   */
  private void getAndVerify() {
    assertEquals(expectedValue, cachedValue.get());
    verifyExpectations();
  }

  private void verifyExpectations() {
    assertEquals(expectedLoadCount, loader.loadCount);
    assertEquals(expectedReloadCount, loader.reloadCount);
    assertEquals(expectedFailCount, loader.failCount);
    assertEquals(expectedTimeoutCount, loader.timeoutCount);
    assertEquals(cachedValue.hasValue() ? elapsedMillis(lastUpdateTime) : 0, cachedValue.getAge());
  }

  private SettableFuture<Integer> expireAndVerifyReload() {
    SettableFuture<Integer> future = SettableFuture.create();
    currentTimeMillis = lastUpdateTime + maxAgeMillis + 1;
    assertTrue(cachedValue.isExpired());
    assertFalse(future.isDone());
    loader.delegate = new MockValueLoader() {
      @Nonnull
      @Override
      public Future<Integer> reload(Integer oldValue) {
        assertEquals(expectedValue, oldValue);
        return future;
      }
    };

    // should call ValueLoader.reload and keep returning the old value until the future is done
    expectedReloadCount++;
    for (int i = 0; i < 10; i++) {
      currentTimeMillis++;
      getAndVerify();
      assertTrue(cachedValue.isExpired());  // still expired
    }
    return future;
  }

  public void testSet() throws Exception {
    AssertUtils.assertThrows(NullPointerException.class, () -> cachedValue.set(null));
    setAndVerify(100);
  }

  public void testRemove() throws Exception {
    assertValueNotSet();
    Integer newValue = 15;
    setAndVerify(newValue);
    cachedValue.remove();
    assertValueNotSet();
  }

  public void testRefresh() throws Exception {
    assertValueNotSet();
    // refresh shouldn't do anything when no value is set
    assertFalse(cachedValue.refresh());
    verifyExpectations();
    setAndVerify(123);

    // now that we've set a value, refresh should call reload immediately
    SettableFuture<Integer> future = refreshAndVerify();
    // while there is a pending future, the next refresh call shouldn't do anything
    assertFalse(cachedValue.refresh());
    getAndVerify();
    // once the future result is available, get() should return the new value, and we should be able to refresh once again
    future.set(expectedValue = 321);
    lastUpdateTime = ++currentTimeMillis;
    getAndVerify();
    refreshAndVerify();
  }

  /**
   * Verifies that the next call to {@link CachedValue#refresh()} succeeds and invokes {@link ValueLoader#reload(Object)}.
   * @return the future returned by {@link ValueLoader#reload(Object)}
   */
  private SettableFuture<Integer> refreshAndVerify() {
    SettableFuture<Integer> future = SettableFuture.create();
    loader.delegate = new MockValueLoader() {
      @Nonnull
      @Override
      public Future<Integer> reload(Integer oldValue) {
        assertEquals(expectedValue, oldValue);
        return future;
      }
    };
    assertTrue(cachedValue.refresh());
    expectedReloadCount++;
    verifyExpectations();
    return future;
  }

  private void setAndVerify(Integer newValue) {
    expectedValue = newValue;
    assertEquals(newValue, cachedValue.set(newValue));
    lastUpdateTime = currentTimeMillis;
    assertTrue(cachedValue.hasValue());
    assertFalse(cachedValue.isExpired());
    assertEquals(0, cachedValue.getAge());
    assertEquals(newValue, cachedValue.get());
    currentTimeMillis++;
    assertEquals(1, cachedValue.getAge());
  }

  private void assertValueNotSet() {
    assertFalse(cachedValue.hasValue());
    assertNull(cachedValue.remove());
    assertEquals(0, cachedValue.getAge());
  }

  private class TestValueLoader implements ValueLoader<Integer> {
    private int loadCount, reloadCount, failCount, timeoutCount;
    private ValueLoader<Integer> delegate;
    Integer valueToLoad = 1;
    private final InvocationRecorder callHistory = new InvocationRecorder(this);

    public TestValueLoader(ValueLoader<Integer> delegate) {
      this.delegate = delegate;
    }

    @Nonnull
    @Override
    public Integer load() {
      loadCount++;
      return valueToLoad;
    }

    @Nonnull
    @Override
    public Future<Integer> reload(Integer oldValue) {
      reloadCount++;
      callHistory.recordInvocation(currentTimeMillis, "reload", oldValue);
      if (delegate != null)
        return delegate.reload(oldValue);
      throw new IllegalStateException("Missing handler for ValueLoader.reload");
    }

    @Nonnull
    @Override
    public FailedReloadAction<Integer> onReloadFailure(Throwable cause, long valueAge, int failCount) {
      this.failCount++;
      callHistory.recordInvocation(currentTimeMillis, "onReloadFailure", cause, valueAge, failCount);
      if (delegate != null)
        return delegate.onReloadFailure(cause, valueAge, failCount);
      throw new IllegalStateException("Missing handler for ValueLoader.onReloadFailure");
    }

    @Nonnull
    @Override
    public FailedReloadAction<Integer> onReloadTimeout(long valueAge, int failCount) {
      timeoutCount++;
      callHistory.recordInvocation(currentTimeMillis, "onReloadTimeout", valueAge, failCount);
      if (delegate != null)
        return delegate.onReloadTimeout(valueAge, failCount);
      throw new IllegalStateException("Missing handler for ValueLoader.onReloadTimeout");
    }
  }

  private static class MockValueLoader implements ValueLoader<Integer> {

    @Nonnull
    @Override
    public Integer load() {
      fail("Unexpected invocation of MockValueLoader.load");
      return null;
    }

    @Nonnull
    @Override
    public Future<Integer> reload(Integer oldValue) {
      fail("Unexpected invocation of MockValueLoader.reload");
      return null;
    }

    @Nonnull
    @Override
    public FailedReloadAction<Integer> onReloadFailure(Throwable cause, long valueAge, int failCount) {
      fail("Unexpected invocation of MockValueLoader.onReloadFailure");
      return null;
    }

    @Nonnull
    @Override
    public FailedReloadAction<Integer> onReloadTimeout(long valueAge, int failCount) {
      fail("Unexpected invocation of MockValueLoader.onReloadTimeout");
      return null;
    }
  }
}