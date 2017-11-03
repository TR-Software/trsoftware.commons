package solutions.trsoftware.commons.server.memquery.algebra;

/**
 * @author Alex, 1/16/14
 */
public interface RelationalExpressionVisitor {

  void visit(RelationalValue expr);

  void visit(BinaryOperation op);

  public void visit(UnaryOperation op);

}
