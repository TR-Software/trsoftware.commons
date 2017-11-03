package solutions.trsoftware.commons.client.benchmarks;

import com.google.gwt.benchmarks.client.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * This benchmarks checks for the best Map implementation in web mode
 * (results for IE7 show that LHM and HM are about the same, while TreeMap
 * is much slower for all key types, especially strings).
 */
public class HashMapBenchmark extends Benchmark {
  // NOTE: must increase the slow script warning threshold for IE in order for this benchmark to work in web mode

  // NOTE: fields used as parameter ranges (with @RangeField annotation) must not be private
  IntRange iterationsRange = new IntRange(100, 1000, Operator.ADD, 100);  // this many numbers will be added to the map

  enum MapType {
    _HashMap {
      public Map newMap() {
        return new HashMap();
      }},
    _LinkedHashMap {
      public Map newMap() {
        return new LinkedHashMap();
      }},
    _TreeMap {
      public Map newMap() {
        return new TreeMap();
      }};
    public abstract Map newMap();
  }

  private Map map;

  @Override
  public String getModuleName() {
    return "solutions.trsoftware.commons.TestCommons";
  }




  public void beginHashMapVsTreeMapIntKeys(@RangeField("iterationsRange") Integer iterations, @RangeEnum(MapType.class) MapType mapType) {
    map = mapType.newMap();
  }
  public void testHashMapVsTreeMapIntKeys(@RangeField("iterationsRange") Integer iterations, @RangeEnum(MapType.class) MapType mapType) {
    for (int i = 1; i <= iterations; i++) {
      map.put(i, i+1);
      assertEquals((Integer)i+1, this.map.get(i));
    }
  }
  // Required for JUnit
  public void testHashMapVsTreeMapIntKeys() {
  }


  public void beginHashMapVsTreeMapLongKeys(@RangeField("iterationsRange") Integer iterations, @RangeEnum(MapType.class) MapType mapType) {
    map = mapType.newMap();
  }
  public void testHashMapVsTreeMapLongKeys(@RangeField("iterationsRange") Integer iterations, @RangeEnum(MapType.class) MapType mapType) {
    for (long i = 1; i <= iterations; i++) {
      map.put(i, i+1);
      assertEquals((Long)i+1, this.map.get(i));
    }
  }
  // Required for JUnit
  public void testHashMapVsTreeMapLongKeys() {
  }



  public void beginHashMapVsTreeMapStringKeys(@RangeField("iterationsRange") Integer iterations, @RangeEnum(MapType.class) MapType mapType) {
    map = mapType.newMap();
  }
  public void testHashMapVsTreeMapStringKeys(@RangeField("iterationsRange") Integer iterations, @RangeEnum(MapType.class) MapType mapType) {
    for (int i = 1; i <= iterations; i++) {
      String key = "key_asdf_" + i + "_qwertyuiopasdfqwezxcvzxcvbasdc";  // make the string reasonably long so the hashCode calculation makes a difference
      Integer value = i + 1;
      map.put(key, value);
      assertEquals(value, this.map.get(key));
    }
  }
  // Required for JUnit
  public void testHashMapVsTreeMapStringKeys() {
  }


}