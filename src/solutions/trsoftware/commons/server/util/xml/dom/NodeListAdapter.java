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

package solutions.trsoftware.commons.server.util.xml.dom;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.AbstractList;
import java.util.List;

/**
 * Decorates a {@link org.w3c.dom.NodeList} as a {@link List} of typed elements, which can be any sub-type of
 * {@link org.w3c.dom.Node}, such as {@link org.w3c.dom.Element}.
 *
 *
 * @param <N> any sub-type of {@link org.w3c.dom.Node} (such as {@link org.w3c.dom.Element}).
 * <em>NOTE: {@link ClassCastException}s might be thrown by the {@link #get(int)} method if the encapsulated {@link NodeList}
 * contains elements of a different type.</em>
 *
 * @author Alex
 * @since 3/13/2018
 */
public class NodeListAdapter<N extends Node> extends AbstractList<N> implements NodeList {

  private NodeList nodeList;

  public NodeListAdapter(NodeList nodeList) {
    this.nodeList = nodeList;
  }

  @Override
  @SuppressWarnings("unchecked")
  public N get(int index) {
    int size = size();
    if (index >= size)
      throw new IndexOutOfBoundsException("Index: "+index+", Size: " + size);
    return (N)nodeList.item(index);
  }

  @Override
  public int size() {
    return nodeList.getLength();
  }

  @Override
  public Node item(int index) {
    return nodeList.item(index);
  }

  @Override
  public int getLength() {
    return nodeList.getLength();
  }
}
