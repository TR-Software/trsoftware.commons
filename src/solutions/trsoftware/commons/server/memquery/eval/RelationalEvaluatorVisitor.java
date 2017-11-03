package solutions.trsoftware.commons.server.memquery.eval;

/**
 * @author Alex, 10/10/2016
 */
public interface RelationalEvaluatorVisitor {

  void visit(ValueEvaluator evaluator);
  void visit(BinaryOperationEvaluator evaluator);
  void visit(UnaryOperationEvaluator evaluator);
}
