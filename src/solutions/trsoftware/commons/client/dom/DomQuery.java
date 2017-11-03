package solutions.trsoftware.commons.client.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;

import java.util.*;

/**
 * Performs lookups on the native DOM hierarchy.
 * 
 * Mar 25, 2010
 *
 * @author Alex
 */
public class DomQuery {


  /** Returns all children of parent matching the given tag name */
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
        enqueChildren(node, toBeExpanded);
      }
    }
    return matched;
  }

  /**
   * Adds the given node's children from a NodeList (which isn't a
   * java.util - compatible collection) to a standard List structure.
   *
   * This method may be overridden to facilitate unit testing.
   */
  private static void enqueChildren(Node node, List<Node> queue) {
    NodeList<Node> childNodes = node.getChildNodes();
    if (childNodes == null)
      return;
    for (int i = 0; i < childNodes.getLength(); i++) {
      queue.add(childNodes.getItem(i));
    }
  }
}
