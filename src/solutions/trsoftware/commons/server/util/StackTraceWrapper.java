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

package solutions.trsoftware.commons.server.util;

/**
 * A marker class that can be used for obtaining or printing a stack trace without
 * implying that any actual exception or error has occurred.
 * <p>
 * Should be instantiated at the call site whose stack trace is desired, and can be passed around the application,
 * but <em>is not intended to actually be thrown</em> (this class extends {@link Throwable} only by necessity,
 * simply to allow obtaining obtaining a stack trace)
 *
 * <h3>Examples:</h3>
 * <pre>
 *   new StackTraceWrapper(message).printStackTrace(System.out);
 *   new StackTraceWrapper(message).getStackTrace();
 * </pre>
 *
 */
public class StackTraceWrapper extends Throwable {

  // TODO: use this in RuntimeUtils instead of Exception

  public StackTraceWrapper() { }

  public StackTraceWrapper(String message) {
    super(message);
  }

  public StackTraceWrapper(String message, Throwable cause) {
    super(message, cause);
  }

  public StackTraceWrapper(Throwable cause) {
    super(cause);
  }
}
