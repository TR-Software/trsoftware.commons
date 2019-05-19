package solutions.trsoftware.commons.shared.util.text;

import java.util.stream.IntStream;

/**
 * Extensible base class that wraps an instance of {@link StringBuilder} to allow augmenting its functionality
 * (it's not possible to directly extend either {@link StringBuilder}, which is declared {@code final}, or
 * {@link AbstractStringBuilder}, which is package-private.
 * <p>
 * <b>GWT Note</b>: since this class resides under {@link solutions.trsoftware.commons.shared}, we don't delegate any of
 * the underlying {@link StringBuilder} methods that are not available in GWT.  If this class is used from normal Java
 * (not GWT client) code, use {@link #getStringBuilder()} to access those methods directly on the wrapped instance.
 *
 * @param <T> the type of the subclass: used as the return type of builder-pattern-style methods
 * @author Alex
 * @see <a href="https://stackoverflow.com/questions/17164375/subclassing-a-java-builder-class">
 *     "Subclassing a Java Builder class" discussion on StackOverflow</a>
 * @see <a href="https://www.artima.com/weblogs/viewpost.jsp?thread=133275">
 *     "Curiously Recurring Generic Pattern" by Bruce Eckel</a>
 * @since 5/14/2019
 */
public abstract class ExtensibleStringBuilder<T extends ExtensibleStringBuilder<T>> implements CharSequence {

  /**
   * The wrapped {@link StringBuilder} instance (to which all methods are delegated)
   */
  protected StringBuilder delegate;

  public ExtensibleStringBuilder(StringBuilder delegate) {
    this.delegate = delegate;
  }

  public ExtensibleStringBuilder() {
    this(new StringBuilder());
  }

  /**
   * @return the wrapped instance, to allow invoking any {@link StringBuilder} methods that are not delegated
   *     by this class (due to GWT restrictions)
   */
  public StringBuilder getStringBuilder() {
    return delegate;
  }

  /**
   * Subclasses must implement to return a {@code this} reference to themselves (this avoids the "unchecked" warnings
   * in methods that return {@link T} without using the {@link SuppressWarnings} annotation everywhere).
   *
   * @return {@code this} reference to the concrete subclass
   * @see <a href="https://stackoverflow.com/a/34741836/1965404">The idea behind this method (on StackOverflow)</a>
   */
  protected abstract T self();  // TODO: provide an implementation of this method (can use @SuppressWarnings on it, since it's just 1 place)

