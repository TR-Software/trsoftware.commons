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

package solutions.trsoftware.commons.server.memquery;

import java.util.Iterator;

/**
 * A relation that is not fully stored in memory.  Instead, the rows are streamed using the provided
 * {@link #iterator()} method, which always returns the same iterator object for the stream of rows.
 * This is a good alternative to {@link ArrayListRelation} for representing the results of an operation
 * that can be pipelined.
 *
 * @author Alex, 1/15/14
 */
public class StreamingRelation extends AbstractRelation {

  protected final Iterator<Row> rowIterator;

  public StreamingRelation(RelationSchema schema, Iterator<Row> rowIterator) {
    super(schema);
    this.rowIterator = rowIterator;
  }

  @Override
  public Iterator<Row> iterator() {
    return rowIterator;
  }
}
