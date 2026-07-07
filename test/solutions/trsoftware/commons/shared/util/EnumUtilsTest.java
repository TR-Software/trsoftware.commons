package solutions.trsoftware.commons.shared.util;

import com.google.common.collect.ImmutableSet;
import com.google.gwt.core.shared.GwtIncompatible;
import solutions.trsoftware.commons.server.testutil.SubProcessTester;
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.testutil.TestUtils;
import solutions.trsoftware.commons.shared.util.function.ThrowingRunnable;

import java.util.*;

import static solutions.trsoftware.commons.server.testutil.SubProcessTester.*;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertThrows;
import static solutions.trsoftware.commons.shared.util.EnumUtils.*;

/**
 * @author Alex
 * @since 3/1/2026
 */
@SuppressWarnings("NonJREEmulationClassesInClientCode")  // GWT doesn't emulate EnumSet#of(Enum, Enum), but this still compiles just fine (implicit EnumSet#of(Enum, Enum[]))
public class EnumUtilsTest extends BaseTestCase {

  private enum Enum0 {  }

  private enum Enum1 { E0; }

  private enum Enum2 { E0, E1; }

  private enum Enum3 { E0, E1, E2; }

  private enum Enum32 { E0, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20, E21, E22, E23, E24, E25, E26, E27, E28, E29, E30, E31; }

  private enum Enum48 { E0, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13, E14, E15, E16, E17, E18, E19, E20, E21, E22, E23, E24, E25, E26, E27, E28, E29, E30, E31, E32, E33, E34, E35, E36, E37, E38, E39, E40, E41, E42, E43, E44, E45, E46, E47; }

  /**
   * Tests {@link EnumUtils#enumSetToInt(EnumSet)}
   */
  public void testEnumSetToInt() {
    testEnumSetToInt(0, null);
    testEnumSetToInt(0, EnumSet.noneOf(Enum0.class));
    testEnumSetToInt(0, EnumSet.allOf(Enum0.class));  // Enum0 has no values

    testEnumSetToInt(0, EnumSet.noneOf(Enum1.class));
    testEnumSetToInt(1, EnumSet.allOf(Enum1.class));

    testEnumSetToInt(0, EnumSet.noneOf(Enum2.class));
    testEnumSetToInt(1, EnumSet.of(Enum2.E0));
    testEnumSetToInt(2, EnumSet.of(Enum2.E1));
    testEnumSetToInt(3, EnumSet.allOf(Enum2.class));

    testEnumSetToInt(0, EnumSet.noneOf(Enum3.class));
    testEnumSetToInt(1, EnumSet.of(Enum3.E0));
    testEnumSetToInt(2, EnumSet.of(Enum3.E1));
    testEnumSetToInt(3, EnumSet.of(Enum3.E0, Enum3.E1));
    testEnumSetToInt(4, EnumSet.of(Enum3.E2));
    testEnumSetToInt(5, EnumSet.of(Enum3.E0, Enum3.E2));
    testEnumSetToInt(6, EnumSet.of(Enum3.E1, Enum3.E2));
    testEnumSetToInt(7, EnumSet.allOf(Enum3.class));

    // test an enum having more than 32 values
    // 1) set not containing any element with ordinal > 32: should work just fine
    testEnumSetToInt(-1, EnumSet.range(Enum48.E0, Enum48.E31));
    // 2) set containing an element with ordinal > 32: should throw AssertionError if assertions are enabled
    assertThrows(AssertionError.class, () -> enumSetToInt(EnumSet.range(Enum48.E30, Enum48.E36)));
  }

  private <E extends Enum<E>> void testEnumSetToInt(int expected, EnumSet<E> enumSet) {
    assertEquals(expected, enumSetToInt(enumSet));
    /*// TODO: temp - could test the array version here too:
    //noinspection unchecked // TODO: @GwtIncompatible
    assertEquals(expected, enumSetToInt(enumSet.toArray((T[])Array.newInstance(elementType, elements.length))));*/
  }

