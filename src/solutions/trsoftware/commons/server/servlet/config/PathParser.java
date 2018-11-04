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

package solutions.trsoftware.commons.server.servlet.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Instantiates a {@link Path} object from a {@link String} using {@link java.nio.file.Paths#get(String, String...)}
 *
 * @author Alex
 * @since 5/22/2018
 */
public class PathParser implements InitParameters.ParameterParser<Path> {

  @Override
  public Path parse(String pathStr) throws Exception {
    return Paths.get(pathStr);
  }
}
