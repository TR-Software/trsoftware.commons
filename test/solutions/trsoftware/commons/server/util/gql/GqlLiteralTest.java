package solutions.trsoftware.commons.server.util.gql;

import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static solutions.trsoftware.commons.server.util.gql.GqlLiteral.*;

/**
 * @author Alex
 * @since 12/3/2019
 */
public class GqlLiteralTest extends TestCase {

  public void testStringLiteral() throws Exception {
    String expected = "'Joe''s Diner'";
    String value = "Joe's Diner";
    assertEquals(expected, StringLiteral.toGql(value));
    assertEquals(expected, new StringLiteral(value).toGql());
  }

  public void testDateTimeLiteral() throws Exception {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    Date value = simpleDateFormat.parse("2019-12-03 12:34:56");
    String expected = "DATETIME('2019-12-03 12:34:56')";
    assertEquals(expected, DateTimeLiteral.toGql(value));
    assertEquals(expected, new DateTimeLiteral(value).toGql());
  }

  public void testValueOf() throws Exception {
    // valueOf(Date):
    { 
      Date value = new Date();
      assertEquals(new DateTimeLiteral(value), valueOf(value));
      assertSame(NULL, valueOf((Date)null));
    }
    // valueOf(String):
    { 
      String value = "Joe's Diner";
      assertEquals(new StringLiteral(value), valueOf(value));
      assertSame(NULL, valueOf((String)null));
    }
    // valueOf(boolean):
    {
      assertSame(TRUE, valueOf(true));
      assertSame(FALSE, valueOf(false));
    }
  }
}