  /**
   * Tests the overloaded methods {@link EnumUtils#enumSetToInt(Enum[])}, {@link EnumUtils#enumSetToInt(Collection)},
   * and {@link EnumUtils#enumSetToInt(Iterator)}
   */
  public void testEnumSetToInt2() {
    testEnumSetToInt2(0, Enum0.class);  // Enum0 has no values

    testEnumSetToInt2(0, Enum1.class);
    testEnumSetToInt2(1, Enum1.class, Enum1.E0);

    testEnumSetToInt2(0, Enum2.class);
    testEnumSetToInt2(1, Enum2.class, Enum2.E0);
    testEnumSetToInt2(2, Enum2.class, Enum2.E1);
    testEnumSetToInt2(3, Enum2.class, Enum2.E0, Enum2.E1);

    testEnumSetToInt2(0, Enum3.class);
    testEnumSetToInt2(1, Enum3.class, Enum3.E0);
    testEnumSetToInt2(2, Enum3.class, Enum3.E1);
    testEnumSetToInt2(3, Enum3.class, Enum3.E0, Enum3.E1);
    testEnumSetToInt2(4, Enum3.class, Enum3.E2);
    testEnumSetToInt2(5, Enum3.class, Enum3.E0, Enum3.E2);
    testEnumSetToInt2(6, Enum3.class, Enum3.E1, Enum3.E2);
    testEnumSetToInt2(7, Enum3.class, Enum3.E0, Enum3.E1, Enum3.E2);
  }

  @SafeVarargs
  private final <E extends Enum<E>> void testEnumSetToInt2(int expected, Class<E> elementType, E... elements) {
    EnumSet<E> enumSet = newEnumSet(elementType, elements);
    assertEquals(expected, enumSetToInt(enumSet));
    // test the array version of the same method
    assertEquals(expected, enumSetToInt(elements));
    // test the Collection and Iterator versions
    assertEquals(expected, enumSetToInt(Arrays.asList(elements)));
    assertEquals(expected, enumSetToInt(ImmutableSet.copyOf(elements)));

    // TODO: temp
    //noinspection unchecked // TODO: @GwtIncompatible
//    assertEquals(expected, enumSetToInt(enumSet.toArray((T[])Array.newInstance(elementType, elements.length))));

  }

  @SafeVarargs
  public static <E extends Enum<E>> EnumSet<E> newEnumSet(Class<E> elementType, E... elements) {
    EnumSet<E> enumSet = EnumSet.noneOf(elementType);
    Collections.addAll(enumSet, elements);
    return enumSet;
  }

  public void testEnumSetFromInt() {
    testEnumSetFromInt(Enum0.class, 0, EnumSet.noneOf(Enum0.class));
    testEnumSetFromInt(Enum0.class, 0, EnumSet.allOf(Enum0.class));  // Enum0 has no values

    testEnumSetFromInt(Enum1.class, 0, EnumSet.noneOf(Enum1.class));
    testEnumSetFromInt(Enum1.class, 1, EnumSet.allOf(Enum1.class));

    testEnumSetFromInt(Enum2.class, 0, EnumSet.noneOf(Enum2.class));
    testEnumSetFromInt(Enum2.class, 1, EnumSet.of(Enum2.E0));
    testEnumSetFromInt(Enum2.class, 2, EnumSet.of(Enum2.E1));
    testEnumSetFromInt(Enum2.class, 3, EnumSet.allOf(Enum2.class));

    testEnumSetFromInt(Enum3.class, 0, EnumSet.noneOf(Enum3.class));
    testEnumSetFromInt(Enum3.class, 1, EnumSet.of(Enum3.E0));
    testEnumSetFromInt(Enum3.class, 2, EnumSet.of(Enum3.E1));
    testEnumSetFromInt(Enum3.class, 3, EnumSet.of(Enum3.E0, Enum3.E1));
    testEnumSetFromInt(Enum3.class, 4, EnumSet.of(Enum3.E2));
    testEnumSetFromInt(Enum3.class, 5, EnumSet.of(Enum3.E0, Enum3.E2));
    testEnumSetFromInt(Enum3.class, 6, EnumSet.of(Enum3.E1, Enum3.E2));
    testEnumSetFromInt(Enum3.class, 7, EnumSet.allOf(Enum3.class));

    // test with an enum type that contains more than 32 values
    testEnumSetFromInt(Enum48.class, -1, EnumSet.range(Enum48.E0, Enum48.E31));
  }

  private <E extends Enum<E>> void testEnumSetFromInt(Class<E> elementType, int bitField, EnumSet<E> expected) {
    assertEquals(expected, enumSetFromInt(elementType, bitField));
    // test that any bits outside the range of this enum are simply ignored
    int nBits = elementType.getEnumConstants().length;  // number of bits needed to represent all EnumSet permutations for this enum class
    if (nBits < Integer.SIZE) {
      int mask = -(1 << nBits);  // n LSB bits are 0 and the rest are 1
      for (int i = 0; i < 10; i++) {
        // prepend some random bits in positions higher than nBits
        int argWithExtraBits = (rnd.nextInt() & mask) | bitField;
        assertEquals(expected, enumSetFromInt(elementType, argWithExtraBits));
        // this should return the same result as the original bitField (the extra bits should be ignored)
      }
    }
  }

