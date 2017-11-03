package solutions.trsoftware.commons.server.memquery;

import solutions.trsoftware.commons.server.memquery.struct.NamedTuple;
import solutions.trsoftware.commons.server.memquery.struct.OrderedTuple;

import java.util.List;

/**
* @author Alex, 1/8/14
*/
public interface Row extends NamedTuple, OrderedTuple {

  RelationSchema getSchema();  // TODO: why does each row need a pointer to its schema? this wastes memory

  /** Gets multiple named values at once */
  List<Object> getValues(List<String> names);

  Object getRawData();


}
