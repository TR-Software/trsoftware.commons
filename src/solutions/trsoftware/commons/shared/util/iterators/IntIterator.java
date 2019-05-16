package solutions.trsoftware.commons.shared.util.iterators;

import java.util.Iterator;
import java.util.PrimitiveIterator;

/**
 * An {@link Iterator} optimized for primitive {@code int} elements:
 * provides the {@link #nextInt()} method to avoid auto-boxing.
 *
 * @see PrimitiveIterator.OfInt
 * @author Alex
 * @since 1/11/2019
 */
public interface IntIterator extends PrimitiveIterator.OfInt {

  // TODO: get rid of this interface (make all subclasses implement java.util.PrimitiveIterator.OfInt directly)

}
