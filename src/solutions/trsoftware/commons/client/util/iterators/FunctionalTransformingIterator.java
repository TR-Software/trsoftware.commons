package solutions.trsoftware.commons.client.util.iterators;

import solutions.trsoftware.commons.client.util.callables.Function1;

import java.util.Iterator;

/**
 * Uses the encapsulated function to do the transform.
 * @author Alex, 1/12/14
 */
public class FunctionalTransformingIterator<I, O> extends TransformingIterator<I, O> {

  private final Function1<I, O> transformer;

  public FunctionalTransformingIterator(Iterator<I> delegate, Function1<I, O> transformer) {
    super(delegate);
    this.transformer = transformer;
  }

  @Override
  public O transform(I input) {
    return transformer.call(input);
  }
}
