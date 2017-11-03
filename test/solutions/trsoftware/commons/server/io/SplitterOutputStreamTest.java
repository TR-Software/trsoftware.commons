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

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import junit.framework.TestCase;

import java.io.PrintStream;

/**
 * Jan 7, 2009
 *
 * @author Alex
 */
public class SplitterOutputStreamTest extends TestCase {


  public void testWriteToOneStream() throws Exception {
    ByteOutputStream stream1 = new ByteOutputStream();

    SplitterOutputStream splitter = new SplitterOutputStream(stream1);
    PrintStream ps = new PrintStream(splitter);
    ps.println("Testing");

    // make sure the data is passed to the underlying stream
    assertEquals("Testing" + System.getProperty("line.separator"), stream1.toString());
  }

  public void testWriteToMultipleStreams() throws Exception {
    ByteOutputStream stream1 = new ByteOutputStream();
    ByteOutputStream stream2 = new ByteOutputStream();

    SplitterOutputStream splitter = new SplitterOutputStream(stream1, stream2);
    PrintStream ps = new PrintStream(splitter);
    ps.println("Testing");

    // make sure the data is passed to the underlying stream
    assertEquals("Testing" + System.getProperty("line.separator"), stream1.toString());
    assertEquals("Testing" + System.getProperty("line.separator"), stream2.toString());
    assertNotSame(stream1.toString(), stream2.toString());
  }
}