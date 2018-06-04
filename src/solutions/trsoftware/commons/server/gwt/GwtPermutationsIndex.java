/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.server.gwt;

import solutions.trsoftware.tools.gwt.artifacts.GwtCompilerArtifacts;

import javax.servlet.ServletContext;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alex
 * @since 1/27/2018
 */
public class GwtPermutationsIndex {

  // lazy init
  private Map<String, Set<String>> gwtPermutationsByModulePath = new LinkedHashMap<>();

  /**
   * Scans the {@link ServletContext#getResourcePaths(String) servletContext.getResourcePaths(moduleBasePath)} for
   * {@code *.cache.html} files.
   *
   * @param moduleBasePath the URI from which the module was loaded (given by the
   * {@value com.google.gwt.user.client.rpc.RpcRequestBuilder#MODULE_BASE_HEADER} header of a GWT-RPC request).
   *
   * @return The set of all available permutation strong names present at the given resource path in the given
   * {@link ServletContext}
   */
  public Set<String> getAvailablePermutations(String moduleBasePath, ServletContext servletContext) {
    // lazy-init the available permutation names from the set of (the *.cache.html resources present at the given context path)
    Set<String> permutations = gwtPermutationsByModulePath.get(moduleBasePath);
    if (permutations == null) {
      synchronized (this) {
        // double-checked locking
        permutations = gwtPermutationsByModulePath.get(moduleBasePath);
        if (permutations == null) {
          permutations = new LinkedHashSet<>();
          // load the names of module permutations available on the server
          Pattern pattern = Pattern.compile(".*" + GwtCompilerArtifacts.PERMUTATION_FILENAME_PATTERN.pattern());
          for (String resourcePath : servletContext.getResourcePaths(moduleBasePath)) {
            Matcher matcher = pattern.matcher(resourcePath);
            if (matcher.matches()) {
              permutations.add(matcher.group(1));
            }
          }
          gwtPermutationsByModulePath.put(moduleBasePath, permutations);
        }
      }
    }
    return permutations;
  }
}
