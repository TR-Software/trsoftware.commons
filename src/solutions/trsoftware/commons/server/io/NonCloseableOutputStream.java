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

package solutions.trsoftware.commons.server.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Wraps an underlying {@link OutputStream} to prevent closing it.
 * <p>
 * This is useful in situations where one might want to wrap streams that should never be closed,
 * such as {@link System#out} or {@link System#err}.
 *
 * @author Alex
 * @since 7/27/2019
 */
public class NonCloseableOutputStream extends FilterOutputStream {

  /**
   * @param out the underlying output stream that should never be closed.
   */
  public NonCloseableOutputStream(OutputStream out) {
    super(out);
  }

  /**
   * Flushes the wrapped stream but does not close it.
   */
  @Override
  public void close() throws IOException {
    flush();
  }

}
