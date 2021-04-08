/*
 * Copyright 2021 TR Software Inc.
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

package solutions.trsoftware.commons.client.debug;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import solutions.trsoftware.commons.client.widgets.Widgets;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Allows throwing dummy exceptions to test the exception handling logic in the application's
 * {@link GWT.UncaughtExceptionHandler}.
 *
 * @author Alex
 * @since Apr 5, 2013
 */
public class ExceptionSimulator extends Composite {

  /** Causes a {@link NullPointerException} to be triggered indirectly as a result of running this method */
  public static void triggerFakeNPE() {
    final int[] foo = null;
    Window.alert("foo[3]="+foo[3]); // will throw NPE
  }

  /** Throws a RuntimeException using an explicit throw statement */
  public static void throwRuntimeException() {
    RuntimeException ex = new RuntimeException("ExceptionSimulator dummy exception");
    // NOTE: in Java, the stack trace of an exception will start on the line where the
    // constructor of the exception is invoked, not where the throw statement is,
    // because the stack trace is created by the exception's constructor
    throw ex;
  }


  /** Causes an exception deep in the GWT widget hierarchy */
  public static void triggerWidgetDomException() {
    RootPanel.get().add(new Composite(){});  // Composite will throw an exception saying that initWidget hasn't been called on this anon Composite
  }

  /** Causes a Javascript {@code ReferenceError} by using a name that hasn't been declared or assigned */
  public static native boolean triggerJavascriptReferenceError() /*-{
    var x = 5;
    // emperorsNewClothes has never been declared, so it can't be used in any
    // capacity other than being assigned without triggering a ReferenceError
    return mickeyMouse = 4, emperorsNewClothes > x;  // mickeyMouse will not trigger a ReferenceError though
  }-*/;

  /** Causes a Javascript {@code TypeError} by trying to use a {@code number} variable as a function */
  public static native boolean triggerJavascriptTypeError() /*-{
    var x = 5;
    return x();  // will throw a TypeError
  }-*/;


  public static void triggerForEachLoopNPE() {
    List foo = null;
    for (Object x : foo) {
      Window.alert("x" + foo);
    }
  }

  public static native String triggerExceptionInReturnExpression(String x) /*-{
    var f;
    function Foo() {
      this.toString = function() {return mickeyMouse2}
    }
    f = new Foo();
    f++; // will trigger reference error for mickeyMouse2
    return f + x;
  }-*/;

  public ExceptionSimulator() {
    initWidget(Widgets.horizontalPanel(
        new Label("ExceptionSimulator: "),
        new Button("RuntimeException", new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            throwRuntimeException();
          }
        }),
        new Button("NullPointerException", new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            triggerFakeNPE();
          }
        }),
        new Button("JavaScript TypeError", new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            triggerJavascriptTypeError();
          }
        }),
        new Button("JavaScript ReferenceError Demo", new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            triggerJavascriptReferenceError();
          }
        }),
        new Button("All Simulated Exceptions", new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            triggerAllExceptions();
          }
        })
    ));
  }

  /**
   * Throws an {@link UmbrellaException} containing all the exceptions that this class simulates.
   */
  public static void triggerAllExceptions() {
    // TODO: implement something like this as a web mode unit test (with Selenium or something)
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      public void execute() {
        ExceptionSimulator.throwRuntimeException();
      }
    });
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      public void execute() {
        // wrap the other two in an UmbrellaException
        Set<Throwable> throwables = new HashSet<Throwable>();
        try {
          ExceptionSimulator.triggerWidgetDomException();
        }
        catch (Throwable ex) {
          throwables.add(ex);
        }
        try {
          ExceptionSimulator.triggerForEachLoopNPE();
        }
        catch (Throwable ex) {
          throwables.add(ex);
        }
        try {
          Window.alert("ExceptionSimulator.triggerJavascriptReferenceError()=" + ExceptionSimulator.triggerJavascriptReferenceError());
        }
        catch (Throwable ex) {
          throwables.add(ex);
        }
        try {
          Window.alert("ExceptionSimulator.triggerJavascriptTypeError()=" + ExceptionSimulator.triggerJavascriptTypeError());
        }
        catch (Throwable ex) {
          throwables.add(ex);
        }
        try {
          Window.alert("ExceptionSimulator.triggerExceptionInReturnExpression()=" + ExceptionSimulator.triggerExceptionInReturnExpression("foo"));
        }
        catch (Throwable ex) {
          throwables.add(ex);
        }
        if (!throwables.isEmpty())
          throw new UmbrellaException(throwables);
      }
    });
    // finally, throw one directly
    ExceptionSimulator.triggerFakeNPE();
  }


}
  