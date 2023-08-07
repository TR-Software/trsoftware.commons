package solutions.trsoftware.commons.server.gwt;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gwt.junit.client.impl.JUnitHost;
import com.google.gwt.logging.shared.RemoteLoggingService;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import org.apache.catalina.Context;
import solutions.trsoftware.commons.client.bridge.rpc.NumberFormatTestService;
import solutions.trsoftware.commons.client.server.MockRpcService;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.util.RandomUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.function.ThrowingFunction;
import solutions.trsoftware.commons.shared.util.function.ThrowingRunnable;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static solutions.trsoftware.commons.server.testutil.ServerAssertUtils.assertEqualsByReflection;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;

/**
 * @author Alex
 * @since 3/8/2023
 */
@Slow
public class SerializationPolicyCacheTest extends SerializationPolicyMapTestCase {

  private Context combinedApp;

  @Override
  protected void addWebApps() throws ServletException {
    combinedApp = addWebapp("SerializationPolicyCacheTest");
  }

  @Override
  protected void tearDown() throws Exception {
    combinedApp = null;
    super.tearDown();
  }

  public void testSerializationPolicyCache() throws Exception {
    SerializationPolicyCache cache = new SerializationPolicyCache(combinedApp.getServletContext());
    System.out.println("cache.getModuleNames() = " + cache.getModuleNames());
    Map<String, Set<Class<? extends RemoteService>>> expectedServicesByModule = ImmutableMap.of(
        "solutions.trsoftware.commons.TestCommons.JUnit.Hosted",
        ImmutableSet.of(NumberFormatTestService.class, JUnitHost.class, MockRpcService.class),
        "solutions.trsoftware.commons.TestCommons.JUnit.Web",
        ImmutableSet.of(JUnitHost.class, RemoteLoggingService.class)
    );
    Set<String> expectedModules = expectedServicesByModule.keySet();
    assertEquals(expectedModules, cache.getModuleNames());
    Map<String, SerializationPolicyMap> expectedPolicyMaps = expectedModules.stream()
        .collect(Collectors.toMap(Function.identity(), ThrowingFunction.unchecked(this::getExpectedPolicyMap)));
    assertEquals(expectedPolicyMaps, cache.getPoliciesByModuleName());
    for (Map.Entry<String, Set<Class<? extends RemoteService>>> servicesForModule : expectedServicesByModule.entrySet()) {
      verifyPoliciesForModule(cache, servicesForModule.getKey(), servicesForModule.getValue());
    }

    // test some error cases:
    {
      String invalidModuleName = randString();
      String invalidServiceName = randString();
      // 1) non-existent module
      assertNull(cache.getPolicyMap(invalidModuleName));
      // 2) non-existent module/service pair
      assertThrows(PolicyNotFoundException.class, (ThrowingRunnable)() -> cache.getSerializationPolicy(invalidModuleName, invalidServiceName));
      // 3) valid module that doesn't contain a given service: already tested in verifyPoliciesForModule
    }

    // test getPoliciesForService
    // 1) the JUnitHost policy is contained in both modules
    verifyPoliciesForService(cache, JUnitHost.class, expectedModules);
    // 2) the MockRpcService policy is contained only in the "Hosted" module
    verifyPoliciesForService(cache, MockRpcService.class, Collections.singleton("solutions.trsoftware.commons.TestCommons.JUnit.Hosted"));
  }

  private void verifyPoliciesForService(SerializationPolicyCache cache, Class<? extends RemoteService> serviceClass, Set<String> expectedModules) throws PolicyNotFoundException {
    Map<String, SerializationPolicy> policiesByModuleName = cache.getPoliciesForService(serviceClass);
    assertEquals(policiesByModuleName, cache.getPoliciesForService(serviceClass.getName()));
    assertEquals(expectedModules, policiesByModuleName.keySet());
    for (Map.Entry<String, SerializationPolicy> entry : policiesByModuleName.entrySet()) {
      String moduleName = entry.getKey();
      assertSame(cache.getSerializationPolicy(moduleName, serviceClass), entry.getValue());
    }
  }

  private void verifyPoliciesForModule(SerializationPolicyCache cache, String moduleName, Set<Class<? extends RemoteService>> expectedServiceClasses) throws Exception {
    SerializationPolicyMap expectedPolicyMap = getExpectedPolicyMap(moduleName);
    SerializationPolicyMap policyMap = cache.getPolicyMap(moduleName);
    System.out.printf("Testing policy map for module %s: %s%n", moduleName, policyMap);
    assertNotNull(policyMap);
    assertEquals(expectedPolicyMap, policyMap);

    for (Class<? extends RemoteService> serviceClass : expectedServiceClasses) {
      SerializationPolicy policy = cache.getSerializationPolicy(moduleName, serviceClass.getName());
      assertNotNull(policy);
      // instance should be cached
      assertSame(policy, cache.getSerializationPolicy(moduleName, serviceClass));
      // NOTE: not testing actual serialization with the policy, since that's already tested in SerializationPolicyMapTest
      assertEqualsByReflection(expectedPolicyMap.getSerializationPolicy(serviceClass), policy);
    }

    // should throw exception for a non-existent service
    String invalidServiceName = randString();
    assertThrows(PolicyNotFoundException.class, (ThrowingRunnable)() -> cache.getSerializationPolicy(moduleName, invalidServiceName));
  }

  @Nonnull
  private SerializationPolicyMap getExpectedPolicyMap(String moduleName) throws PolicyNotFoundException {
    return new SerializationPolicyMap(moduleName, combinedApp.getServletContext());
  }

  @Nonnull
  private String randString() {
    return RandomUtils.randString(10, StringUtils.ASCII_LETTERS);
  }
}