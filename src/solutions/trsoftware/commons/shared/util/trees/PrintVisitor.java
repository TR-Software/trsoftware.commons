package solutions.trsoftware.commons.shared.util.trees;

import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * @author Alex
 * @since 2/9/2018
 */
public class PrintVisitor<T extends Node> extends AbstractVisitor<T> {

  public PrintVisitor() {
  }

  public PrintVisitor(TraversalStrategy strategy) {
    super(strategy);
  }

  @Override
  public void visit(T node) {
    System.out.println(StringUtils.repeat(' ', node.depth()*2) + "+ " + node.toString());
  }
}
