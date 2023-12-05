package solutions.trsoftware.commons.shared.util.graphs;

import org.openjdk.jmh.infra.Blackhole;
import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.annotations.ExcludeFromSuite;
import solutions.trsoftware.commons.shared.util.StringUtils;
import solutions.trsoftware.commons.shared.util.graphs.PathSearchBenchmark.GridSize;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static solutions.trsoftware.commons.shared.util.graphs.PathSearchBenchmark.BenchmarkConfig;

/**
 * @author Alex
 * @since 11/12/2023
 */
@ExcludeFromSuite
public class PathSearchBenchmarkTest extends BaseTestCase {

  private PathSearchBenchmark benchmark;

  public void testBenchmarkConfig() throws Exception {
    benchmark = new PathSearchBenchmark();
    for (GridSize gridSize : GridSize.values()) {
      BenchmarkConfig config = new BenchmarkConfig(gridSize);
      config.setUp();
      Blackhole blackhole = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");

      System.out.println(StringUtils.repeat('=', 100));
      System.out.println(config);
      System.out.println(StringUtils.repeat('=', 100));
      Arrays.stream(gridSize.gridSpec).forEach(System.out::println);

      // test all the search methods with the above config

      printHeader(" aStar ", '-');
      invokeBenchmarkMethod(config, blackhole, benchmark::aStar, AStarTest::printSearchResult);

      printHeader(" aStarMultiPath ", '-');
      invokeBenchmarkMethod(config, blackhole, benchmark::aStarMultiPath, AStarTest::printSearchResult);

      printHeader(" dijkstra ", '-');
      invokeBenchmarkMethod(config, blackhole, benchmark::dijkstra, DijkstraTest::printAbridgedSearchResult);

      printHeader(" dijkstraMultiPath ", '-');
      invokeBenchmarkMethod(config, blackhole, benchmark::dijkstraMultiPath, DijkstraTest::printAbridgedSearchResult);
    }
  }

  private <T, R extends PathSearchResult<T>> void invokeBenchmarkMethod(
      BenchmarkConfig config, Blackhole blackhole,
      BiFunction<BenchmarkConfig, Blackhole, List<R>> method,
      Consumer<R> printResultMethod) {
    List<R> results = method.apply(config, blackhole);
    printSearchResults(results, printResultMethod);
  }

  private <T, R extends PathSearchResult<T>> void printSearchResults(List<R> results, Consumer<R> printResultMethod) {
    for (R result : results) {
      printResultMethod.accept(result);
      System.out.println();
    }
  }

  private void printHeader(String headerText, char pad) {
    System.out.println(StringUtils.padCenter(headerText, 80, pad));
  }

}