package solutions.trsoftware.commons.server.memquery.util;

import solutions.trsoftware.commons.server.memquery.aggregations.Aggregation;
import solutions.trsoftware.commons.server.memquery.aggregations.Avg;
import solutions.trsoftware.commons.server.memquery.aggregations.ColStats;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * @author Alex, 1/13/14
 */
public abstract class AggregationUtils {

  public static Class getAggregationValueType(Class<? extends Aggregation> cls) {
//    Type genericSuperclass = cls.getGenericSuperclass();
//      if (genericInterface instanceof ParameterizedType) {
//        ParameterizedType parameterizedInterface = (ParameterizedType)genericInterface;
//        if (parameterizedInterface.getRawType() == targetInterface)
//          return parameterizedInterface.getActualTypeArguments();
//      }
//    }

    Class<?> superCls = cls.getSuperclass();
    Class parametrizedCls = cls;
    if (superCls == ColStats.class)
      parametrizedCls = superCls;
    return getAggregationValueTypeHelper(parametrizedCls);
  }

  private static Class getAggregationValueTypeHelper(Class cls) {
    return (Class)((ParameterizedTypeImpl)cls.getGenericSuperclass()).getActualTypeArguments()[0];
  }

  public static Class<? extends Aggregation> getMeanFor(Class type) {
    if (Number.class.isAssignableFrom(type))
      return Avg.class;
    throw new IllegalArgumentException(String.format("Mean aggregation for type %s not supported", type.getSimpleName()));
  }
}