  /**
   * Tests {@link EnumUtils#enumSetToInt(EnumSet)} with a set that contains elements with ordinal &ge; 32.
   */
  @Slow
  @GwtIncompatible  // forking the JVM
  public void testEnumSetToIntWithOverflow() throws Exception {
    boolean assertsEnabled = EnumUtils.class.desiredAssertionStatus();
    /*//noinspection AssertWithSideEffects - Intentional side effect: assigns true to assertsEnabled if assertions are enabled
    assert assertsEnabled = true;*/

    // invoke enumSetToInt for an EnumSet having an element ordinal >= 32
    EnumSet<Enum48> enumSetWithOverflow = EnumSet.range(Enum48.E30, Enum48.E36);
    EnumSetToIntCommand<Enum48, EnumSet<Enum48>> command = new EnumSetToIntCommand<>(enumSetWithOverflow);
    
    
    if (assertsEnabled) {
      // assertions enabled in host JVM, can execute the command directly to trigger the expected AssertionError
      TestUtils.printSectionHeader("With assertions enabled");
      assertThrows(AssertionError.class, (ThrowingRunnable)command::call);
      // assertions disabled in host JVM, have to execute command in subprocess to check outcome when assertions are disabled
      TestUtils.printSectionHeader("With assertions disabled (forking JVM to omit -ea)");
      Result response = invokeInSubProcess(Collections.emptyList(), command);
      System.out.println("response = " + response);
      if (response instanceof NormalResult) {
        NormalResult result = (NormalResult)response;
        assertEquals(enumSetToInt(EnumSet.range(Enum48.E30, Enum48.E31)), result.getResult());
      }
      else {
        fail("Exceptional result: " + ((ExceptionalResult)response).getException().getMessage());
      }
    }
    else {
      // assertions disabled in host JVM, have to execute command in subprocess to check outcome with assertions enabled
      TestUtils.printSectionHeader("With assertions enabled (forking JVM to include -ea)");
      Result response = invokeInSubProcess(Arrays.asList("-ea"),
          command);
      System.out.println("response = " + response);
      assertTrue(response instanceof ExceptionalResult);
      ExceptionalResult result = (ExceptionalResult)response;
      Throwable ex = result.getException();
      assertTrue(ex instanceof AssertionError);
      String msgHeader = "---------- Expected exception from subprocess executing " + command + " ----------";
      System.out.println(msgHeader);
      ex.printStackTrace(System.out);
      System.out.println(StringUtils.repeat('-', msgHeader.length()));
      TestUtils.printSectionHeader("With assertions disabled");
      // assertions already disabled in host JVM, can execute the command directly without it triggering an AssertionError
      assertEquals(enumSetToInt(EnumSet.range(Enum48.E30, Enum48.E31)), (int)command.call());
    }
  }

  public void testGetElementType() throws Exception {
    testGetElementType(Enum1.class);
    testGetElementType(Enum2.class);
    testGetElementType(Enum3.class);
    testGetElementType(Enum48.class);

    // Enum0 is a special case where getElementType breaks down b/c the enum class doesn't actually have any declared constants
    assertThrows(IllegalArgumentException.class, (Runnable)() -> testGetElementType(Enum0.class));
  }

  private <E extends Enum<E>> void testGetElementType(Class<E> elementType) {
    EnumSet<E> all = EnumSet.allOf(elementType);
    EnumSet<E> none = EnumSet.noneOf(elementType);
    assertEquals(elementType, getElementType(all));
    // should also work for an empty enum set, assuming that the enum itself is not empty (e.g. Enum0)
    assertEquals(elementType, getElementType(none));
  }

  /**
   * Helper for {@link #testEnumSetToIntWithOverflow()}:
   * Invokes {@link EnumUtils#enumSetToInt(EnumSet)} with the given argument.
   *
   * @param <E> the enum type
   * @param <T> the enum set type
   * @see SubProcessTester
   */
  private static class EnumSetToIntCommand<E extends Enum<E>, T extends Collection<E>> implements SubProcessTester.Command<Integer> {
    private T arg;

    private EnumSetToIntCommand(T arg) {
      this.arg = arg;
    }

    private EnumSetToIntCommand() {
    }

    @Override
    public Integer call() throws Exception {
      return enumSetToInt(arg);
    }

    @Override
    public String toString() {
      return StringUtils.methodCallToString("enumSetToInt", arg);
    }
  }

}