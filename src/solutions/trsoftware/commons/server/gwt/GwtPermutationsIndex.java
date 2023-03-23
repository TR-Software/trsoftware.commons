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

package solutions.trsoftware.commons.server.gwt;

import com.google.common.collect.ImmutableSet;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import solutions.trsoftware.tools.gwt.artifacts.GwtCompilerArtifacts;

import javax.servlet.ServletContext;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * Indexes the compiled permutations for a webapp's GWT modules by scanning the module's resource path in the webapp
 * (i.e. the files emitted into the {@code -war} directory by the GWT compiler).
 *
 * @author Alex
 * @since 1/27/2018
 */
public class GwtPermutationsIndex {

  /**
   * Cache of {@link #getAvailablePermutations(String, ServletContext)} results.
   */
  private final Map<String, ImmutableSet<String>> gwtPermutationsByModulePath = new LinkedHashMap<>();

  /**
   * Derives the set of all available permutations for a module based on the
   * {@link GwtCompilerArtifacts#PERMUTATION_FILENAME_PATTERN .cache.(js|html)} files contained in the module's base
   * path ({@link ServletContext#getResourcePaths(String) servletContext.getResourcePaths(moduleBasePath)}.
   * <p>
   * This assumes that each module's files are in a separate directory.
   *
   * @param moduleBasePath the URI from which the module was loaded (given by the
   * {@value RpcRequestBuilder#MODULE_BASE_HEADER} header of a GWT-RPC request and {@link GWT#getModuleBaseURL()}).
   *
   * @return The set of all available permutation strong names present at the given resource path in the given
   * {@link ServletContext}
   */
  public ImmutableSet<String> getAvailablePermutations(String moduleBasePath, ServletContext servletContext) {
    // lazy-init the available permutation names from the set of (the *.cache.html resources present at the given context path)
    ImmutableSet<String> permutations = gwtPermutationsByModulePath.get(moduleBasePath);
    if (permutations == null) {
      synchronized (this) {
        // double-checked locking
        permutations = gwtPermutationsByModulePath.get(moduleBasePath);
        if (permutations == null) {
          // load the names of module permutations available on the server
          Set<String> paths = servletContext.getResourcePaths(moduleBasePath);
          if (paths == null) {
            throw new IllegalArgumentException(
                format("Invalid moduleBasePath ('%s' not found in '%s' webapp deployed from %s)",
                    moduleBasePath, servletContext.getContextPath(), servletContext.getRealPath("/")));
          }
          ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
          Pattern pattern = Pattern.compile(".*" + GwtCompilerArtifacts.PERMUTATION_FILENAME_PATTERN.pattern());
          for (String resourcePath : paths) {
            Matcher matcher = pattern.matcher(resourcePath);
            if (matcher.matches()) {
              setBuilder.add(matcher.group(1));
            }
          }
          gwtPermutationsByModulePath.put(moduleBasePath, permutations = setBuilder.build());
        }
      }
    }
    return permutations;
  }
}
