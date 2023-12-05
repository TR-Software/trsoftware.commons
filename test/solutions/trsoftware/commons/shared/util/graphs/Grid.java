package solutions.trsoftware.commons.shared.util.graphs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import solutions.trsoftware.commons.shared.util.iterators.IndexedIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static solutions.trsoftware.commons.shared.util.MathUtils.floorMod;
import static solutions.trsoftware.commons.shared.util.RandomUtils.rnd;

/**
 * Represents a 2-dimensional grid of square cells (similar to a chessboard), allowing 4 directions
 * of movement (up, down, left, right) between the cells, with wraparound at the edges of the board.
 *
 * @author Alex, 7/30/2017
 */
public class Grid implements Iterable<Grid.Cell> {

  /* TODO: maybe remove duplication with com.typingsnake.shared.model.Board? (same with Location)
   *   - could extract those classes to Commons (as a new package for game prog utils), pushing Snake-proprietary code to subclasses
   */

  private final Cell[][] cells;
  private final int width;
  private final int height;

  /**
   * Creates a new instance based on the given row specifications.
   *
   * @param gridSpec a string representation of the desired grid, such that
   *   {@code gridSpec[y].charAt(x)} will be translated to {@code new Cell(new Location(x, y), gridSpec[y].charAt(x))}
   * @see #getStringRepr()
   */
  public Grid(String[] gridSpec) {
    width = gridSpec[0].length();
    height = gridSpec.length;
    cells = new Cell[height][width];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        cells[y][x] = new Cell(new Location(x, y), gridSpec[y].charAt(x));
      }
    }
  }

  public Grid(int width, int height, char fillChar) {
    this.width = width;
    this.height = height;
    cells = new Cell[height][width];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        cells[y][x] = new Cell(new Location(x, y), fillChar);
      }
    }

  }

  public Cell[][] getCells() {
    return cells;
  }

  public Stream<Cell> streamCells() {
    return Arrays.stream(cells).flatMap(Arrays::stream);
  }

  public Cell getCell(Location loc) {
    return getCell(loc.x, loc.y);
  }

  public Cell getCell(int x, int y) {
    return cells[y][x];
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  /**
   * @return {@link #getWidth() width} &times; {@link #getHeight() height}
   */
  public int getArea() {
    return width * height;
  }

  public Location randomLocation() {
    return cells[rnd().nextInt(height)][rnd().nextInt(width)].location;
  }

  public String[] getStringRepr() {
    String[] ret = new String[height];
    for (int y = 0; y < height; y++) {
      StringBuilder row = new StringBuilder();
      for (int x = 0; x < width; x++) {
        row.append(cells[y][x].value);
      }
      ret[y] = row.toString();
    }
    return ret;
  }

  public List<Cell> getAdjacentCells(Location location) {
    return getAdjacentCells(location.x, location.y);
  }

  private ImmutableList<Cell> getAdjacentCells(int x, int y) {
    return ImmutableList.of(
        cells[(height + y - 1) % height][x], // up
        cells[(height + y + 1) % height][x], // down
        cells[y][(width + x - 1) % width],   // left
        cells[y][(width + x + 1) % width]    // right
    );
  }


  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(width * height + height);
    for (String row : getStringRepr()) {
      sb.append(row).append('\n');
    }
    return sb.toString();
  }

  /**
   * @return the Manhattan distance between a and b
   */
  public int distance(Location a, Location b) {
    int xDist = Math.min(
        floorMod(b.x - a.x, width),
        floorMod(a.x - b.x, width));  // the lesser distance of moving left vs. moving right
    int yDist = Math.min(
        floorMod(b.y - a.y, height),
        floorMod(a.y - b.y, height));  // the lesser distance of moving up vs. moving down
    return xDist + yDist;
  }

  @Override
  public Iterator<Cell> iterator() {
    return new IndexedIterator<Cell>(width*height) {
      @Override
      protected Cell get(int idx) {
        int x = idx % width;
        int y = idx / height;
        return cells[y][x];
      }
    };
  }


  public static class Cell {
    private final Location location;
    private char value;

    public Cell(Location location, char value) {
      this.location = location;
      this.value = value;
    }

    public Location getLocation() {
      return location;
    }

    public char getValue() {
      return value;
    }

    @VisibleForTesting
    public void setValue(char value) {
      this.value = value;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("{");
      sb.append(location);
      sb.append(", '").append(value);
      sb.append("'}");
      return sb.toString();
    }
  }
}
