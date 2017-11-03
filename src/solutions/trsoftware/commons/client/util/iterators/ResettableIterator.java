package solutions.trsoftware.commons.client.util.iterators;

import java.util.Iterator;

/**
 * Defines an iterator that can be reset back to an initial state. This interface allows an iterator to be repeatedly reused.
 *
 * @author Alex, 10/15/2016
 */
public interface ResettableIterator<E> extends Iterator<E>, solutions.trsoftware.commons.client.util.Resettable {

}
