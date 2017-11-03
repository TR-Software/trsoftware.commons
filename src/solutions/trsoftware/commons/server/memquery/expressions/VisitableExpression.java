package solutions.trsoftware.commons.server.memquery.expressions;

/**
 * @param <R> the result type of the expression.
 * @param <A> the argument type of the expression. If the expression takes more than 1 arg (e.g. binary expression),
 * the argument type might be {@code Pair<X, Y>}.
 *
 * @author Alex, 1/11/14
 */
public interface VisitableExpression<A, R> extends Expression<A, R> {

  void accept(ExpressionVisitor visitor);

}
