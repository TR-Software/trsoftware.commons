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
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.server.rpc.SerializationPolicy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * A multimap-like cache of the {@linkplain SerializationPolicy serialization policies} for all the GWT modules
 * in a webapp.
 * Allows retrieving the policy for any valid {@code (moduleName, serviceInterface)} pair.
 *
 * @author Alex
 * @since 3/3/2023
 */
public class SerializationPolicyCache {

  private static Logger LOGGER = Logger.getLogger(SerializationPolicyCache.class.getName());

  private final ImmutableMap<String, SerializationPolicyMap> policiesByModuleName;

  /**
   * Parses all the {@code (moduleName, serviceInterface)} &rarr; {@code policy} mappings
   * from the GWT compiler artifacts emitted into the
   * <nobr>{@code /WEB-INF/deploy/<moduleName>/rpcPolicyManifest}</nobr> path of the webapp.
   *
   * @param servletContext the webapp context (for locating the resources)
   */
  public SerializationPolicyCache(@Nonnull ServletContext servletContext) {
    this(servletContext, SerializationPolicyMap.DEFAULT_DEPLOY_PATH);
  }

  /**
   * Parses all the {@code (moduleName, serviceInterface)} &rarr; {@code policy} mappings
   * from the GWT compiler artifacts emitted into the given {@code -deploy} path of the webapp.
   *
   * @param servletContext the webapp context (for locating the resources)
   * @param deployPath base path of the {@code -deploy} resources in the webapp
   *   (typically {@value SerializationPolicyMap#DEFAULT_DEPLOY_PATH})
   */
  public SerializationPolicyCache(@Nonnull ServletContext servletContext, @Nonnull String deployPath) {
    Set<String> paths = servletContext.getResourcePaths(deployPath);
    ImmutableMap.Builder<String, SerializationPolicyMap> mapBuilder = ImmutableMap.builder();
    for (String path : paths) {
      if (path.endsWith("/")) {
        // potential module dir
        Set<String> subPaths = servletContext.getResourcePaths(path);
        if (subPaths != null && subPaths.contains(path + "rpcPolicyManifest/")) {
          String moduleName = path.substring(deployPath.length()).replaceAll("/", "");
          try {
            SerializationPolicyMap policyMap = new SerializationPolicyMap(moduleName, servletContext, deployPath);
            mapBuilder.put(moduleName, policyMap);
          }
          catch (PolicyNotFoundException e) {
            LOGGER.log(Level.WARNING,
                /*e,*/  // not logging the stack trace for now
                () -> format("Unable to load serialization policies for module '%s' from %s", moduleName, path));
          }
        }
      }
    }
    policiesByModuleName = mapBuilder.build();
  }

  /**
   * @return the names of all modules that have serialization policies
   */
  public Set<String> getModuleNames() {
    return policiesByModuleName.keySet();
  }

  @Nullable
  public SerializationPolicyMap getPolicyMap(String moduleName) {
    return policiesByModuleName.get(moduleName);
  }

  public Map<String, SerializationPolicyMap> getPoliciesByModuleName() {
    return policiesByModuleName;
  }

  public SerializationPolicy getSerializationPolicy(String moduleName, Class<? extends RemoteService> serviceClass) throws PolicyNotFoundException {
    return getSerializationPolicy(moduleName, serviceClass.getName());
  }

  /**
   * Loads the serialization policy for the given RPC service interface in the given GWT module, if found.
   * The returned object will be cached and subsequent invocations will return the same instance.
   *
   * @param serviceClassName the fully-qualified class name of a {@link RemoteService} interface
   * @throws PolicyNotFoundException if unable to find or parse the policy file for the given service
   */
  public SerializationPolicy getSerializationPolicy(String moduleName, String serviceClassName) throws PolicyNotFoundException {
    SerializationPolicyMap policyMap = getPolicyMap(moduleName);
    if (policyMap != null) {
      return policyMap.getSerializationPolicy(serviceClassName);
    }
    throw new PolicyNotFoundException(format("Module '%s' does not exist or does not contain a serialization policy for service %s",
        moduleName, serviceClassName));
  }

  /**
   * @return a {@code moduleName} &rarr; {@link SerializationPolicy} mapping for the policies matching the given service;
   * or empty map if none of the modules contain a serialization policy for the given service
   */
  public Map<String, SerializationPolicy> getPoliciesForService(Class<? extends RemoteService> serviceClass) {
    return getPoliciesForService(serviceClass.getName());
  }

  /**
   * @param serviceClassName name of a {@link RemoteService} interface
   * @return a {@code moduleName} &rarr; {@link SerializationPolicy} mapping for the policies matching the given service;
   * or empty map if none of the modules contain a serialization policy for the given service
   */
  public Map<String, SerializationPolicy> getPoliciesForService(String serviceClassName) {
    ImmutableMap.Builder<String, SerializationPolicy> mapBuilder = ImmutableMap.builder();
    for (String moduleName : getModuleNames()) {
      try {
        SerializationPolicy policy = getSerializationPolicy(moduleName, serviceClassName);
        mapBuilder.put(moduleName, policy);
      }
      catch (PolicyNotFoundException e) {
        // ignore
      }
    }
    return mapBuilder.build();
  }

}
