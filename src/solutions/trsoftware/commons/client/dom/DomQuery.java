/*
 * Copyright 2022 TR Software Inc.
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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import solutions.trsoftware.commons.client.jso.JsDocument;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static solutions.trsoftware.commons.client.dom.DomQuery.NodeVisitor.*;

/**
 * Performs lookups on the native DOM hierarchy.
 * 
 * Mar 25, 2010
 *
 * @author Alex
 */
public class DomQuery {

  // TODO(12/10/2021): move all methods to DomUtils and get rid of this class

  /**
   * Returns all children of parent matching the given tag name.
   *
   * <p style="color: #0073BF; font-weight: bold;">
   *   TODO(12/7/2021): delete this unused method, or repurpose it as a BFS version of {@link #walkNodeTree(Node, NodeVisitor, int)}
   *   and {@link NodeTreeIterator}
   * </p>
   *
   * @deprecated use {@link ParentNode#querySelectorAll(String)} instead
   */
  public static <T extends Element> Set<T> findChildrenByTagName(Element parent, String tagName, int maxIterations) {
    if (parent == null)
      return Collections.emptySet();
    LinkedList<Node> toBeExpanded = new LinkedList<Node>();  // BFS queue
    toBeExpanded.add(parent);
    Set<Node> alreadyExpanded = new HashSet<Node>();
    Set<T> matched = new HashSet<T>();  // the matched children
    for (int i = 0; !toBeExpanded.isEmpty() && i < maxIterations; i++) {
      Node node = toBeExpanded.removeFirst();
      if (node == null || node.getNodeType() != Node.ELEMENT_NODE) // ignore non-element nodes (e.g. TEXT), which will throw an exception because they don't define hashCode
        continue;
      if (alreadyExpanded.contains(node))
        return Collections.emptySet();  // we have a cyclic tree for some reason, exit to avoid infinite recursion
      alreadyExpanded.add(node);
      if (node.getNodeName().equalsIgnoreCase(tagName)) {  // WARNING: instanceof usages like (node instanceof IFrameElement) will be true for any JSO, not just iframe nodes, so we have to check the tag name instead
        matched.add((T)node);
      }
      if (node.hasChildNodes()) {
        enqueueChildren(node, toBeExpanded);
      }
    }
    return matched;
  }


  /**
   * Adds the given node's children from a NodeList (which isn't a
   * java.util - compatible collection) to a standard List structure.
   *
   * <p style="color: #0073BF; font-weight: bold;">
   *   TODO: can use {@link DomUtils#asList(NodeList)} for this
   * </p>
   */
  private static void enqueueChildren(Node node, List<Node> queue) {
    NodeList<Node> childNodes = node.getChildNodes();
    if (childNodes == null)
      return;
    for (int i = 0; i < childNodes.getLength(); i++) {
      queue.add(childNodes.getItem(i));
    }
  }

  /**
   * Performs a recursive depth-first (pre-order) traversal of a DOM subtree using the given visitor,
   * which receives the node and its depth as arguments for every element visited.
   * To stop the traversal at any time, the visitor can return {@code false} from {@link NodeVisitor#visit(Node, int)}.
   * <p>
   * This implementation assumes that the node tree contains no reference cycles (otherwise the traversal will continue
   * until a stack overflow error).
   *
   * @param root the root of the DOM subtree (at depth 0).
   * @param visitor to receive the visited nodes and control the traversal
   * @return {@code true} iff the traversal visited every node in the subtree (i.e. wasn't interrupted by the visitor
   *     returning {@code false}).
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Node#recurse_through_child_nodes">
   *     JavaScript code that inspired this implementation</a>
   */
  public static void walkNodeTree(Node root, NodeVisitor visitor) {
    walkNodeTree(root, visitor, 0);
  }

