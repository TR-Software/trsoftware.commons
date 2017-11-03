package solutions.trsoftware.commons.shared.util.trees;

import static solutions.trsoftware.commons.client.util.LogicUtils.firstNonNull;

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
}
