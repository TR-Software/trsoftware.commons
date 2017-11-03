package solutions.trsoftware.jsonp.client;

import com.google.gwt.json.client.JSONValue;

/**
 * Defines a callback action for a JSONP remote service.
 *
 * @author Alex
 */
public interface JsonpCallback {

  void execute(JSONValue result);

  /** Called if the JSONP call times out */
  void onTimeout();
}
