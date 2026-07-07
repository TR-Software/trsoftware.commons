package solutions.trsoftware.commons.shared.util;

import com.google.common.base.Strings;
import solutions.trsoftware.commons.shared.util.iterators.ArrayIterator;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;

/**
 * @author Alex
 * @since 3/1/2026
 */
public class EnumUtils {
  // TODO: doc & unit test

  /**
   * Creates a bit field representation of the given enum set, such that bit {@code i} is {@code 1} iff the enum set
   * contains an element with {@linkplain Enum#ordinal ordinal} value {@code i}.
   * <p>
   * <i>Note:</i> it's possible to use this method even if the enum type {@link E} declares more than {@value Integer#SIZE} constants,
   * as long as the given set doesn't actually contain any elements with an {@linkplain Enum#ordinal ordinal} value
   * greater than {@value Integer#SIZE}.
   *
   * @param enumSet the set of elements to encode
   * @param <E> the enum type
   * @throws AssertionError if assertions are enabled and any element of the set has an {@linkplain Enum#ordinal ordinal}
   *   value greater than {@value Integer#SIZE}; otherwise, if assertions disabled, the non-compliant elements are simply ignored
   * @return bitfield representation of the given enum set
   * @see #enumSetFromInt(Class, int)
   */
  public static <E extends Enum<E>> int enumSetToInt(EnumSet<E> enumSet) {
    return enumSetToInt((Collection<E>)enumSet);
  }

  /**
   * Creates a bit field representation of an enum set containing the unique elements from the given collection, such that
   * bit {@code i} is {@code 1} iff the collection contains an element with {@linkplain Enum#ordinal ordinal} value {@code i}.
   * <p>
   * <i>Note:</i> it's possible to use this method even if the enum type {@link E} declares more than {@value Integer#SIZE} constants,
   * as long as the given set doesn't actually contain any elements with an {@linkplain Enum#ordinal ordinal} value
   * greater than {@value Integer#SIZE}.
   *
   * @param elements the elements to encode (duplicated values ignored)
   * @param <E> the enum type
   * @throws AssertionError if assertions are enabled and any element of the collection has an {@linkplain Enum#ordinal ordinal}
   *   value greater than {@value Integer#SIZE}; otherwise, if assertions disabled, the non-compliant elements are simply ignored
   * @return bitfield representation of the given enum set
   */
  public static <E extends Enum<E>> int enumSetToInt(Collection<E> elements) {
    return elements == null || elements.isEmpty() ? 0
        : enumSetToInt(elements.iterator());
  }

  /**
   * Creates a bit field representation of an enum set containing the given elements, such that
   * bit {@code i} is {@code 1} iff the arg array contains an element with {@linkplain Enum#ordinal ordinal} value {@code i}.
   * <p>
   * <i>Note:</i> it's possible to use this method even if the enum type {@link E} declares more than {@value Integer#SIZE} constants,
   * as long as the given inputs don't actually contain any element with an {@linkplain Enum#ordinal ordinal} value
   * greater than {@value Integer#SIZE}.
   *
   * @param elements the elements to encode (duplicated values ignored)
   * @param <E> the enum type
   * @throws AssertionError if assertions are enabled and any input element has an {@linkplain Enum#ordinal ordinal}
   *   value greater than {@value Integer#SIZE}; otherwise, if assertions disabled, the non-compliant elements are simply ignored
   * @return bitfield representation of the given enum set
   */
  @SafeVarargs
  public static <E extends Enum<E>> int enumSetToInt(E... elements) {
    return elements.length == 0 ? 0
        : enumSetToInt(new ArrayIterator<>(elements));
  }

  /**
   * Creates a bit field representation of an enum set containing the unique elements from the given iterator, such that
   * bit {@code i} is {@code 1} iff the iterator contains an element with {@linkplain Enum#ordinal ordinal} value {@code i}.
   * <p>
   * <i>Note:</i> it's possible to use this method even if the enum type {@link E} declares more than {@value Integer#SIZE} constants,
   * as long as the iterator doesn't actually contain any elements with an {@linkplain Enum#ordinal ordinal} value
   * greater than {@value Integer#SIZE}.
   *
   * @param elements iterator of the elements to encode (duplicated values ignored)
   * @param <E> the enum type
   * @throws AssertionError if assertions are enabled and any element of the collection has an {@linkplain Enum#ordinal ordinal}
   *   value greater than {@value Integer#SIZE}; otherwise, if assertions disabled, the non-compliant elements are simply ignored
   * @return bitfield representation of the given enum set
   */
  public static <E extends Enum<E>> int enumSetToInt(Iterator<E> elements) {
    int bitField = 0;
    while (elements.hasNext()) {
      E next = elements.next();
      int i = next.ordinal();
      assert i < Integer.SIZE : Strings.lenientFormat("Contains element with ordinal >= 32 (%s.ordinal() = %s)", next.name(), i);
      //noinspection ConstantConditions - in case assertions are disabled, just skip any elements with ordinal >= 32
      if (i >= Integer.SIZE)
        continue;
      bitField |= 1 << i;
    }
    return bitField;
  }

  /**
   * Reconstructs an {@link EnumSet} of the given enum type from a bit field representation where
   * bit {@code i} is {@code 1} iff the enum set contains an element with {@linkplain Enum#ordinal ordinal} value {@code i}.
   *
   * @param elementType the enum class
   * @param bitField bit field representation of the enum set, such that
   *   bit {@code i} is {@code 1} iff the set contains an element with ordinal value {@code i}
   * @param <E> the enum type
   * @return an enum set containing the elements specified by the given bit field
   */
  public static <E extends Enum<E>> EnumSet<E> enumSetFromInt(Class<E>elementType, int bitField) {
    EnumSet<E> enumSet = EnumSet.noneOf(elementType);
    if (bitField != 0) {
      E[] values = elementType.getEnumConstants();
      // Note: this logic is intentionally permissive: uses only the bits up to the number of actual enum constants,
      // without caring if any other bits are set
      for (int i = 0; i < Integer.SIZE && i < values.length; i++) {
        if (((bitField >>> i) & 1) == 1)  // if the i-th bit is 1
          enumSet.add(values[i]);
      }
    }
    return enumSet;
  }  // TODO: don't have to restrict return type to EnumSet, can use arbitrary Set impls (<S extends Set<E>>) passed as a supplier, maybe even an arbitrary Collection<E> type

  /**
   * Dynamically determines the element type of an {@link EnumSet}.
   * <p>
   * <b>Note:</b> this method supports empty input sets if the enum type itself is not empty,
   * but fails if the enum class doesn't actually contain any values.
   *
   * @param <E> The enum type
   * @param enumSet The EnumSet instance
   * @return The class of the enum elements
   */
  public static <E extends Enum<E>> Class<E> getElementType(EnumSet<E> enumSet) {
    // Note: this code was suggested by a Google AI overview for search "java EnumSet get element class"
    if (!enumSet.isEmpty()) {
      return enumSet.iterator().next().getDeclaringClass();
    }
    else {
      /*
       If the set is empty, create its complement (which will not be empty unless the enum class itself has no declared values)
       and get the class from the first element of the complement.
      */
      EnumSet<E> complement = EnumSet.complementOf(enumSet);
      if (!complement.isEmpty()) {
        return complement.iterator().next().getDeclaringClass();
      } else {
        // we have the unlikely case that the enum class itself is empty (no declared constants)
        throw new IllegalArgumentException("Unable to derive element type from empty EnumSet if the actual enum class is also empty");
      }
    }
  }  // TODO: unit test
}
