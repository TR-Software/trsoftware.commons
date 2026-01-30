package solutions.trsoftware.commons.server.util.crypto.mac;

import solutions.trsoftware.commons.server.util.crypto.MacFunction;
import solutions.trsoftware.commons.server.util.crypto.MacFunctionTestCase;

/**
 * @author Alex
 * @since 1/13/2026
 */
public class MacFunctionPrototypeTest extends MacFunctionTestCase {

  protected MacFunction createMacFunction(String algorithm, byte[] keyBytes) {
    return new MacFunctionPrototype(algorithm, keyBytes);
  }
}