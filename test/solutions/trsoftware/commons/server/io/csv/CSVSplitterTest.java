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

package solutions.trsoftware.commons.server.io.csv;

import junit.framework.TestCase;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Iterator;

/**
 * Mar 12, 2011
 *
 * @author Alex
 */
public class CSVSplitterTest extends TestCase {

  private static class CSVSplitterWithCustomLineSeparator extends CSVSplitter {
    private String lineSeparator = System.getProperty("line.separator");

    public CSVSplitterWithCustomLineSeparator(CSVReader inputReader, int chunkSizeLines) {
      super(inputReader, chunkSizeLines);
    }

    private CSVSplitterWithCustomLineSeparator(Reader inputReader, int chunkSizeLines) {
      super(inputReader, chunkSizeLines);
    }

    @Override
    protected CSVWriter newCSVWriter(Writer destination) {
      return new CSVWriter(destination, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER, lineSeparator);
    }

    public String getLineSeparator() {
      return lineSeparator;
    }

    public void setLineSeparator(String lineSeparator) {
      this.lineSeparator = lineSeparator;
    }
  }

  private CSVSplitterWithCustomLineSeparator newSplitter(final String input, final int chunkSizeLines, String lineSeparator) {
    CSVSplitterWithCustomLineSeparator ret = new CSVSplitterWithCustomLineSeparator(new StringReader(input), chunkSizeLines);
    ret.setLineSeparator(lineSeparator);
    return ret;
  }

  public void testBasicSplitting() throws Exception {
    {
      // test split size of 1 line
      Iterator<String> splitter = newSplitter("1,2,\"a b\"\n2,3,\"a c\"\n3,4,5\n", 1, "\n");
      assertTrue(splitter.hasNext());
      assertEquals("1,2,a b\n", splitter.next());  // the original quoting will not be preserved
      assertTrue(splitter.hasNext());
      assertEquals("2,3,a c\n", splitter.next());
      assertTrue(splitter.hasNext());
      assertEquals("3,4,5\n", splitter.next());
      assertFalse(splitter.hasNext());
    }
    {
      // test split size of 2 lines
      Iterator<String> splitter = newSplitter("1,2,\"a b\"\n2,3,\"a c\"\n3,4,5\n", 2, "\n");
      assertTrue(splitter.hasNext());
      assertEquals("1,2,a b\n2,3,a c\n", splitter.next());  // the original quoting will not be preserved
      assertTrue(splitter.hasNext());
      assertEquals("3,4,5\n", splitter.next());
      assertFalse(splitter.hasNext());
    }
    {
      // test split size of 3 or more lines
      for (int splitSize = 3; splitSize < 100; splitSize++) {
        Iterator<String> splitter = newSplitter("1,2,\"a b\"\n2,3,\"a c\"\n3,4,5\n", splitSize, "\n");
        assertTrue(splitter.hasNext());
        assertEquals("1,2,a b\n2,3,a c\n3,4,5\n", splitter.next());  // the original quoting will not be preserved
        assertFalse(splitter.hasNext());
      }
    }
  }

  public void testSplittingWithNestedNewlines() throws Exception {
    {
      // test split size of 1 line
      Iterator<String> splitter = newSplitter("1,2,\"a b\"\n2,3,\"a\nc\"\n3,4,5\n", 1, "\n");
      assertTrue(splitter.hasNext());
      assertEquals("1,2,a b\n", splitter.next());  // the original quoting will not be preserved
      assertTrue(splitter.hasNext());
      assertEquals("2,3,\"a\nc\"\n", splitter.next());  // the quoting around "a\nc" will be preserverd because of the nested newline
      assertTrue(splitter.hasNext());
      assertEquals("3,4,5\n", splitter.next());
      assertFalse(splitter.hasNext());
    }
    {
      // test split size of 2 lines
      Iterator<String> splitter = newSplitter("1,2,\"a b\"\n2,3,\"a\nc\"\n3,4,5\n", 2, "\n");
      assertTrue(splitter.hasNext());
      assertEquals("1,2,a b\n2,3,\"a\nc\"\n", splitter.next());  // the original quoting around "a b" will not be preserved, but the quoting around "a\nc" will be preserverd because of the nested newline
      assertTrue(splitter.hasNext());
      assertEquals("3,4,5\n", splitter.next());
      assertFalse(splitter.hasNext());
    }
    {
      // test split size of 3 or more lines
      for (int splitSize = 3; splitSize < 100; splitSize++) {
        Iterator<String> splitter = newSplitter("1,2,\"a b\"\n2,3,\"a\nc\"\n3,4,5\n", splitSize, "\n");
        assertTrue(splitter.hasNext());
        assertEquals("1,2,a b\n2,3,\"a\nc\"\n3,4,5\n", splitter.next());  // the original quoting around "a b" will not be preserved, but the quoting around "a\nc" will be preserverd because of the nested newline
        assertFalse(splitter.hasNext());
      }
    }
  }

  public void testSplittingWhileIgnoringEmptyLines() throws Exception {
    {
      // test split size of 1 line
      Iterator<String> splitter = newSplitter("\n1,2,\"a b\"\n2,3,\"a\nc\"\n3,4,5\n\n", 1, "\n");
      assertTrue(splitter.hasNext());
      // the first \n should be ignored
      assertEquals("1,2,a b\n", splitter.next());  // the original quoting will not be preserved
      assertTrue(splitter.hasNext());
      assertEquals("2,3,\"a\nc\"\n", splitter.next());  // the quoting around "a\nc" will be preserverd because of the nested newline
      assertTrue(splitter.hasNext());
      assertEquals("3,4,5\n", splitter.next());
      assertFalse(splitter.hasNext()); // the last \n should be ignored
    }
    {
      // test split size of 2 lines
      Iterator<String> splitter = newSplitter("\n1,2,\"a b\"\n2,3,\"a\nc\"\n3,4,5\n\n", 2, "\n");
      assertTrue(splitter.hasNext());
      assertEquals("1,2,a b\n2,3,\"a\nc\"\n", splitter.next());  // the original quoting around "a b" will not be preserved, but the quoting around "a\nc" will be preserverd because of the nested newline
      assertTrue(splitter.hasNext());
      assertEquals("3,4,5\n", splitter.next());
      assertFalse(splitter.hasNext());
    }
    {
      // test split size of 3 or more lines
      for (int splitSize = 3; splitSize < 100; splitSize++) {
        Iterator<String> splitter = newSplitter("\n1,2,\"a b\"\n2,3,\"a\nc\"\n3,4,5\n\n", splitSize, "\n");
        assertTrue(splitter.hasNext());
        assertEquals("1,2,a b\n2,3,\"a\nc\"\n3,4,5\n", splitter.next());  // the original quoting around "a b" will not be preserved, but the quoting around "a\nc" will be preserverd because of the nested newline
        assertFalse(splitter.hasNext());
      }
    }
  }
}