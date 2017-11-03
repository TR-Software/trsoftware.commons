package solutions.trsoftware.commons.shared.util;

/**
 * Date: Nov 14, 2008 Time: 2:55:18 PM
 *
 * @author Alex
 */
public interface TakesValue<V> extends HasValue<V> {

  public void setValue(V value);
}