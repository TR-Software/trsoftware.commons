package solutions.trsoftware.commons.server.memquery.expressions;

/**
 * @author Alex, 1/12/14
 */
public abstract class ExpressionVisitor {

  public void visit(ColRef colRef) { }

  public void visit(Constant c) { }

}
