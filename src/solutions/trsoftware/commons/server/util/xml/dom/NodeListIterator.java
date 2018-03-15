package solutions.trsoftware.commons.server.util.xml.dom;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import solutions.trsoftware.commons.shared.util.iterators.IndexedIterator;

/**
 * Allows iterating over a {@link NodeList}, or converting it to an {@link Iterable}
 * (see {@link #makeIterable(NodeList)}).
 *
 * @author Alex
 * @since 3/12/2018
 */
public class NodeListIterator<N extends Node> extends IndexedIterator<N> {

  private NodeList nodeList;

  public NodeListIterator(NodeList nodeList) {
    super(nodeList.getLength());
    this.nodeList = nodeList;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected N get(int idx) {
    return (N)nodeList.item(idx);
  }

  /**
   * @return an {@link Iterable} adapter for the given {@link NodeList}
   */
  public static Iterable<Node> makeIterable(NodeList nodeList) {
    return () -> new NodeListIterator<Node>(nodeList);
  }
}
