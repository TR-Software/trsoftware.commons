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

package solutions.trsoftware.commons.client.dom;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;

import javax.annotation.Nonnull;
import java.util.AbstractList;
import java.util.List;
import java.util.Objects;

/**
 * Wraps a {@link NodeList} to make it compatible with the Java Collections Framework.
 * <p>
 * In some cases, the {@link NodeList} is live, which means that changes in the DOM automatically update
 * the collection.  This wrapper class preserves that behavior (whereas simply copying the {@link NodeList}'s
 * elements to a new {@link List} would not).
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/NodeList">MDN Reference</a>
 * @author Alex
 * @since 4/12/2019
 */
public class NodeListWrapper<T extends Node> extends AbstractList<T> {

  protected NodeList<T> nodeList;

  public NodeListWrapper(@Nonnull NodeList<T> nodeList) {
    Objects.requireNonNull(nodeList);
    this.nodeList = nodeList;
  }

  @Override
  public T get(int index) {
    return nodeList.getItem(index);
  }

  @Override
  public int size() {
    return nodeList.getLength();
  }
}
