/*
 * Copyright 2023 TR Software Inc.
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

package solutions.trsoftware.commons.server.gwt;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.logging.shared.RemoteLoggingService;
import solutions.trsoftware.commons.client.bridge.rpc.NumberFormatTestService;
import solutions.trsoftware.commons.client.server.MockRpcService;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.util.Area2d;
import solutions.trsoftware.commons.shared.util.RandomUtils;

import javax.servlet.ServletContext;
import java.util.logging.LogRecord;
import java.util.stream.DoubleStream;

/**
 * @author Alex
 * @since 3/4/2023
 */
@Slow
public class SerializationPolicyMapTest extends SerializationPolicyMapTestCase {

  public void testGetSerializationPolicy() throws Exception {
    /*
      1) the "gwtWebModeTests" app contains a manifest.txt file with all the policy mappings
    */
    {
      ServletContext servletContext = webModeApp.getServletContext();
      SerializationPolicyMap policyMap = new SerializationPolicyMap(MODULE_NAME, servletContext);
      verifyExpectedPolicyNames(policyMap, ImmutableMap.of(
          "com.google.gwt.junit.client.impl.JUnitHost", "C63F04478389D766205EADF6DDB3A13C",
          "com.google.gwt.logging.shared.RemoteLoggingService", "08FAFA5EB62F7040921DAB5F0E2BA17F"
      ));
      // a) test the JUnitHost service serialization policy
      getAndValidateSerializationPolicyForJUnitHost(policyMap);
      // b) test the RemoteLoggingService service serialization policy
      getAndValidateSerializationPolicy(policyMap, RemoteLoggingService.class.getMethod("logOnServer", LogRecord.class), null);
    }
    /*
      2) the "gwtHostedModeTests" app contains an empty manifest.txt, so policy names have to be derived
         from the files in the manifests subdir
    */
    {
      ServletContext servletContext = hostedModeApp.getServletContext();
      SerializationPolicyMap policyMap = new SerializationPolicyMap(MODULE_NAME, servletContext);
      verifyExpectedPolicyNames(policyMap, ImmutableMap.of(
          "solutions.trsoftware.commons.client.bridge.rpc.NumberFormatTestService", "93E70DB6989E525BDF54A0041209CF42",
          "com.google.gwt.junit.client.impl.JUnitHost", "C63F04478389D766205EADF6DDB3A13C",
          "solutions.trsoftware.commons.client.server.MockRpcService", "A4CFBC9ADC2AD09D08DF4CCC34419B72"
      ));
      // a) test the NumberFormatTestService service serialization policy
      getAndValidateSerializationPolicy(policyMap,
          NumberFormatTestService.class.getMethod("generateFloats", int.class),
          DoubleStream.generate(RandomUtils::randDouble).limit(4).mapToObj(Double::toString).toArray(String[]::new)
      );
      // b) test the JUnitHost service serialization policy (using the same dummy object as (1)(a))
      getAndValidateSerializationPolicyForJUnitHost(policyMap);
      // c) test the MockRpcService service serialization policy
      getAndValidateSerializationPolicy(policyMap,
          MockRpcService.class.getMethod("enlargeArea", Area2d.class, int.class, int.class),
          new Area2d(10, 20)
      );
    }

    /*
      TODO: test some error cases, such as:
        - a webapp without any policies
        - a policy file that cannot be read
        - a module that doesn't exist (expect the same outcome as a webapp without any policies)
     */
  }

}