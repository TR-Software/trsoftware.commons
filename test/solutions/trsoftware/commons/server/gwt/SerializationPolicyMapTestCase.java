package solutions.trsoftware.commons.server.gwt;

import com.google.gwt.junit.client.impl.JUnitHost;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.SerializationPolicy;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Alex
 * @since 3/8/2023
 */
public class SerializationPolicyMapTestCase extends GwtArtifactsTestCase {

  protected void verifyExpectedPolicyNames(SerializationPolicyMap policyMap, Map<String, String> expectedPolicyNames) {
    assertEquals(expectedPolicyNames.keySet(), policyMap.getServiceClassNames());
    for (Map.Entry<String, String> entry : expectedPolicyNames.entrySet()) {
      assertEquals(entry.getValue(), policyMap.getPolicyStrongName(entry.getKey()));
    }
  }

  /**
   * @param serviceMethod will try to encode a response from this {@link RemoteService} method
   * @param methodResult the response object to serialize using the cached serialization policy
   */
  @SuppressWarnings("unchecked")
  protected void getAndValidateSerializationPolicy(SerializationPolicyMap policyMap, Method serviceMethod, Object methodResult) throws Exception {
    Class<? extends RemoteService> serviceClass = (Class<? extends RemoteService>)serviceMethod.getDeclaringClass();
    SerializationPolicy policy = policyMap.getSerializationPolicy(serviceClass.getName());
    assertNotNull(policy);
    // should return the same policy instance regardless of whether looking up by class or name of class
    assertSame(policy, policyMap.getSerializationPolicy(serviceClass));
    validateSerializationPolicy(policy, serviceMethod, methodResult);
  }

  protected void getAndValidateSerializationPolicyForJUnitHost(SerializationPolicyMap policyMap) throws Exception {
    // try encoding a dummy response from the JUnitHost.getTestBlock
    getAndValidateSerializationPolicy(policyMap,
        JUnitHost.class.getMethod("getTestBlock", int.class, JUnitHost.ClientInfo.class),
        new JUnitHost.InitialResponse(1,
            new JUnitHost.TestBlock(new JUnitHost.TestInfo[]{
                new JUnitHost.TestInfo(getClass().getPackage().getName(), getClass().getSimpleName(), getName())
            }, 0)));
  }

  protected void validateSerializationPolicyForJUnitHost(SerializationPolicy policy) throws Exception {
    // try encoding a dummy response from the JUnitHost.getTestBlock
    validateSerializationPolicy(policy,
        JUnitHost.class.getMethod("getTestBlock", int.class, JUnitHost.ClientInfo.class),
        new JUnitHost.InitialResponse(1,
            new JUnitHost.TestBlock(new JUnitHost.TestInfo[]{
                new JUnitHost.TestInfo(getClass().getPackage().getName(), getClass().getSimpleName(), getName())
            }, 0)));
    // TODO: remove code duplication with getAndValidateSerializationPolicyForJUnitHost
  }

  private void validateSerializationPolicy(SerializationPolicy policy, Method serviceMethod, Object response) throws SerializationException {
    String encodedResponse = RPC.encodeResponseForSuccess(serviceMethod, response, policy);
    System.out.printf("Serialized response from %s.%s:%n  %s%n",
        serviceMethod.getDeclaringClass().getName(), serviceMethod.getName(), encodedResponse);
    assertNotNull(encodedResponse);
  }
}
