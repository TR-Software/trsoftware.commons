package solutions.trsoftware.commons.shared.util.trees;

import java.util.Collection;

/**
 * The base type of a <a href="https://en.wikipedia.org/wiki/Tree_(data_structure)">tree</a> node which supports
 * the <a href="https://en.wikipedia.org/wiki/Visitor_pattern">Visitor pattern</a>.
 *
 * @param <T> the type of the data stored in the node.
 * @see Visitor
 * @author Alex, 10/31/2017
 */
public interface Node<T> {

  <V extends Visitor> void accept(V visitor);

  Collection<Node<T>> getChildren();

  /** @return the value stored in this node */
  T getValue();
}
