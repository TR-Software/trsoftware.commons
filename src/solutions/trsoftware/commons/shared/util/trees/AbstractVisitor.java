package solutions.trsoftware.commons.shared.util.trees;

/**
 * Base class for a {@link Visitor} that defines a {@link TraversalStrategy}
 *
 * @author Alex, 10/31/2017
 */
public abstract class AbstractVisitor<T extends Node> implements Visitor<T> {

  protected final TraversalStrategy strategy;

  public AbstractVisitor(TraversalStrategy strategy) {
    this.strategy = strategy;
  }

  public AbstractVisitor() {
    this(null);
  }

  @Override
  public TraversalStrategy getStrategy() {
    return strategy;
  }
}
