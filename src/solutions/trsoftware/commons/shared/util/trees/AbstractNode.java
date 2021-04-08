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

package solutions.trsoftware.commons.shared.util.trees;

import static solutions.trsoftware.commons.shared.util.LogicUtils.firstNonNull;

/**
 *
 * @author Alex, 10/31/2017
 */
public abstract class AbstractNode<T> implements Node<T> {

  @Override
  public <V extends Visitor> void accept(V visitor) {
    TraversalStrategy strategy = firstNonNull(visitor.getStrategy(), TraversalStrategy.PRE_ORDER);
    strategy.traverse(this, visitor);
    /*
    TODO: might want to also support generic visitation using startVisit/endVisit methods instead of a TraversalStrategy
    (see https://en.wikipedia.org/wiki/Tree_traversal#Generic_tree and com.google.gwt.dev.js.ast.JsVisitor)
    */
  }

  @Override
  public int depth() {
    return computeDepth(this);
  }

  /**
   * Computes the depth of a tree node, which is formally defined as
   * <em>the number of edges from the tree's root node to the node</em>
   * @return the number of edges from the tree's root node to the node
   * ({@code 0} for the root (i.e. 1st level node), {@code 1} for a 2nd-level node, etc.)
   * @see <a href="https://en.wikipedia.org/wiki/Tree_(data_structure)">Tree Data Structure</a>
   */
  public static <N extends Node<?>> int computeDepth(N node) {
    int depth = 0;
    Node<?> parent = node.getParent();
    while (parent != null) {
      depth++;
      parent = parent.getParent();
    }
    return depth;
  }

  /**
   * This implementation simply checks whether the result of {@link #getChildren()} is empty.
   * Subclasses may override to provide a more efficient implementation.
   * @return {@code true} iff this node doesn't have any children
   */
  @Override
  public boolean isLeaf() {
    return getChildren().isEmpty();
  }
}
