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

import java.io.IOException;
import java.io.Writer;


/**
 * Removes occurrences of the given string from the character stream being written.
 *
 * Note: This class does not extend java.io.FilterWriter because it has
 * too many methods to override.
 *
 * Oct 23, 2009
 *
 * @author Alex
 */
public class StringFilteringWriter extends Writer {

  private boolean inMatchingRun = false;
  /** We sequester the chars that match one of the filters until it's safe to approve or reject them */
  private StringBuilder matchingRun = new StringBuilder();

  private final Writer delegate;
  private final String filter;

  public StringFilteringWriter(Writer delegate, String filter) {
    this.delegate = delegate;
    this.filter = filter;
  }


  /** Implements the filtering logic */
  @Override
  public void write(char buf[], int off, int len) throws IOException {
    // process the chars one by one
    int limit = len + off;
    for (int i = off; i < limit; i++) {
      char c = buf[i];
      // see if this char matches the next character of the string being filtered
      if (c == filter.charAt(matchingRun.length())) {
        // we've either entered a new matching region or still in one
        if (!inMatchingRun) {
          // we've entered a new run
          inMatchingRun = true;
        }
        matchingRun.append(c);
      }
      else {
        // we've left the matching run, which wasn't matched completely, so whatever
        // is in it has the green light to be written now
        if (inMatchingRun) {
          closeOutUnfinishedMatch();
        }
        delegate.write(c);  // can proceed with writing this character (NOTE: do not replace this with a call to write(buf, i, 1) - that's actually slower!
      }
      if (matchingRun.length() == filter.length()) {
        // we've matched the entire run up to this point; close it out
        resetMatchState();
      }
    }
  }

  private void closeOutUnfinishedMatch() throws IOException {
    delegate.write(matchingRun.toString());
    resetMatchState();
  }

  private void resetMatchState() {
    // reset the match state
    matchingRun.delete(0, matchingRun.length());
    inMatchingRun = false;
  }

  @Override
  public void flush() throws IOException {
    if (inMatchingRun)
      closeOutUnfinishedMatch();
    delegate.flush();
  }

  /**
   * Closes the stream, flushing it first. Once the stream has been closed,
   * further write() or flush() invocations will cause an IOException to be
   * thrown. Closing a previously closed stream has no effect.
   *
   * @throws IOException If an I/O error occurs
   */
  @Override
  public void close() throws IOException {
    flush();  // see if we've got any chars waiting to get written
    delegate.close();
  }
}