package solutions.trsoftware.commons.server.memquery.expressions;

import solutions.trsoftware.commons.client.util.callables.Function1;

/**
 * @param <R> the result type of the expression.
 * @param <A> the argument type of the expression. If the expression takes more than 1 arg (e.g. binary expression),
 * the argument type might be {@code Pair<X, Y>}.
 *
 * @author Alex, 1/11/14
 */
public interface Expression<A, R> extends Function1<A, R> {

  Class<R> getResultType();

  Class<A> getArgType();

}
