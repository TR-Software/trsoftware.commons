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

package solutions.trsoftware.commons.client.controller;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.SimpleEventBus;
import solutions.trsoftware.commons.client.Messages;
import solutions.trsoftware.commons.client.images.CommonsImages;
import solutions.trsoftware.commons.client.logging.Log;
import solutions.trsoftware.commons.client.widgets.popups.ModalDialog;
import solutions.trsoftware.commons.client.widgets.popups.PleaseWaitPopup;

/**
 * @author Alex, 9/22/2017
 */
public abstract class BaseRpcAction<T> implements Command, AsyncCallback<T> {

  /**
   * Any further attempt to invoke an RPC will result in user being prompted to reload the page.
   * @see #suspendRPCsAndPromptToReloadPage()
   */
  private static boolean suspendedUntilPageReload;
  /**
   * Will be used to track whether user clicks "Cancel" when prompted to reload the page.
   * @see #suspendRPCsAndPromptToReloadPage()
   */
  private static boolean reloadPromptShowing;
  /** allows timing RPC calls */
  protected double startTime;
  /** allows timing RPC calls */
  protected double endTime;
  private PleaseWaitPopup busyPopup;

  /** Name of the concrete action class */
  protected String name;

  private EventBus eventBus;

  protected BaseRpcAction() {
    name = getClass().getName();
    if (name.contains("$")) // add super's name to anonymous inner classes
      name += " extends " + getClass().getSuperclass().getName();
  }

