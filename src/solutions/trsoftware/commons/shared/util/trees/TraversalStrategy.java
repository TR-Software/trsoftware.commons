package solutions.trsoftware.commons.shared.util.trees;

import solutions.trsoftware.commons.client.util.Assert;

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
    public <T> void traverse(Node<T> node, Visitor visitor) {
      Collection<Node<T>> children = node.getChildren();
      // 1) visit self
      visitor.visit(node);
      // 2) visit the subtrees of all children
      for (Node<T> child : children)
        child.accept(visitor);
    }
  },
  /**
   * DFS <a href="https://en.wikipedia.org/wiki/Tree_traversal#In-order">in-order</a> traversal:
   * visit left subtree, then self, then right subtree.
   * <b>This only makes sense for binary trees!</b>
   */
  IN_ORDER {
    @Override
    public <T> void traverse(Node<T> node, Visitor visitor) {
      Collection<Node<T>> children = node.getChildren();
      if (children.size() > 2)
        throw new IllegalArgumentException(this + " traversal makes sense only for binary trees");
      Iterator<Node<T>> childIter = children.iterator();
      // visit the left sub-tree
      if (childIter.hasNext())
        childIter.next().accept(visitor);
      // visit self
      visitor.visit(node);
      // visit the right sub-tree
      if (childIter.hasNext())
        childIter.next().accept(visitor);
      Assert.assertFalse(childIter.hasNext());  // this should never happen because we already checked the size of the collection
      throw new UnsupportedOperationException("Method .traverse has not been fully implemented yet.");
    }
  },
  /**
   * DFS <a href="https://en.wikipedia.org/wiki/Tree_traversal#Post-order">post-order</a> traversal:
   * visit children before self.
   */
  POST_ORDER {
    @Override
    public <T> void traverse(Node<T> node, Visitor visitor) {
      Collection<Node<T>> children = node.getChildren();
      // visit the subtrees of all children before visiting self
      for (Node<T> child : children)
        child.accept(visitor);
      visitor.visit(node);
    }
  },
  /** BFS <a href="https://en.wikipedia.org/wiki/Tree_traversal#Breadth-first_search">level-order</a> traversal */
  BFS {
    @Override
    public <T> void traverse(Node<T> node, Visitor visitor) {
      throw new UnsupportedOperationException("Method BFS.traverse has not been fully implemented yet.");
    }
  };

  public abstract <T> void traverse(Node<T> node, Visitor visitor);
}
