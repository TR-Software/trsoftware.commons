package solutions.trsoftware.commons.server.io.csv;

import solutions.trsoftware.commons.client.util.MapUtils;
import solutions.trsoftware.commons.client.util.callables.Function1;

/**
 * Mar 15, 2010
 *
 * @author Alex
 */
public class CSVObjectBinderTest extends CSVObjectBinderBaseTest {

  @Override
  protected void setUp() throws Exception {
    myClassBinder = new CSVObjectBinder<MyClass>(MyClass.class, new String[]{"foo", "bar"},
        MapUtils.<String, Function1<String, Object>>hashMap(
            "foo",
            new Function1<String, Object>() {
              public Object call(String arg) {
                return new Integer(arg);
              }
            },
            "bar",
            new Function1<String, Object>() {
              public Object call(String arg) {
                return Float.parseFloat(arg);
              }
            }),
        MapUtils.<String, Function1<Object, String>>hashMap(
            "foo",
            new Function1<Object, String>() {
              public String call(Object arg) {
                return String.valueOf(arg);
              }
            },
            "bar",
            new Function1<Object, String>() {
              public String call(Object arg) {
                return String.valueOf(arg);
              }
            }
        ));
  }


}