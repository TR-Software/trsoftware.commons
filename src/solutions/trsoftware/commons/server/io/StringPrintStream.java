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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * A print stream (like {@link System#out}) that writes to a string buffer.
 *
 * @author Alex
 */
public class StringPrintStream extends PrintStream {

  public StringPrintStream() {
    super(new ByteArrayOutputStream());
  }

  public StringPrintStream(int bufferSize) {
    super(new ByteArrayOutputStream(bufferSize));
  }

  public StringPrintStream(int bufferSize, boolean autoFlush) {
    super(new ByteArrayOutputStream(bufferSize), autoFlush);
  }

  public StringPrintStream(int bufferSize,  boolean autoFlush, String encoding) throws UnsupportedEncodingException {
    super(new ByteArrayOutputStream(bufferSize), autoFlush, encoding);
  }

  @Override
  public String toString() {
    return getBuffer().toString();
  }

  public int size() {
    return getBuffer().size();
  }

  private ByteArrayOutputStream getBuffer() {
    return (ByteArrayOutputStream)out;
  }
}