  /**
   * Recursive implementation of {@link #walkNodeTree(Node, NodeVisitor)}.
   *
   * @param depth the depth of the given root node (typically 0)
   */
  private static int walkNodeTree(Node root, NodeVisitor visitor, int depth) {
    /*
      TODO: consider generalizing this to allow more traversal types, like BFS (see solutions.trsoftware.commons.shared.util.trees.TraversalStrategy)
        - can repurpose the old DomQuery.findChildrenByTagName implementation for BFS
    */

    int result = visitor.visit(root, depth);
    if ((result & TERMINATE) != 0)
      return TERMINATE;
    else if ((result & SKIP_SUBTREE) == 0 && root.hasChildNodes()) {
      NodeList<Node> childNodes = root.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        Node child = childNodes.getItem(i);
        int childResult = walkNodeTree(child, visitor, depth + 1);
        if ((childResult & TERMINATE) != 0)
          return TERMINATE;
        else if ((childResult & SKIP_SIBLINGS) != 0)
          break;
      }
    }
    return result;
  }

  /**
   * Callback invoked during a DOM traversal with {@link #walkNodeTree(Node, NodeVisitor)}
   */
  public interface NodeVisitor {
    /**
     * Continue the traversal, visiting all children and siblings of the current node.
     */
    int CONTINUE = 0;
    /**
     * Terminate the traversal immediately without visiting any more nodes.
     */
    int TERMINATE = 1;
    /**
     * Skip the subtree of the current node,
     * but still visit its siblings (unless combined with {@link #SKIP_SIBLINGS}).
     */
    int SKIP_SUBTREE = 2;
    /**
     * Don't visit any other siblings of the current node,
     * but still visit its subtree (unless combined with {@link #SKIP_SUBTREE}).
     */
    int SKIP_SIBLINGS = 4;
    /**
     * Skip both the subtree and the siblings of the current node.
     */
    int SKIP_SUBTREE_AND_SIBLINGS = SKIP_SUBTREE | SKIP_SIBLINGS;

    /**
     * Invoked for every node visited during a DOM traversal with {@link #walkNodeTree(Node, NodeVisitor)}.
     *
     * @param node the current node being visited
     * @param depth the depth of the node in the tree (starting with {@code 0} for the root node).
     * @return a bitfield that determines how to continue (or terminate) the traversal:
     *   either {@link #CONTINUE}, {@link #TERMINATE}, {@link #SKIP_SUBTREE}, {@link #SKIP_SIBLINGS},
     *   or {@link #SKIP_SUBTREE_AND_SIBLINGS}
     */
    int visit(Node node, int depth);
  }

  /**
   * Streams all the nodes encountered during a depth-first (pre-order) traversal of a DOM subtree starting at the given node.
   * The main differences from {@link #walkNodeTree(Node, NodeVisitor)} are that this doesn't report the node depth
   * nor provides a way to control the traversal.
   * <p>
   * The behavior of the returned stream is undefined in the presence of structural modifications to the given node's
   * subtree, therefore it's best to consume the stream immediately upon receipt.
   *
   * @param root the root of the DOM subtree (at depth 0).
   * @return a stream that iterates over all the nodes in the subtree, in depth-first order
   * @see NodeTreeIterator
   * @see #walkNodeTree(Node, NodeVisitor)
   */
  public static Stream<Node> walkNodeTree(Node root) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new NodeTreeIterator(root),
        Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.NONNULL), false);
  }


  /**
   * Iterates over all the nodes encountered during a depth-first (pre-order) traversal of a DOM subtree starting at
   * a given node.
   * <p>
   * NOTE: This iterator doesn't provide any "fail-fast" guarantee in the presence of structural modifications
   * to the given node's subtree, therefore it's best to consume it immediately upon construction.
   *
   * @see #walkNodeTree(Node)
   * @see #walkNodeTree(Node, NodeVisitor)
   */
  public static class NodeTreeIterator implements Iterator<Node> {
    /*
      TODO: consider generalizing this to allow more traversal types (see solutions.trsoftware.commons.shared.util.trees.TraversalStrategy)
        - can use the LinkedList as a queue instead of a stack for a bread-first traversal
      TODO: can try to implement "fail-fast" behavior using a MutationObserver (https://developer.mozilla.org/en-US/docs/Web/API/MutationObserver)
    */
    private final LinkedList<Node> stack = new LinkedList<>();

    /**
     * @param root the starting node for the traversal
     */
    public NodeTreeIterator(Node root) {
      stack.push(root);
    }

    @Override
    public boolean hasNext() {
      return !stack.isEmpty();
    }

    @Override
    public Node next() {
      Node node = stack.pop();  // NOTE: this throws NoSuchElementException, as required by the Iterator contract
      if (node.hasChildNodes()) {
        NodeList<Node> childNodes = node.getChildNodes();
        // push child nodes in reverse order to maintain left-to-right encounter order (see https://en.wikipedia.org/wiki/Depth-first_search#:~:text=These%20two%20variations,D%2C%20C%2C%20G.)
        for (int i = childNodes.getLength() - 1; i >= 0; i--) {
          stack.push(childNodes.getItem(i));
        }
      }
      return node;
    }
  }
}
