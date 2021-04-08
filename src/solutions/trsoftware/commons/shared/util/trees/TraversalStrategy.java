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

import solutions.trsoftware.commons.shared.util.Assert;

import java.util.Collection;
import java.util.Iterator;

/**
 * Defines <a href="https://en.wikipedia.org/wiki/Tree_traversal">tree traversal</a> strategies to use with
 * the <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor pattern</a> implemented by a {@link Visitor}
 *
 * @author Alex, 10/31/2017
 */
public enum TraversalStrategy {
  /**
   * DFS <a href="https://en.wikipedia.org/wiki/Tree_traversal#Pre-order">pre-order</a> traversal:
   * visit self before children.
   */
  PRE_ORDER {
    @Override
    public <N extends Node<?>> void traverse(N node, Visitor<N> visitor) {
      if (node != null) {
        visitor.beginVisit(node);
        // 1) visit self
        visitor.visit(node);
        // 2) visit the subtrees of all children
        Collection<? extends Node<?>> children = node.getChildren();
        for (Node<?> child : children)
          child.accept(visitor);
        visitor.endVisit(node);
      }
    }
  },
  /**
   * DFS <a href="https://en.wikipedia.org/wiki/Tree_traversal#In-order">in-order</a> traversal:
   * visit left subtree, then self, then right subtree.
   * <b>This only makes sense for binary trees!</b>
   */
  IN_ORDER {
    @Override
    public <N extends Node<?>> void traverse(N node, Visitor<N> visitor) {
      if (node != null) {
        visitor.beginVisit(node);
        Collection<? extends Node<?>> children = node.getChildren();
        if (children.size() > 2)
          throw new IllegalArgumentException(this + " traversal makes sense only for binary trees");
        Iterator<? extends Node<?>> childIter = children.iterator();
        // visit the left sub-tree
        if (childIter.hasNext())
          childIter.next().accept(visitor);
        // visit self
        visitor.visit(node);
        // visit the right sub-tree
        if (childIter.hasNext())
          childIter.next().accept(visitor);
        visitor.endVisit(node);
        Assert.assertFalse(childIter.hasNext());  // this should never happen because we already checked the size of the collection
      }
    }
  },
  /**
   * DFS <a href="https://en.wikipedia.org/wiki/Tree_traversal#Post-order">post-order</a> traversal:
   * visit children before self.
   */
  POST_ORDER {
    @Override
    public <N extends Node<?>> void traverse(N node, Visitor<N> visitor) {
      if (node != null) {
        visitor.beginVisit(node);
        Collection<? extends Node<?>> children = node.getChildren();
        // visit the subtrees of all children before visiting self
        for (Node<?> child : children)
          child.accept(visitor);
        visitor.visit(node);
        visitor.endVisit(node);
      }
    }
  },
  /** BFS <a href="https://en.wikipedia.org/wiki/Tree_traversal#Breadth-first_search">level-order</a> traversal */
  BFS {
    @Override
    public <N extends Node<?>> void traverse(N node, Visitor<N> visitor) {
      throw new UnsupportedOperationException("Method BFS.traverse has not been fully implemented yet.");
    }
  };

  public abstract <N extends Node<?>> void traverse(N node, Visitor<N> visitor);
}