  /**
   * Delegates to {@link StringBuilder#append(int)}
   *
   * @see StringBuilder#append(int)
   */
  public T append(Object obj) {
    delegate.append(obj);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#append(int)}
   *
   * @see StringBuilder#append(int)
   */
  public T append(String str) {
    delegate.append(str);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#append(int)}
   *
   * @see StringBuilder#append(int)
   */
  public T append(StringBuffer sb) {
    delegate.append(sb);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#append(int)}
   *
   * @see StringBuilder#append(int)
   */
  public T append(CharSequence s) {
    delegate.append(s);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#append(int)}
   *
   * @see StringBuilder#append(int)
   */
  public T append(CharSequence s, int start, int end) {
    delegate.append(s, start, end);

    return self();
  }

  /**
   * Delegates to {@link StringBuilder#append(int)}
   *
   * @see StringBuilder#append(int)
   */
  public T append(char[] str) {
    delegate.append(str);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#append(int)}
   *
   * @see StringBuilder#append(int)
   */
  public T append(char[] str, int offset, int len) {
    delegate.append(str, offset, len);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#append(int)}
   *
   * @see StringBuilder#append(int)
   */
  public T append(boolean b) {
    delegate.append(b);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#append(int)}
   *
   * @see StringBuilder#append(int)
   */
  public T append(char c) {
    delegate.append(c);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#append(int)}
   *
   * @see StringBuilder#append(int)
   */
  public T append(int i) {
    delegate.append(i);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#append(int)}
   *
   * @see StringBuilder#append(int)
   */
  public T append(long lng) {
    delegate.append(lng);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#append(int)}
   *
   * @see StringBuilder#append(int)
   */
  public T append(float f) {
    delegate.append(f);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#append(int)}
   *
   * @see StringBuilder#append(int)
   */
  public T append(double d) {
    delegate.append(d);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#appendCodePoint(int)}
   *
   * @see StringBuilder#appendCodePoint(int)
   */
  public T appendCodePoint(int codePoint) {
    delegate.appendCodePoint(codePoint);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#delete(int, int)}
   *
   * @see StringBuilder#delete(int, int)
   */
  public T delete(int start, int end) {
    delegate.delete(start, end);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#deleteCharAt(int)}
   *
   * @see StringBuilder#deleteCharAt(int)
   */
  public T deleteCharAt(int index) {
    delegate.deleteCharAt(index);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#replace(int, int, String)}
   *
   * @see StringBuilder#replace(int, int, String)
   */
  public T replace(int start, int end, String str) {
    delegate.replace(start, end, str);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#insert(int, int)}
   *
   * @see StringBuilder#insert(int, int)
   */
  public T insert(int index, char[] str, int offset, int len) {
    delegate.insert(index, str, offset, len);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#insert(int, int)}
   *
   * @see StringBuilder#insert(int, int)
   */
  public T insert(int offset, Object obj) {
    delegate.insert(offset, obj);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#insert(int, int)}
   *
   * @see StringBuilder#insert(int, int)
   */
  public T insert(int offset, String str) {
    delegate.insert(offset, str);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#insert(int, int)}
   *
   * @see StringBuilder#insert(int, int)
   */
  public T insert(int offset, char[] str) {
    delegate.insert(offset, str);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#insert(int, int)}
   *
   * @see StringBuilder#insert(int, int)
   */
  public T insert(int dstOffset, CharSequence s) {
    delegate.insert(dstOffset, s);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#insert(int, int)}
   *
   * @see StringBuilder#insert(int, int)
   */
  public T insert(int dstOffset, CharSequence s, int start, int end) {
    delegate.insert(dstOffset, s, start, end);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#insert(int, int)}
   *
   * @see StringBuilder#insert(int, int)
   */
  public T insert(int offset, boolean b) {
    delegate.insert(offset, b);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#insert(int, int)}
   *
   * @see StringBuilder#insert(int, int)
   */
  public T insert(int offset, char c) {
    delegate.insert(offset, c);
    return self();
  }

  public T insert(int offset, int i) {
    delegate.insert(offset, i);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#insert(int, int)}
   *
   * @see StringBuilder#insert(int, int)
   */
  public T insert(int offset, long l) {
    delegate.insert(offset, l);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#insert(int, int)}
   *
   * @see StringBuilder#insert(int, int)
   */
  public T insert(int offset, float f) {
    delegate.insert(offset, f);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#insert(int, int)}
   *
   * @see StringBuilder#insert(int, int)
   */
  public T insert(int offset, double d) {
    delegate.insert(offset, d);
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#indexOf(String)}
   *
   * @see StringBuilder#indexOf(String)
   */
  public int indexOf(String str) {
    return delegate.indexOf(str);
  }

  /**
   * Delegates to {@link StringBuilder#indexOf(String)}
   *
   * @see StringBuilder#indexOf(String)
   */
  public int indexOf(String str, int fromIndex) {
    return delegate.indexOf(str, fromIndex);
  }

  /**
   * Delegates to {@link StringBuilder#lastIndexOf(String)}
   *
   * @see StringBuilder#lastIndexOf(String)
   */
  public int lastIndexOf(String str) {
    return delegate.lastIndexOf(str);
  }

  /**
   * Delegates to {@link StringBuilder#lastIndexOf(String)}
   *
   * @see StringBuilder#lastIndexOf(String)
   */
  public int lastIndexOf(String str, int fromIndex) {
    return delegate.lastIndexOf(str, fromIndex);
  }

  /**
   * Delegates to {@link StringBuilder#reverse()}
   *
   * @see StringBuilder#reverse()
   */
  public T reverse() {
    delegate.reverse();
    return self();
  }

  /**
   * Delegates to {@link StringBuilder#toString()}
   *
   * @see StringBuilder#toString()
   */
  @Override
  public String toString() {
    return delegate.toString();
  }

  /**
   * Delegates to {@link StringBuilder#length()}
   *
   * @see StringBuilder#length()
   */
  @Override
  public int length() {
    return delegate.length();
  }

  /**
   * Delegates to {@link StringBuilder#capacity()}
   *
   * @see StringBuilder#capacity()
   */
  public int capacity() {
    return delegate.capacity();
  }

  /**
   * Delegates to {@link StringBuilder#ensureCapacity(int)}
   *
   * @see StringBuilder#ensureCapacity(int)
   */
  public void ensureCapacity(int minimumCapacity) {
    delegate.ensureCapacity(minimumCapacity);
  }

  /**
   * Delegates to {@link StringBuilder#trimToSize()}
   *
   * @see StringBuilder#trimToSize()
   */
  public void trimToSize() {
    delegate.trimToSize();
  }

  /**
   * Delegates to {@link StringBuilder#setLength(int)}
   *
   * @see StringBuilder#setLength(int)
   */
  public void setLength(int newLength) {
    delegate.setLength(newLength);
  }

  /**
   * Delegates to {@link StringBuilder#charAt(int)}
   *
   * @see StringBuilder#charAt(int)
   */
  @Override
  public char charAt(int index) {
    return delegate.charAt(index);
  }

  /**
   * @see StringBuilder#getChars(int, int, char[], int)
   */
  public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
    delegate.getChars(srcBegin, srcEnd, dst, dstBegin);
  }

  /**
   * @see StringBuilder#setCharAt(int, char)
   */
  public void setCharAt(int index, char ch) {
    delegate.setCharAt(index, ch);
  }

  /**
   * @see StringBuilder#substring(int, int)
   */
  public String substring(int start) {
    return delegate.substring(start);
  }

  /**
   * Delegates to {@link StringBuilder#subSequence(int, int)}
   *
   * @see StringBuilder#subSequence(int, int)
   */
  @Override
  public CharSequence subSequence(int start, int end) {
    return delegate.subSequence(start, end);
  }

  /**
   * Delegates to {@link StringBuilder#substring(int, int)}
   *
   * @see StringBuilder#substring(int, int)
   */
  public String substring(int start, int end) {
    return delegate.substring(start, end);
  }

  /**
   * Delegates to {@link StringBuilder#chars()}
   *
   * @see StringBuilder#chars()
   */
  @Override
  public IntStream chars() {
    return delegate.chars();
  }

  // NOTE: not delegating the codePoints() method here because it's not available in GWT
}
