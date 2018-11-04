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

package solutions.trsoftware.commons.server.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} that doesn't do anything with the written bytes - it simply skips over them.
 * This is useful in situations where an input stream must be read but we don't care about the individual bytes,
 * which might be the case when using a {@link java.io.FilterInputStream} to process data.
 * Example: computing checksums of files using {@link java.security.DigestInputStream}.
 *
 * @author Alex
 * @since 11/11/2017
 */
public class NullOutputStream extends OutputStream {

  @Override
  public void write(int b) throws IOException {
    // intentionally empty
  }
}
