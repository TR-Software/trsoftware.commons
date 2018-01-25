package solutions.trsoftware;

import junit.framework.TestCase;

import java.sql.Timestamp;
import java.util.Date;

/**
 * This class can be used to test the run-time behavior various Java features which might be ambiguous.
 *
 * @author Alex
 * @since 11/29/2017
 */
public class RuntimeTest extends TestCase {

  /**
   * Tests what happens when there is an overloaded method for a more general type and one for a more specific type.
   */
  public void testPolymorphism() throws Exception {
    class PolyTester {
      private Date date;

      public void setDate(Date date) {
        Class<? extends Date> dateClass = date.getClass();
        System.out.printf("setter for %s invoked with instance of %s%n", Date.class.getName(), dateClass.getName());
        this.date = date;
      }

      public void setDate(Timestamp date) {
        Class<? extends Timestamp> dateClass = date.getClass();
        System.out.printf("setter for %s invoked with instance of %s%n", Timestamp.class.getName(), dateClass.getName());
        this.date = date;
      }
    }

    Date d = new Date();
    Timestamp t = new Timestamp(System.currentTimeMillis());
    PolyTester polyTester = new PolyTester();
    polyTester.setDate(d);  // the Date method should be invoked
    polyTester.setDate(t);  // the Timestamp method should be invoked
    polyTester.setDate((Date)t);  // the Date method should be invoked if we up-cast a Timestamp to Date
  }

}
