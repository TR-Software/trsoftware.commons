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

package solutions.trsoftware.commons.shared.util;

/**
 * Date: Nov 3, 2008 Time: 12:10:57 PM
 *
 * @author Alex
 */
public class ImmutablePair<K, V> extends Pair<K, V> {
  public ImmutablePair(K first, V second) {
    super(first, second);
  }

  private ImmutablePair() {
  }

  @Override
  public V setValue(V value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setFirst(K first) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setSecond(V second) {
    throw new UnsupportedOperationException();
  }
}
