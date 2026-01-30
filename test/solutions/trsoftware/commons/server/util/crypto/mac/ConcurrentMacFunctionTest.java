package solutions.trsoftware.commons.server.util.crypto.mac;

import solutions.trsoftware.commons.server.util.crypto.MacFunction;
import solutions.trsoftware.commons.server.util.crypto.MacFunctionTestCase;

/**
 * @author Alex
 * @since 1/13/2026
 */
public class ConcurrentMacFunctionTest extends MacFunctionTestCase {

  protected MacFunction createMacFunction(String algorithm, byte[] keyBytes) {
    return new ConcurrentMacFunction(algorithm, keyBytes);
  }
}