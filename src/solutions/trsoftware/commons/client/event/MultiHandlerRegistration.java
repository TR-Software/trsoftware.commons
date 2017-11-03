package solutions.trsoftware.commons.client.event;


import com.google.web.bindery.event.shared.HandlerRegistration;
import solutions.trsoftware.commons.client.util.callables.Function1_;
import solutions.trsoftware.commons.client.util.callables.Functions;

import java.util.Arrays;

/**
 * Combines several handler registrations into one.
 *
 * @author Alex, 10/6/2015
 */
public class MultiHandlerRegistration implements com.google.gwt.event.shared.HandlerRegistration {

  private final HandlerRegistration[] registrations;

  public MultiHandlerRegistration(HandlerRegistration...registrations) {
    this.registrations = registrations;
  }

  @Override
  public void removeHandler() {
    Functions.tryCall(Arrays.asList(registrations), new Function1_<HandlerRegistration>() {
      @Override
      public void call(HandlerRegistration reg) {
        if (reg != null)
          reg.removeHandler();
      }
    });
  }
}
