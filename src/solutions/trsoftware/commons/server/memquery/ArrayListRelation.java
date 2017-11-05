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

package solutions.trsoftware.commons.server.memquery;

import java.util.ArrayList;
import java.util.Iterator;

import static solutions.trsoftware.commons.shared.util.CollectionUtils.asList;

/**
 * A relation that is materialized as an ArrayList of rows.
 *
 * @author Alex, 1/15/14
 */
public class ArrayListRelation extends AbstractRelation implements MaterializedRelation {

  protected final ArrayList<Row> rows;

  protected ArrayListRelation(RelationSchema schema, ArrayList<Row> rows) {
    super(schema);
    rows.trimToSize();
    this.rows = rows;
  }

  public ArrayListRelation(RelationSchema schema, Iterator<Row> rowIter) {
    this(schema, asList(rowIter));
  }

  public ArrayListRelation(Relation relation) {
    this(relation.getSchema(), relation.iterator());
  }

  @Override
  public Iterator<Row> iterator() {
    return rows.iterator();
  }

  @Override
  public ArrayList<Row> getRows() {
    return rows;
  }

  @Override
  public int size() {
    return rows.size();
  }
}