  /**
   * @param name A name for this action (for debugging purposes)
   */
  protected BaseRpcAction(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  /**
   * Shows a modal dialog telling the user he needs to reload the page and do the reload automatically if he hits "OK".
   * This method may return before the user has responded to the dialog. TODO: explain this
   */
  protected static void suspendRPCsAndPromptToReloadPage() {
    suspendedUntilPageReload = true;
    maybePromptToReloadPage();
  }

  protected static void maybePromptToReloadPage() {
    if (!reloadPromptShowing) {
      reloadPromptShowing = true;
      ModalDialog.softConfirm(Messages.get().reloadAppMessage(), new ModalDialog.ResponseHandler<Boolean>() {
        @Override
        public void handleDialogResponse(Boolean response) {
          if (response)
            Window.Location.reload();
          reloadPromptShowing = false;
        }
      });
    }
  }

  public final void onFailure(Throwable caught) {
    endTime = Duration.currentTimeMillis();
    if (Log.ENABLED) {
      GWT.log("BaseRpcAction.onFailure", caught);
      Log.write("Call to " + name + " failed ( " + getRoundTripTime() + " ms): " + caught.getClass().getName() + ": " + caught.getMessage());
    }
    if (caught instanceof IncompatibleRemoteServiceException) {
      /* When GameServiceServlet determines that the client code version doesn't match what's currently deployed on the server,
      it will return an IncompatibleRemoteServiceException response (using RPC.encodeResponseForFailure) to the RPC.
      However there seems to be a GWT bug with the client-side deserialization of that response
      because AbstractSerializationStreamReader.readObject computes typeSignature = "com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException/3936916533",
      instead of an obfuscate typeSignature (like 's' or 'c'); example normal response: //OK[0,15,1.443469202763E12,0,11,0,0,0,0,2,14,0.0,0,13,0,0,0,0.0,0.0,0.0,12,11,0,0,10,9,8,0,7,0,6,5,0,4,0,3,2,1,["s","c","6","g","126A7FC4E5080DE58FC56EC1D6A4AD8D","w","us","h","Guest","","guest:2457920816943966115","j","1t","r","10"],1,7],
      Therefore this method will receive IncompatibleRemoteServiceException("The response could not be deserialized") from RpcCallbackAdapter.java:93,
      which is perfectly fine, because although it's not the same IncompatibleRemoteServiceException object that was in the server's response,
      it's still an IncompatibleRemoteServiceException, so we just ignore the fact that its message is "The response could not be deserialized",
      and handle it as though it signifies that the client's app code version is incompatible. */
      suspendRPCsAndPromptToReloadPage();
    }
    else {
      // for any other exception, let the subclass handle it
      handleFailure(caught);
    }
    getEventBus().fireEventFromSource(new FailureEvent(caught), this);
  }

  /** Subclasses should override to provide handling for exceptions that might be thrown by their particular RPCs */
  protected void handleFailure(Throwable caught) {
    throw new RuntimeException(caught); // this is a last resort; let the UncaughtExceptionHandler deal with it
  }

  protected abstract void handleSuccess(T result);

  public int getRoundTripTime() {
    return (endTime == 0) ? 0 : (int)(endTime - startTime);
  }

  public final void onSuccess(T result) {
    endTime = Duration.currentTimeMillis();
    Log.write("Call to " + name + " succeeded (" + getRoundTripTime() + " ms roundtrip).");
    handleSuccess(result);
    getEventBus().fireEventFromSource(new SuccessEvent<T>(result), this);
    onFinished();
  }

  /**
   * Called after either {@link #onSuccess(Object)} or {@link #onFailure(Throwable)} to perform any cleanup task
   * that needs to happen regardless of success or failure of the action.
   */
  protected void onFinished() {
    if (busyPopup != null)
      busyPopup.hide();
    getEventBus().fireEventFromSource(new FinishedEvent(), this);
  }

  /**
   * Causes the Command to perform its encapsulated behavior.
   */
  public final void execute() {
    if (suspendedUntilPageReload) {
      maybePromptToReloadPage();
      Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
          onFinished();  // since neither onSuccess nor onFailure will ever be called, we call onFinally to allow the subclass to clean up (e.g. hide a popup dialog that triggered this action)
        }
      });
    }
    else {
      // invoke the RPC call
      startTime = Duration.currentTimeMillis();
      endTime = 0;  // reset the last value, if any
      if (Log.ENABLED)
        Log.write("Invoking " + name);
      if (busyPopup != null)
        busyPopup.showRelativeToWindow(.5, .333);
      getEventBus().fireEventFromSource(new ExecuteEvent(), this);
      executeRpcAction();
    }
  }

  protected abstract void executeRpcAction();

  /**
   * Subclasses that wish to show a "please wait" popup message while the RPC is executing should call
   * this method with their customized message.
   */
  public void showBusyMessage(String message) {
    if (busyPopup == null)
      busyPopup = new PleaseWaitPopup(message, AbstractImagePrototype.create(CommonsImages.INSTANCE.info24()));
    if (!busyPopup.isShowing())
      busyPopup.showRelativeToWindow(.5, .333);
  }

  public void setBusyPopup(PleaseWaitPopup busyPopup, boolean modal) {
    this.busyPopup = busyPopup;
    busyPopup.setGlassEnabled(modal);
  }

  /**
   * Lazy-inits {@link #eventBus} and returns it.
   */
  private EventBus getEventBus() {
    if (eventBus == null)
      eventBus = createEventBus();  // lazy init
    return eventBus;
  }

  /**
   * @return A new instance of {@link SimpleEventBus}.
   * Subclasses may override to provide a different {@link EventBus} implementation.
   */
  protected EventBus createEventBus() {
    return new SimpleEventBus();
  }

  public HandlerRegistration addExecuteHandler(ExecuteEvent.Handler handler) {
    return getEventBus().addHandlerToSource(ExecuteEvent.TYPE, this, handler);
  }

  public HandlerRegistration addFinishedHandler(FinishedEvent.Handler handler) {
    return getEventBus().addHandlerToSource(FinishedEvent.TYPE, this, handler);
  }

  public HandlerRegistration addSuccessHandler(SuccessEvent.Handler handler) {
    return getEventBus().addHandlerToSource(SuccessEvent.TYPE, this, handler);
  }

  public HandlerRegistration addFailureHandler(FailureEvent.Handler handler) {
    return getEventBus().addHandlerToSource(FailureEvent.TYPE, this, handler);
  }

}
