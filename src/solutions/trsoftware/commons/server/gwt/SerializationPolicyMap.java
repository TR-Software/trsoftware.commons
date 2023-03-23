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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import solutions.trsoftware.commons.server.io.ServerIOUtils;
import solutions.trsoftware.commons.shared.util.MapUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Parses the {@code serviceName} &rarr; {@code policyStrongName} mappings for a particular GWT module from
 * the compiler resources emitted into the {@link #DEFAULT_DEPLOY_PATH -deploy} directory in the webapp, and loads
 * the corresponding {@link SerializationPolicy} objects as needed.
 *
 * @see #getSerializationPolicy(String)
 * @see #getSerializationPolicy(Class)
 * @author Alex
 * @since 3/3/2023
 */
public class SerializationPolicyMap {

  public static final String DEFAULT_DEPLOY_PATH = "/WEB-INF/deploy";

  private final String moduleName;
  private final ServletContext servletContext;
  private final String deployPath;

  private final ImmutableMap<String, String> serviceNameToPolicyStrongName;

  private final ConcurrentHashMap<String, SerializationPolicy> serviceNameToPolicy;

  /* TODO:
       - is moduleName always the same as GWT.getModuleBaseURL() (passed in the "X-GWT-Module-Base" header or RPC requests)
         - maybe change the moduleName parameter to moduleBasePath?
           (see GwtPermutationsIndex.getAvailablePermutations(moduleBasePath, servletContext))
       - do we have to worry about a potential difference between GWT.getModuleBaseURL() and GWT.getModuleBaseForStaticFiles()?
         ("Normally this will be the same value as {@link #getModuleBaseURL}, but
           may be different when a GWT app is configured to get its static resources from a different server.")
         - very unlikely; just document the potential discrepancy
       - maybe create a (lazy-inited) static flyweight of instances for each module (for symmetry with GwtPermutationsIndex)
   */

  /**
   * Parses the {@code serviceName} &rarr; {@code policyStrongName} mappings from the GWT compiler artifacts emitted
   * into the <nobr>{@code /WEB-INF/deploy/<moduleName>/rpcPolicyManifest}</nobr> path of the webapp.
   *
   * @param moduleName the GWT module name
   * @param servletContext the webapp context (for locating the resources)
   * @throws PolicyNotFoundException if unable to find any RPC policy mappings
   */
  public SerializationPolicyMap(@Nonnull String moduleName, @Nonnull ServletContext servletContext) throws PolicyNotFoundException {
    this(moduleName, servletContext, DEFAULT_DEPLOY_PATH);
  }

  /**
   * Extracts the {@code serviceName} &rarr; {@code policyStrongName} mappings from the GWT compiler artifacts emitted
   * into the <nobr>{@code /<deployPath>/<moduleName>/rpcPolicyManifest}</nobr> path of the webapp.
   *
   * @param moduleName the GWT module name
   * @param servletContext the webapp context (for locating the resources)
   * @param deployPath base path of the {@code -deploy} resources in the webapp (typically {@value #DEFAULT_DEPLOY_PATH})
   * @throws PolicyNotFoundException if unable to find any RPC policy mappings
   */
  public SerializationPolicyMap(@Nonnull String moduleName, @Nonnull ServletContext servletContext, @Nonnull String deployPath) throws PolicyNotFoundException {
    this.moduleName = requireNonNull(moduleName, "moduleName");
    this.servletContext = requireNonNull(servletContext, "servletContext");
    this.deployPath = requireNonNull(deployPath, "deployPath");
    String manifestPath = StringUtils.join("/", deployPath, moduleName, "rpcPolicyManifest");
    // try parsing the policy mappings from manifest.txt, if possible, otherwise fall back to scanning the manifests subdir
    Optional<ImmutableMap<String, String>> policyMapping = readManifestTxt(servletContext, manifestPath);
    if (!policyMapping.isPresent()) {
      // scan the manifests subdir if unable to get anything from manifest.txt
      policyMapping = readManifestsFromSubdir(servletContext, manifestPath);
    }
    serviceNameToPolicyStrongName = policyMapping.orElseThrow(() ->
        new PolicyNotFoundException(format("Unable to locate any serialization policies in %s/%s", deployPath, moduleName)));
    serviceNameToPolicy = new ConcurrentHashMap<>();
  }

  /**
   * @return name of the GWT module corresponding to this policy map
   */
  public String getModuleName() {
    return moduleName;
  }

  /**
   * Loads the serialization policy for the given RPC service interface, if found.
   * The returned object will be cached and subsequent invocations will return the same instance.
   *
   * @param serviceClassName the fully-qualified class name of a {@link RemoteService} interface
   * @throws PolicyNotFoundException if unable to find or parse the policy file for the given service
   */
  public SerializationPolicy getSerializationPolicy(@Nonnull String serviceClassName) throws PolicyNotFoundException {
    return MapUtils.computeIfAbsent(serviceNameToPolicy, serviceClassName, this::loadSerializationPolicyForService);
  }

  /**
   * Loads the serialization policy for the given RPC service interface, if found.
   * The returned object will be cached and subsequent invocations will return the same instance.
   *
   * @param serviceClass an interface that implements {@link RemoteService}
   * @throws PolicyNotFoundException if unable to find or parse the policy file for the given service
   */
  public SerializationPolicy getSerializationPolicy(Class<? extends RemoteService> serviceClass) throws PolicyNotFoundException {
    return getSerializationPolicy(serviceClass.getName());
  }

  /**
   * Returns the serialization policy strong name for the given RPC service interface in this module.
   * The corresponding policy file would be {@code <moduleBaseURL>/<policyStrongName>.gwt.rpc}
   *
   * @param serviceClassName the fully-qualified class name of a {@link RemoteService} interface
   * @return the policy strong name or {@code null} if the GWT module doesn't have a serialization policy associated
   * with the given service interface
   */
  @Nullable
  public String getPolicyStrongName(String serviceClassName) {
    return serviceNameToPolicyStrongName.get(serviceClassName);
  }

  /**
   * @return the names of all service interface classes that have a serialization policy in this module
   */
  public Set<String> getServiceClassNames() {
    return serviceNameToPolicyStrongName.keySet();
  }

  private SerializationPolicy loadSerializationPolicyForService(String serviceClassName) throws PolicyNotFoundException {
    String policyStrongName = getPolicyStrongName(serviceClassName);
    if (policyStrongName != null) {
      return loadSerializationPolicy(policyStrongName);
    }
    throw new PolicyNotFoundException(format("Missing serialization policy manifest for service %s in module %s"
        + "; did you forget to include it in %s?",
        serviceClassName, moduleName, deployPath));
  }

  private SerializationPolicy loadSerializationPolicy(@Nonnull String policyStrongName) throws PolicyNotFoundException {
    // NOTE: this code is based on from com.google.gwt.user.server.rpc.RemoteServiceServlet.loadSerializationPolicy
    String policyFileName = SerializationPolicyLoader.getSerializationPolicyFileName(policyStrongName);
    String policyFilePath = "/" + moduleName + "/" + policyFileName;

    SerializationPolicy serializationPolicy = null;
    try (InputStream is = servletContext.getResourceAsStream(policyFilePath)) {
      if (is != null) {
        try {
          serializationPolicy = SerializationPolicyLoader.loadFromStream(is,
              null);
        } catch (ParseException e) {
          logErrorAndThrow(format("Failed to parse the policy file '%s'", policyFilePath), e);
        }
      }
      else {
        logErrorAndThrow(format("The serialization policy file '%s' was not found; did you forget to include it in this deployment?", policyFilePath), null);
      }
    }
    catch (IOException e) {
      logErrorAndThrow(format("Could not read the policy file '%s'", policyFilePath), e);
    }
    assert serializationPolicy != null;  // should've already thrown an exception
    return serializationPolicy;
  }

  private void logErrorAndThrow(String errorMsg, Throwable cause) throws PolicyNotFoundException {
    servletContext.log(errorMsg, cause);
    throw new PolicyNotFoundException(errorMsg, cause);
  }

  /**
   * Parses the {@code serviceName} &rarr; {@code policyStrongName} mappings from
   * {@code /WEB-INF/deploy/<moduleName>/rpcPolicyManifest/manifest.txt}.
   * <p>
   * <strong>Note:</strong> it's possible that {@code manifest.txt} could contain only comments, in which case the policy
   * mappings have to be extracted from the files in the {@code rpcPolicyManifest/manifests} subdirectory
   * (see {@link #readManifestsFromSubdir(ServletContext, String)}).
   *
   * @param manifestPath the path where {@code manifest.txt} is located in the webapp context
   *   (e.g. {@code /WEB-INF/deploy/<moduleName>/rpcPolicyManifest})
   *
   * @return the {@code serviceName} &rarr; {@code policyStrongName} map, if {@code manifest.txt} contained any such mappings
   */
  public static Optional<ImmutableMap<String, String>> readManifestTxt(@Nonnull ServletContext context, @Nonnull String manifestPath) {
    if (!manifestPath.endsWith("/"))
      manifestPath += "/";
    String manifestResource = manifestPath + "manifest.txt";
    ImmutableMap.Builder<String, String> serviceNameToPolicyStrongName = ImmutableMap.builder();
    boolean success = false;
    try (InputStream inputStream = context.getResourceAsStream(manifestResource)) {
      if (inputStream != null) {
        BufferedReader br = new BufferedReader(ServerIOUtils.readUTF8(inputStream));
        Pattern pattern = Pattern.compile("(.*), ((.*)\\.gwt\\.rpc)");
        for (@Nullable String line = br.readLine(); line != null; line = br.readLine()) {
          line = line.trim();
          if (line.isEmpty() || line.startsWith("#"))
            continue;
          success |= parseServiceMapping(line, pattern, serviceNameToPolicyStrongName);
        }
      }
    }
    catch (IOException e) {
      context.log("Error reading " + manifestResource, e);
    }
    return success ? Optional.of(serviceNameToPolicyStrongName.build()) : Optional.empty();
  }

  /**
   * Parses the {@code serviceName} &rarr; {@code policyStrongName} mappings from the descriptor files in
   * {@code /WEB-INF/deploy/<moduleName>/rpcPolicyManifest/manifests} directory.
   * <p>
   * The {@code manifests} directory should contain a separate text file for each serialization policy in the module.
   * These files (whose name are irrelevant) consist of 2 lines of text giving the service interface name and
   * the policy file name.  For example:
   * <pre>
   *   serviceClass: solutions.trsoftware.commons.client.bridge.rpc.NumberFormatTestService
   *   path: 93E70DB6989E525BDF54A0041209CF42.gwt.rpc
   * </pre>
   * The above example file indicates that the serialization policy for {@code NumberFormatTestService}
   * is in {@code <moduleBaseURL>/93E70DB6989E525BDF54A0041209CF42.gwt.rpc} (where {@code "93E70DB6989E525BDF54A0041209CF42"}
   * is the policy strong name).
   *
   * @param manifestPath the path where {@code manifest.txt} is located in the webapp context
   *   (e.g. {@code /WEB-INF/deploy/<moduleName>/rpcPolicyManifest})
   *
   * @return the {@code serviceName} &rarr; {@code policyStrongName} map, if able to derive it from the contents
   * of the {@code rpcPolicyManifest/manifests} directory
   */
  public static Optional<ImmutableMap<String, String>> readManifestsFromSubdir(@Nonnull ServletContext servletContext, @Nonnull String manifestPath) {
    boolean success = false;
    Set<String> manifests = servletContext.getResourcePaths(manifestPath + "/manifests");
    ImmutableMap.Builder<String, String> serviceNameToPolicyStrongName = null;
    if (manifests != null) {
      serviceNameToPolicyStrongName = ImmutableMap.builder();
      Pattern pattern = Pattern.compile("serviceClass: (\\S*)\\s*path: ((\\S*)\\.gwt\\.rpc).*", Pattern.DOTALL);
      for (String manifest : manifests) {
        try (InputStream in = servletContext.getResourceAsStream(manifest)) {
          if (in != null) {
            String manifestText = ServerIOUtils.readCharactersIntoString(in);
            success |= parseServiceMapping(manifestText, pattern, serviceNameToPolicyStrongName);
          }
        }
        catch (IOException e) {
          servletContext.log("Error reading " + manifest, e);
        }
      }
    }
    return success ? Optional.of(serviceNameToPolicyStrongName.build()) : Optional.empty();
  }

  /**
   * Attempts to extract a serviceClass &rarr; policy mapping from the given string.
   *
   * @param text the string to match
   * @param pattern the pattern for extracting the serviceClass name (group 0), and policy strong name (group 3)
   * @param mapBuilder the mapping will be added to this builder
   * @return {@code true} iff mapping was found and added to the map builder
   */
  private static boolean parseServiceMapping(String text, Pattern pattern, ImmutableMap.Builder<String, String> mapBuilder) {
    Matcher matcher = pattern.matcher(text);
    if (matcher.matches()) {
      String servletClassName = matcher.group(1);
      String policyFileName = matcher.group(2);
      String policyStrongName = matcher.group(3);
      mapBuilder.put(servletClassName, policyStrongName);
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("moduleName", moduleName)
//        .add("policyMap", serviceNameToPolicyStrongName)
        .addValue(serviceNameToPolicyStrongName)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    SerializationPolicyMap policyMap = (SerializationPolicyMap)o;

    if (!moduleName.equals(policyMap.moduleName))
      return false;
    return serviceNameToPolicyStrongName.equals(policyMap.serviceNameToPolicyStrongName);
  }

  @Override
  public int hashCode() {
    int result = moduleName.hashCode();
    result = 31 * result + serviceNameToPolicyStrongName.hashCode();
    return result;
  }
}
