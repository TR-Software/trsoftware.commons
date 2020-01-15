package solutions.trsoftware.commons.server.util.gql;

import com.google.common.escape.CharEscaper;
import com.google.common.escape.Escaper;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * A a GQL value literal that can be used in the {@code WHERE} clause of a GQL query.
 *
 * @author Alex
 * @since 12/2/2019
 *
 * @see <a href="https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference">GQL Reference</a>
 * @see #valueOf(String)
 * @see #valueOf(Date)
 * @see #valueOf(boolean)
 * @see #NULL
 * @see #TRUE
 * @see #FALSE
 */
public abstract class GqlLiteral implements GqlElement {

  /**
   * Delegates to {@link #toGql()}
   */
  @Override
  public final String toString() {
    return toGql();
  }

  /**
   * @return an instance of {@link StringLiteral} wrapping the given value, or {@link #NULL} if the arg is {@code null}
   */
  public static GqlLiteral valueOf(String value) {
    return value == null ? NULL : new StringLiteral(value);
  }

  /**
   * @return an instance of {@link DateTimeLiteral} wrapping the given value, or {@link #NULL} if the arg is {@code null}
   */
  public static GqlLiteral valueOf(Date value) {
    return value == null ? NULL : new DateTimeLiteral(value);
  }

  /**
   * @return the literal {@link #TRUE} or {@link #FALSE}, depending on the given arg.
   */
  public static GqlLiteral valueOf(boolean value) {
    return value ? TRUE : FALSE;
  }
  
  /**
   * The GQL literal {@code NULL}
   */
  public static final GqlLiteral NULL = new GqlLiteral() {
    @Override
    public String toGql() {
      return "NULL";
    }
  };
  
  /**
   * The GQL boolean literal {@code TRUE}
   * @see #valueOf(boolean)
   */
  public static final GqlLiteral TRUE = new GqlLiteral() {
    @Override
    public String toGql() {
      return "TRUE";
    }
  };
  
  /**
   * The GQL boolean literal {@code FALSE}
   * @see #valueOf(boolean)
   */
  public static final GqlLiteral FALSE = new GqlLiteral() {
    @Override
    public String toGql() {
      return "FALSE";
    }
  };
  

  /**
   * A GQL literal for a string value, as a single-quoted string.
   * Single-quote characters in the string are escaped as ''. For example: 'Joe''s Diner'
   *
   * @see #valueOf(String)
   */
  public static class StringLiteral extends GqlLiteral {

    private static final char[] SINGLE_QUOTE_ESCAPE = "''".toCharArray();

    private static final Escaper SINGLE_QUOTE_ESCAPER = new CharEscaper() {
      @Override
      protected char[] escape(char c) {
        if (c == '\'')
          return SINGLE_QUOTE_ESCAPE;
        return null;
      }
    };

    private final String value;

    public StringLiteral(@Nonnull String value) {
      this.value = Objects.requireNonNull(value);
    }

    /**
     * A GQL string literal is a single-quoted string with single-quote characters in the string escaped as ''.
     * For example: 'Joe''s Diner'
     * @return a GQL string literal representing the given string
     */
    public static String toGql(String value) {
      return "'" + SINGLE_QUOTE_ESCAPER.escape(value) + "'";
    }

    @Override
    public String toGql() {
      return toGql(value);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      StringLiteral that = (StringLiteral)o;
      return value.equals(that.value);
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }
  }

  /**
   * A GQL {@code DATETIME('YYYY-MM-DD HH:MM:SS')} literal.
   *
   * @see #valueOf(Date)
   */
  public static class DateTimeLiteral extends GqlLiteral {

    /**
     * Formats a date as {@code YYYY-MM-DD HH:MM:SS}.
     * <p>
     * NOTE: using a {@link ThreadLocal} because {@link SimpleDateFormat} instances are not threadsafe.
     */
    private static final ThreadLocal<SimpleDateFormat> gqlDateTimeFormat = ThreadLocal.withInitial(() -> {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      sdf.setTimeZone(TimeZone.getTimeZone("UTC"));  // set UTC time zone because timestamps in the datastore are UTC
      return sdf;
    });

    private Date value;

    public DateTimeLiteral(@Nonnull Date value) {
      this.value = Objects.requireNonNull(value);
    }

    /**
     * @return a GQL {@code DATETIME('YYYY-MM-DD HH:MM:SS')} literal corresponding to the given date object
     */
    public static String toGql(Date date) {
      return "DATETIME('" + gqlDateTimeFormat.get().format(date) + "')";
    }

    @Override
    public String toGql() {
      return toGql(value);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      DateTimeLiteral that = (DateTimeLiteral)o;
      return value.equals(that.value);
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }
  }


  /*
    TODO: add support for other GQL literal types:
      - DATE
      - TIME
      - KEY
      - USER
      - GEOPT
    @see https://cloud.google.com/appengine/docs/standard/python/datastore/gqlreference
   */
}
