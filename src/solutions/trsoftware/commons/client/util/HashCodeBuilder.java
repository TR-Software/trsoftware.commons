package solutions.trsoftware.commons.client.util;

/**
 * Constructs a hash code from a composite of hash codes of a collection of objects.
 *
 * Example: new HashCodeBuilder().update(a, b, c).hashCode();
 *
 * Oct 17, 2012
 *
 * @author Alex
 */
public class HashCodeBuilder {

  private int result = 0;

  public HashCodeBuilder update(Object a) {
    // this algorithm is based on IntelliJ's auto-generated hash codes
    result = 31 * result + (a != null ? a.hashCode() : 0);
    return this;
  }

  public HashCodeBuilder update(Object... inputs) {
    for (Object x : inputs) {
      update(x);
    }
    return this;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HashCodeBuilder that = (HashCodeBuilder)o;

    if (result != that.result) return false;

    return true;
  }

  public int hashCode() {
    return result;
  }
}
