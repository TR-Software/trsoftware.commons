package solutions.trsoftware.commons.client.jso;

/**
 * Represents the native JS types.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/typeof">Javascript typeof operator</a>
 *
 * @author Alex
 * @since 11/17/2017
 */
public enum JsType {

  UNDEFINED,
  BOOLEAN,
  NUMBER,
  STRING,
  FUNCTION,
  /** Either an actual object or a {@code null} */
  OBJECT,
  /** This type is new in ECMAScript 2015 */
  SYMBOL;

  /**
   * @param nativeName value obtained using a JS {@code typeof} expression
   * @return the {@code enum} constant corresponding to the given JS name
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/typeof">Javascript typeof operator</a>
   */
  public static JsType parse(String nativeName) {
    return valueOf(nativeName.toUpperCase());
  }

}
