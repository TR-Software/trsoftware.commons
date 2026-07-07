package solutions.trsoftware.commons.client.controller;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.StatusCodeException;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.bundle.CommonsCss;
import solutions.trsoftware.commons.client.controller.BaseRpcAction.State;
import solutions.trsoftware.commons.client.jso.JsDocument;
import solutions.trsoftware.commons.client.server.MockRpcService;
import solutions.trsoftware.commons.client.server.MockRpcServiceAsync;
import solutions.trsoftware.commons.client.widgets.popups.ModalDialog;
import solutions.trsoftware.commons.shared.testutil.MockException;
import solutions.trsoftware.commons.shared.testutil.TestUtils;
import solutions.trsoftware.commons.shared.util.Area2d;
import solutions.trsoftware.commons.shared.util.Box;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static solutions.trsoftware.commons.client.controller.BaseRpcAction.State.*;

/**
 * @author Alex
 * @since 6/30/2026
 */
public class BaseRpcActionTest extends CommonsGwtTestCase {

  /** Backup for OG value of {@link ModalDialog#getMinDelayBetweenDialogs()} */
  private int minDialogShowingTime;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    // need to adjust ModalDialog settings for testIncompatibleRemoteServiceException
    minDialogShowingTime = ModalDialog.getMinDialogShowingTime();
    // allow click on the "Cancel" button of the BaseRpcAction reload prompt dialog to execute the response handler right away
    ModalDialog.setMinDialogShowingTime(0);
  }

  @Override
  protected void gwtTearDown() throws Exception {
    super.gwtTearDown();
    // restore the settings we changed for testIncompatibleRemoteServiceException
    ModalDialog.setMinDialogShowingTime(minDialogShowingTime);
    // reset suspendedUntilPageReload field to not interfere with other tests if testIncompatibleRemoteServiceException fails
    BaseRpcAction.setSuspendedUntilPageReload(false);

  }

  public void testOnSuccess() {
    executeAndVerifySuccess(new EnlargeAreaAction(new Area2d(2, 3), 2, 1),
        result -> assertEquals(new Area2d(4, 4), result), true
    );
  }
  
  public void testOnFailure() {
    int i = 0;
    TestUtils.printSectionHeader(++i + ") MockRpcService.enlargeArea with null arg");
    // this server call will throw NullPointerException (area arg is null), passed to callback as a StatusCodeException ("500 Server Error The call failed on the server; see server log for details")
    executeAndVerifyFailure(new EnlargeAreaAction(null, 2, 1),
        exception -> assertTrue(exception instanceof StatusCodeException),
        false
    );
    TestUtils.printSectionHeader(++i + ") MockRpcService.throwException");
    String exMessage = getName();
    executeAndVerifyFailure(new ThrowExceptionAction(MockException.class.getName(), exMessage),
        exception -> {
          assertEquals(new MockException(exMessage), exception);
        }, true
    );
  }

  public void testIncompatibleRemoteServiceException() {
    EnlargeAreaAction action = new EnlargeAreaAction(new Area2d(1, 1), 1, 1);
    assertFalse(BaseRpcAction.isSuspendedUntilPageReload());
    action.onFailure(new IncompatibleRemoteServiceException(getName()));
    assertTrue(BaseRpcAction.isSuspendedUntilPageReload());
    assertTrue(BaseRpcAction.isReloadPromptShowing());
    // document.querySelectorAll('.SoftModalDialogBox .gwt-Button')[1].click()
    // physically verify that the ModalDialog.softConfirm prompt is attached to the DOM
    NodeList<Element> promptButtons = JsDocument.get().querySelectorAll("." + CommonsCss.get().SoftModalDialogBox() + " .gwt-Button");
    assertNotNull(promptButtons);
    assertEquals(2, promptButtons.getLength());


    EnlargeAreaAction action2 = new EnlargeAreaAction(new Area2d(2, 2), 2, 2);
    action2.addEventHandlers();
    action2.addFinishedHandler(event -> {
      assertSame(action2, event.getSource());
      verifyOutcome(action2, REJECTED, null);
      /*
       Hide the reload page prompt (ModalDialog.softConfirm displayed from BaseRpcAction.maybePromptToReloadPage)
       and reset suspendedUntilPageReload field to not interfere with other tests
      */
      // click on the "Cancel" button of the ModalDialog.softConfirm prompt
      ButtonElement.as(promptButtons.getItem(1)).click();
      // run the rest of this test in a deferred command, after the above click event gets handled
      Scheduler.get().scheduleDeferred(() -> {
        assertFalse(BaseRpcAction.isReloadPromptShowing());
        // reset suspendedUntilPageReload field to allow the next action to execute normally (o/w it would stay true until actual page reload)
        BaseRpcAction.setSuspendedUntilPageReload(false);

        // after clearing suspendedUntilPageReload, the next action should execute normally
        EnlargeAreaAction action3 = new EnlargeAreaAction(new Area2d(3, 3), 3, 3);
        executeAndVerifySuccess(action3,
            result -> assertEquals(new Area2d(6, 6), result), true
        );
      });
    });
//    delayTestFinish(1000);
    delayTestFinish(120_000);
    action2.execute();
    // this action should be rejected right away b/c BaseRpcAction.suspendedUntilPageReload
    assertEquals(REJECTED, action2.getState());
    assertNull(action2.getOutcome());
  }

  /**
   * Verifies the state and outcome of the given RPC action after it's finished.
   * This method should be invoked from a {@link FinishedEvent} handler.
   *
   * @param rpc a finished action
   * @param state expected final state of the action
   * @param outcomeAssertion either a {@link SuccessAssertion}, a {@link FailureAssertion}, or {@code null},
   *   depending on the expected outcome
   * @param <T> the RPC result type
   * @throws IllegalArgumentException if {@code outcomeAssertion} is neither {@code null}, {@link SuccessAssertion}, nor {@link FailureAssertion}
   */
  private <T> void verifyOutcome(MockRpcServiceAction<T> rpc, State state, @Nullable Consumer<?> outcomeAssertion) {
    Preconditions.checkArgument(outcomeAssertion instanceof SuccessAssertion || outcomeAssertion instanceof FailureAssertion || state == REJECTED,
        "outcomeAssertion");
    assertEquals(state, rpc.getState());
    assertTrue(state.isFinished());
    boolean rejected = state == REJECTED;
    boolean success = state == SUCCESS;
    boolean failure = state == FAILURE;
    // verify that the appropriate abstract methods and event handlers were invoked
    assertEquals(!rejected, rpc.executeEventFired);
    assertEquals(!rejected, rpc.executeEventFired);
    assertEquals(success, rpc.handleSuccessInvoked.hasValue());
    assertEquals(success, rpc.successEventFired);
    assertEquals(failure, rpc.handleFailureInvoked.hasValue());
    assertEquals(failure, rpc.failureEventFired);
    assertTrue(rpc.onFinishedInvoked);
    assertTrue(rpc.finishedEventFired);

    // verify the outcome value
    BaseRpcAction<T>.Outcome outcome = rpc.getOutcome();
    if (success) {
      assertTrue(outcome instanceof BaseRpcAction.Success);
      //noinspection unchecked,ConstantConditions
      ((SuccessAssertion<T>)outcomeAssertion).accept(((BaseRpcAction<T>.Success)outcome).getResult());
    }
    else if (failure) {
      assertTrue(outcome instanceof BaseRpcAction.Failure);
      ((FailureAssertion)outcomeAssertion).accept(((BaseRpcAction<T>.Failure)outcome).getException());
    }
    else
      assertNull(outcome);
  }


  /**
   * Executes the given RPC action, and verifies that the outcome will be {@link BaseRpcAction#onSuccess(Object)}
   * using a {@link FinishedEvent} handler.
   *
   * @param rpc the action to execute
   * @param successAssertion function that verifies the object returned by the RPC
   * @param finishTest whether to invoke {@link #finishTest()} at the end of the {@link FinishedEvent} handler;
   *   can pass {@code false} when testing multiple RPCs.
   * @param <T> the RPC result type
   */
  private <T> void executeAndVerifySuccess(MockRpcServiceAction<T> rpc, SuccessAssertion<T> successAssertion, boolean finishTest) {
    executeAndVerify(rpc, successAssertion, finishTest);
  }

  /**
   * Executes the given RPC action, and verifies that the outcome will be {@link BaseRpcAction#onFailure(Throwable)}
   * using a {@link FinishedEvent} handler.
   *
   * @param rpc the action to execute
   * @param failureAssertion function that verifies the exception thrown by the RPC
   * @param finishTest whether to invoke {@link #finishTest()} at the end of the {@link FinishedEvent} handler;
   *   can pass {@code false} when testing multiple RPCs.
   * @param <T> the RPC result type
   */
  private <T> void executeAndVerifyFailure(MockRpcServiceAction<T> rpc, FailureAssertion failureAssertion, boolean finishTest) {
    executeAndVerify(rpc, failureAssertion, finishTest);
  }

  /**
   * Executes the given RPC action and invokes the given assertion on the outcome using a {@link FinishedEvent} handler.
   *
   * @param rpc the action to execute
   * @param outcomeAssertion either a {@link SuccessAssertion} or a {@link FailureAssertion}
   * @param finishTest whether to invoke {@link #finishTest()} at the end of the {@link FinishedEvent} handler;
   *   can pass {@code false} when testing multiple RPCs.
   * @param <T> the RPC result type
   * @throws IllegalArgumentException if {@code outcomeAssertion} is neither a {@link SuccessAssertion} nor a {@link FailureAssertion}
   */
  private <T> void executeAndVerify(MockRpcServiceAction<T> rpc, Consumer<?> outcomeAssertion, boolean finishTest) {
    Preconditions.checkArgument(outcomeAssertion instanceof SuccessAssertion || outcomeAssertion instanceof FailureAssertion,
        "outcomeAssertion");
    boolean success = outcomeAssertion instanceof SuccessAssertion;
    State expectedState = success ? SUCCESS : FAILURE;

    // execute the RPC and verify the expected outcome
    assertEquals(NEW, rpc.getState());
    assertNull(rpc.getOutcome());
    rpc.addEventHandlers();
    rpc.execute();
    assertEquals(EXECUTING, rpc.getState());
    delayTestFinish(500);

    rpc.addFinishedHandler(event -> {
      assertSame(rpc, event.getSource());
      verifyOutcome(rpc, expectedState, outcomeAssertion);
      if (finishTest)
        finishTest();
    });
  }

  interface SuccessAssertion<T> extends Consumer<T> {
    @Override
    void accept(T result);
  }
  interface FailureAssertion extends Consumer<Throwable> {
    @Override
    void accept(Throwable exception);
  }


  static abstract class MockRpcServiceAction<T> extends BaseRpcAction<T> {
    // these fields can be used to check whether the corresponding methods have been invoked by BaseRpcAction

    /** Arg passed to {@link #handleSuccess(Object)} if that method was invoked */
    protected final Box<T> handleSuccessInvoked = new Box<>();
    /** Arg passed to {@link #handleFailure(Throwable)} if that method was invoked */
    protected final Box<Throwable> handleFailureInvoked = new Box<>();
    /** {@code true} if {@link #onFinished()} was invoked */
    protected boolean onFinishedInvoked;

    protected boolean executeEventFired;
    protected boolean finishedEventFired;
    protected boolean successEventFired;
    protected boolean failureEventFired;

    @Override
    protected void handleFailure(Throwable caught) {
      handleFailureInvoked.setValue(caught);
      verifyFailure(caught);
    }

    @Override
    protected void handleSuccess(T result) {
      handleSuccessInvoked.setValue(result);
      verifySuccess(result);
    }

    private void verifySuccess(T result) {
      assertEquals(SUCCESS, getState());
      BaseRpcAction<T>.Outcome outcome = getOutcome();
      assertTrue(outcome instanceof BaseRpcAction.Success);
      assertSame(result, ((BaseRpcAction<T>.Success)outcome).getResult());
    }

    private void verifyFailure(Throwable caught) {
      assertEquals(FAILURE, getState());
      BaseRpcAction<T>.Outcome outcome = getOutcome();
      assertTrue(outcome instanceof BaseRpcAction.Failure);
      assertSame(caught, ((BaseRpcAction<T>.Failure)outcome).getException());
    }

    @Override
    protected void onFinished() {
      super.onFinished();
      onFinishedInvoked = true;
    }

    /**
     * Adds handlers for all {@link RpcEvent}s to verify the fired events using the corresponding fields
     * (e.g. {@link #executeEventFired}, {@link #successEventFired}, etc.)
     */
    void addEventHandlers() {
      addExecuteHandler(event -> {
        executeEventFired = true;
        assertSame(this, event.getSource());
      });
      addFinishedHandler(event -> {
        finishedEventFired = true;
        assertSame(this, event.getSource());
      });
      addSuccessHandler(event -> {
        successEventFired = true;
        assertSame(this, event.getSource());
        verifySuccess(event.getResult());
      });
      addFailureHandler(event -> {
        failureEventFired = true;
        assertSame(this, event.getSource());
        verifyFailure(event.getException());
      });
    }
  }

  /**
   * Invokes {@link MockRpcService#enlargeArea(Area2d, int, int)}
   */
  static class EnlargeAreaAction extends MockRpcServiceAction<Area2d> {
    private final Area2d area;
    private final int width, height;

    EnlargeAreaAction(Area2d area, int width, int height) {
      this.area = area;
      this.width = width;
      this.height = height;
    }

    @Override
    protected void executeRpcAction() {
      MockRpcServiceAsync.INSTANCE.get().enlargeArea(area, width, height, this);
    }
  }

  /**
   * Invokes {@link MockRpcService#throwException(String, String)}
   */
  static class ThrowExceptionAction extends MockRpcServiceAction<Void> {
    private final String clsName;
    private final String message;

    ThrowExceptionAction(String clsName, String message) {
      this.clsName = clsName;
      this.message = message;
    }

    @Override
    protected void executeRpcAction() {
      MockRpcServiceAsync.INSTANCE.get().throwException(clsName, message, this);
    }
  }
}