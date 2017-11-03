package solutions.trsoftware.commons.client.util;

import java.io.Serializable;
import java.util.Map;

/**
 * Convenience class for representing a pair of two objects - a Map.Entry
 * implementation compatible with GWT.
 *
 * @author Alex
 */
public class Pair<K, V> implements Map.Entry<K,V>, Serializable {
  private K first;
  private V second;

  public Pair(K first, V second) {
    this.first = first;
    this.second = second;
  }

  public K getKey() {
    return first;
  }

  public V getValue() {
    return second;
  }

  public void setFirst(K first) {
    this.first = first;
  }

  public void setSecond(V second) {
    this.second = second;
  }
  
  public V setValue(V value) {
    V oldValue = this.second;
    this.second = value;
    return oldValue;
  }

  public K first() {
    return first;
  }

  public V second() {
    return second;
  }

  @Override
  public String toString() {
    String k = first != null ? first.toString() : null;
    String v = second != null ? second.toString() : null;
    return StringUtils.template("($1,$2)", k, v);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Pair)) return false;
    Pair pair = (Pair)o;
    if (first != null ? !first.equals(pair.first) : pair.first != null)
      return false;  // fail if firsts don't match
    if (second != null ? !second.equals(pair.second) : pair.second != null)
      return false;  // fail if seconds don't match
    return true;
  }

  public int hashCode() {
    int result;
    result = (first != null ? first.hashCode() : 0);
    result = 31 * result + (second != null ? second.hashCode() : 0);
    return result;
  }
}
