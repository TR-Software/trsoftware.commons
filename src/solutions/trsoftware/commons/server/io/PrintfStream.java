/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.io;

import java.io.PrintStream;

/**
 * Wraps a {@link PrintStream} and exposes some convenience methods for it.
 *
 * @author Alex
 */
public class PrintfStream {

  PrintStream delegate;

  public PrintfStream(PrintStream delegate) {
    this.delegate = delegate;
  }

  public PrintStream getDelegate() {
    return delegate;
  }

  // the convenience methods

  /** Same as {@link PrintStream#printf(String, Object...)}, but additionally prints a newline at the end. */
  public PrintStream println(String format, Object... args) {
    return delegate.printf(format + "%n", args);
  }



}
