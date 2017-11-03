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
 * Wraps a {@link PrintStream} to print a prefix on every invocation of
 * {@link PrintStream#println(String)} and {@link PrintStream#printf(String, Object...)}.
 *
 * @author Alex
 */
public class PrefixedPrintStream extends PrintStream {

  private String prefix;
  private PrintStream delegate;

  public PrefixedPrintStream(String prefix, PrintStream delegate) {
    super(delegate);
    this.prefix = prefix;
    this.delegate = delegate;
  }

  public PrintStream getDelegate() {
    return delegate;
  }

  // override methods to add the prefix


  @Override
  public void println(String x) {
    super.println(prefix + x);
  }

  @Override
  public PrintStream printf(String format, Object... args) {
    return super.printf(prefix + format, args);
  }

}
