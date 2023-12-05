package solutions.trsoftware.commons.shared.util.graphs;

import solutions.trsoftware.commons.shared.BaseTestCase;
import solutions.trsoftware.commons.shared.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alex
 * @since 10/11/2023
 */
public abstract class PathSearchTestCase extends BaseTestCase {

  protected GraphSpec graphSpec;

  public static <T> void printPaths(Collection<List<T>> shortestPaths, String indent) {
    if (shortestPaths != null) {
      shortestPaths.stream()
          .sorted(CollectionUtils.lexicographicOrder(Comparator.comparing(T::toString)))
          .map(path -> indent + path)
          .forEach(System.out::println);
    }
    else
      System.out.println("\t\t" + null);
  }

  @Override
  public void tearDown() throws Exception {
    graphSpec = null;
    super.tearDown();
  }

  public static Grid generateGrid(String[] gridSpec) {
    Arrays.stream(gridSpec).forEach(System.out::println);
    return new Grid(gridSpec);
  }

  protected static class GraphSpec implements solutions.trsoftware.commons.shared.util.graphs.GraphSpec<Location> {

    private final Grid grid;

    protected GraphSpec(Grid grid) {
      this.grid = grid;
    }

    @Override
    public Iterable<Location> neighbors(Location node) {
      return grid.getAdjacentCells(node).stream().map(Grid.Cell::getLocation).collect(Collectors.toList());
    }

    @Override
    public double cost(Location a, Location b) {
      if (grid.getCell(b).getValue() == '#')
        return Double.POSITIVE_INFINITY;  // # represents an obstacle, which means can't move there
      else
        return 1;  // otherwise, movement cost to any adjacent cell is always 1
    }

    @Override
    public double heuristic(Location a, Location b) {
      return grid.distance(a, b);
    }
  }
}
