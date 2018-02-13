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

package solutions.trsoftware.commons.shared.util.trees;

import java.util.Collection;

/**
 * The base type for a <a href="https://en.wikipedia.org/wiki/Tree_(data_structure)">tree</a> node which supports
 * the <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor pattern</a>.
 *
 * @param <T> the type of the data stored in the node.
 * @see Visitor
 * @author Alex, 10/31/2017
 */
public interface Node<T> {

  <V extends Visitor> void accept(V visitor);

  /**
   * @return the parent of this node in the tree; {@code null} indicates that this node is the root.
   */
  Node<T> getParent();

  Collection<? extends Node<T>> getChildren();

  /**
   * @return {@code true} iff this node doesn't have any children
   */
  boolean isLeaf();

  /** @return the value stored in this node */
  T getData();

  /**
   * @return the depth of this node in the tree, which is formally defined as
   * <em>the number of edges from the tree's root node to the node</em>
   * ({@code 0} for the root (i.e. 1st level node), {@code 1} for a 2nd-level node, etc.)
   * @see <a href="https://en.wikipedia.org/wiki/Tree_(data_structure)">Tree Data Structure</a>
   */
  int depth();
}
