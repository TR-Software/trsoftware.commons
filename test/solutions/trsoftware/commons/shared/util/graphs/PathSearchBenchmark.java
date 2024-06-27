/*
 * Copyright 2022 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package solutions.trsoftware.commons.shared.util.graphs;

import com.google.common.base.MoreObjects;
import com.google.gwt.core.shared.GwtIncompatible;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import solutions.trsoftware.commons.shared.util.text.CharRange;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Alex
 * @since 4/29/2022
 */
@GwtIncompatible
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1)
@Fork(value = 1, warmups = 1)
@Measurement(time = 3)
//@Threads(4)
public class PathSearchBenchmark {

  public enum GridSize {
    SMALL(new String[]{
        // example from AStarTest.testSearchWithMultiplePaths:
        "X...",
        "#a#.",
        "###.",
        ".X#.",
        "....",
        "....",
        "..b.",
    }),
    /** 15x15 grid */
    MEDIUM(new String[]{
        // example generated with GameTest.testGameplay
        "......#........",
        "......#........",
        ".####.#........",
        "....#.#........",
        "X...###........",
        "#........##....",
        "#......#.######",
        ".......###.....",
        "........##.....",
        "......#########",
        "...##.###.X....",
        "#######........",
        ".....##........",
        ".....###b......",
        "....a####......",
    }),
    /** 30x20 grid */
    LARGE(new String[]{
        // example generated with GameTest.testGameplay
        "..............................",
        "..##..........................",
        "...#.##.......................",
        "...#.##.......................",
        "...#.##.............X.........",
        "...#.##.......................",
        "...####.......................",
        "...####.......................",
        "...######.....................",
        "#########....................#",
        "....################.........#",
        "....####......######.........#",
        "....####...####...##.........#",
        "....####...#......##a........#",
        "########...#......###........#",
        ".......#####.....X###.........",
        "..................#b..........",
        "..................##..........",
        "..............................",
        "..............................",
    }),
    ;

    final String[] gridSpec;

    GridSize(String[] gridSpec) {
      this.gridSpec = gridSpec;
    }

    public String[] getGridSpec() {
      return gridSpec;
    }
  }

  @State(Scope.Benchmark)
  public static class BenchmarkConfig {
    @Param
    GridSize size;
//    GridSize size = GridSize.SMALL;

    PathSearchTestCase.GraphSpec graphSpec;
    List<Location> start = new ArrayList<>();
    Set<Location> goals = new HashSet<>();

    public BenchmarkConfig() {
    }

    public BenchmarkConfig(GridSize size) {
      this.size = size;
    }

    @Setup
    public void setUp() {
      String[] gridSpec = size.gridSpec;
      Grid grid = new Grid(gridSpec);
      graphSpec = new PathSearchTestCase.GraphSpec(grid);
      CharRange startPosChars = new CharRange('a', 'z');
      grid.streamCells().forEach(cell -> {
        char c = cell.getValue();
        if (c == 'X')  // locations marked with "X" on gridSpec
          goals.add(cell.getLocation());
        else if (startPosChars.contains(c))
          start.add(cell.getLocation());
      });
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("size", size)
          .add("start", start)
          .add("goals", goals)
          .toString();
    }
  }

  @Benchmark
  public List<AStarSearchResult<Location>> aStar(BenchmarkConfig config, Blackhole blackhole) {
    return search(config, blackhole,
        new AStar<>(config.graphSpec));
  }

  @Benchmark
  public List<AStarMultiPathResult<Location>> aStarMultiPath(BenchmarkConfig config, Blackhole blackhole) {
    return search(config, blackhole,
        new AStarMultiPath<>(config.graphSpec));
  }

  @Benchmark
  public List<DijkstraSearchResult<Location>> dijkstra(BenchmarkConfig config, Blackhole blackhole) {
    return search(config, blackhole,
        new Dijkstra<>(config.graphSpec));
  }

  @Benchmark
  public List<DijkstraMultiPathResult<Location>> dijkstraMultiPath(BenchmarkConfig config, Blackhole blackhole) {
    return search(config, blackhole,
        new DijkstraMultiPath<>(config.graphSpec));
  }

  private <R extends AStarSearchResult<Location>> List<R> search(BenchmarkConfig config, Blackhole blackhole, AStarBase<Location, R> searchImpl) {
    List<R> results = new ArrayList<>();
    for (Location start : config.start) {
      R result = searchImpl.search(start, config.goals);
      blackhole.consume(result.getNumNodesExamined());
      blackhole.consume(result.getReachedGoal());
      blackhole.consume(result.getShortestPathCost());
      blackhole.consume(result.getShortestPath());
      results.add(result);
    }
    return results;
  }

  private <R extends DijkstraSearchResult<Location>> List<R> search(BenchmarkConfig config, Blackhole blackhole, DijkstraBase<Location, R> searchImpl) {
    List<R> results = new ArrayList<>();
    for (Location start : config.start) {
      R result = searchImpl.search(start);
      blackhole.consume(result.getNumNodesExamined());
      for (Location target : config.goals) {
        blackhole.consume(result.getShortestPathCost(target));
        blackhole.consume(result.getShortestPath(target));
      }
      results.add(result);
    }
    return results;
  }

}
