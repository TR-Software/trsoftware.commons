package solutions.trsoftware.commons.server.memquery.eval;

import solutions.trsoftware.commons.server.memquery.Relation;

import java.util.concurrent.Callable;

/**
 * @author Alex, 1/16/14
 */
public interface RelationalEvaluator<R extends Relation> extends Callable<R> {

  void accept(RelationalEvaluatorVisitor visitor);

}
