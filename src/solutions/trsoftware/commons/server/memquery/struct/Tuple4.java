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

package solutions.trsoftware.commons.server.memquery.struct;

/**
 * An immutable 4-tuple, with each element individually typed and accessed with a dedicated getter in addition to
 * the {@link #getValue(int)} method.
 *
 * @author Alex, 1/15/14
 */
public class Tuple4<A, B, C, D> extends Tuple3<A,B, C> {

  private final D d;

  public Tuple4(A a, B b, C c, D d) {
    super(a, b, c);
    this.d = d;
  }

  public D getD() {
    return d;
  }

  @Override
  public int size() {
    return 4;
  }

  @Override
  public Object getValue(int index) {
    if (index == 3)
      return d;
    return super.getValue(index);
  }
}
