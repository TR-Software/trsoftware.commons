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

import solutions.trsoftware.commons.shared.util.Resettable;
import solutions.trsoftware.commons.shared.util.iterators.ResettableCachingIterator;

import java.util.Iterator;

/**
 * A {@link StreamingRelation} whose iterator can be reset.
 * @author Alex, 10/15/2016
 */
public class ResettableStreamingRelation extends StreamingRelation implements Resettable {

  public ResettableStreamingRelation(RelationSchema schema, Iterator<Row> rowIterator) {
    super(schema, new ResettableCachingIterator<Row>(rowIterator));
  }

  @Override
  public void reset() {
    ((Resettable)rowIterator).reset();
  }
}
