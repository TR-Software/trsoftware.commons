package solutions.trsoftware.commons.server.io.csv;

import solutions.trsoftware.commons.client.util.callables.Function1;

import java.util.Map;

/**
 * Mar 15, 2010
 *
 * @author Alex
 */
public class CSVObjectBinder<T> extends CSVObjectBinderBase<T> {
  private final Map<String, Function1<String, Object>> fieldParsers;
  private final Map<String, Function1<Object, String>> fieldSerializers;

  public CSVObjectBinder(Class<T> type, String[] fieldNames, Map<String, Function1<String, Object>> fieldParsers, Map<String, Function1<Object, String>> fieldSerializers) {
    super(type, fieldNames);
    this.fieldParsers = fieldParsers;
    this.fieldSerializers = fieldSerializers;
  }

  public Object fieldFromString(String name, String value) {
    return fieldParsers.get(name).call(value);
  }

  public String fieldToString(String name, Object value) {
    return fieldSerializers.get(name).call(value);
  }
}
