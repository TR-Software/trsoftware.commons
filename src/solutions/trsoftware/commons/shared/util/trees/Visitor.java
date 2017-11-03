package solutions.trsoftware.commons.shared.util.trees;

/**
 * A <a href="https://en.wikipedia.org/wiki/Visitor_pattern">visitor</a> that can be used to traverse a
 * <a href="https://en.wikipedia.org/wiki/Tree_(data_structure)">tree</a> of {@link Node}{@code s}.
 *
 * <p>
 *   <b>NOTE: this visitor doesn't support polymorphism, and is therefore most useful for trees with only 1 type of node.</b>
 *   For polymorphic trees, it might be more useful to create a custom visitor type
 *   (see {@link com.google.gwt.dev.js.ast.JsSuperVisitor} for an example).
 * </p>
 *
 * @param <T> The supertype for the nodes in the type of tree on which this visitor operates.
 * @see Node
 * @author Alex, 10/31/2017
 */
public interface Visitor<T extends Node> {

  void visit(T node);

  TraversalStrategy getStrategy();
}
