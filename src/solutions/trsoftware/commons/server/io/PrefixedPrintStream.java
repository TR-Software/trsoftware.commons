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

import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
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
  /**
   * Number of space chars to print after the {@link #prefix}
   */
  private int indent;

  /**
   * @param prefix will be prepended to every invocation of {@link #println(String)} and {@link #printf(String, Object...)}
   * <br><strong>WARNING: </strong> if this string contains any {@code printf} formatting sequences (e.g. {@code "%s"}),
   * all calls to {@link #printf(String, Object...)} must include the corresponding values.
   */
  public PrefixedPrintStream(String prefix, PrintStream delegate) {
    super(delegate);
    this.prefix = prefix;
    this.delegate = delegate;
  }

  public PrintStream getDelegate() {
    return delegate;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public int getIndent() {
    return indent;
  }

  /**
   * @param indent the number of space chars to print after the {@link #prefix}
   */
  public void setIndent(int indent) {
    this.indent = indent;
  }

  @Nonnull
  private String prependPrefix(String s) {
    return prefix + StringUtils.indent(indent, s);
  }

  // override methods to add the prefix


  @Override
  public void println(String x) {
    super.println(prependPrefix(x));
  }

  @Override
  public PrintStream printf(String format, Object... args) {
    return super.printf(prependPrefix(format), args);
  }

}
