package solutions.trsoftware.commons.server.management.monitoring;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.annotations.Slow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static solutions.trsoftware.commons.server.management.monitoring.SystemLoadStatType.*;

/**
 * @author Alex
 * @since 1/14/2019
 */
public class SystemLoadProfilerTest extends TestCase {

  private SystemLoadProfiler memoryProfiler;
  private SystemLoadProfiler cpuProfiler;
  private SystemLoadProfiler defaultProfiler;
  private int nObjects = 10_000_000;

  public void setUp() throws Exception {
    super.setUp();
    memoryProfiler = new SystemLoadProfiler(HEAP_USED, HEAP_COMMITTED);
    memoryProfiler.start(1, TimeUnit.MILLISECONDS);
    cpuProfiler = new SystemLoadProfiler(CPU);
    cpuProfiler.start(1, TimeUnit.MILLISECONDS);
    defaultProfiler = new SystemLoadProfiler();
    defaultProfiler.start(1, TimeUnit.MILLISECONDS);
    
  }

  @Override
  protected void tearDown() throws Exception {
    memoryProfiler.stop();
//    System.out.println(memoryProfiler.printSummary());
    memoryProfiler = null;
    cpuProfiler.stop();
//    System.out.println(cpuProfiler.printSummary());
    cpuProfiler = null;
    defaultProfiler.stop();
    System.out.println(defaultProfiler.printSummary());
    defaultProfiler = null;
    super.tearDown();
  }

  public void testSampling() throws Exception {
    List<Vector3d<Integer>> data = allocateObjects(100_000, () -> new Vector3dImpl<>(1, 2, 3));
  }

  @Slow
  public void testProfilingWrapperAllocation() throws Exception {
    List<Vector3d<Integer>> data = allocateObjects(nObjects, () -> new Vector3dImpl<>(1, 2, 3));
  }

  @Slow
  public void testProfilingPrimitiveAllocation() throws Exception {
    List<Vector3d<Integer>> data = allocateObjects(nObjects, () -> new PrimitiveIntVector3d(1, 2, 3));
  }

  public static <T> List<T> allocateObjects(int n, Supplier<T> supplier) {
    ArrayList<T> data = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      data.add(supplier.get());
    }
    return data;
  }


  public interface Vector3d<T> {
    T get(int i);
  }

  public static class Vector3dImpl<T> implements Vector3d<T> {
    private Object[] arr = new Object[3];

    public Vector3dImpl(T x, T y, T z) {
      arr[0] = x;
      arr[1] = y;
      arr[2] = z;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int i) {
      return (T)arr[i];
    }
  }

  public static class PrimitiveIntVector3d implements Vector3d<Integer> {
    private int x, y, z;

    public PrimitiveIntVector3d(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    @Override
    public Integer get(int i) {
      switch (i) {
        case 0: return x;
        case 1: return y;
        case 2: return z;
        default: throw new IllegalArgumentException();
      }
    }
  }



}