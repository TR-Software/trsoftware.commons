/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author Alex, 8/2/2017
 */
public abstract class AStarSearch<T> {
  private T start;
  private T goal;
  private PriorityQueue<PQEntry<T>> frontier = new PriorityQueue<>();
  private HashMap<T, T> cameFrom = new HashMap<>();
  private HashMap<T, Double> costSoFar = new HashMap<>();
  private int numNodesExamined;

  public AStarSearch(T start, T goal) {
    this.start = start;
    this.goal = goal;
    frontier.add(new PQEntry<>(start, 0));
    cameFrom.put(start, null);
    costSoFar.put(start, 0d);
  }


  /**
   * @return an optimal path from {@link #start} to {@link #goal}, or an empty list if there is no path.
   */
  public LinkedList<T> search() {
    while (!frontier.isEmpty()) {
      T current = frontier.poll().value;
      if (current.equals(goal))
        break;
      numNodesExamined++;
      for (T next : neighbors(current)) {
        double newCost = costSoFar.get(current) + cost(current, next);
        if (Double.isFinite(newCost) && (!costSoFar.containsKey(next) || newCost < costSoFar.get(next))) {
          /*
           NOTE: this if stmt adds the node for further examination iff:
             1) It's reachable, as checked by Double.isFinite(newCost)
             2) wasn't already evaluated or has a lower cost than what we've seen before
           */
          costSoFar.put(next, newCost);
          double priority = newCost + heuristic(goal, next);
          frontier.add(new PQEntry<>(next, priority));
          cameFrom.put(next, current);
        }
      }
    }
    // now reconstruct the path
    LinkedList<T> path = new LinkedList<>();
    if (cameFrom.containsKey(goal)) {
      // if cameFrom doesn't contain goal, then we didn't find a path (this method will return an empty list)
      T next = goal;
      while (next != null) {
        path.addFirst(next);
        next = cameFrom.get(next);
      }
    }
    return path;
  }

  public int getNumNodesExamined() {
    return numNodesExamined;
  }

  protected abstract List<T> neighbors(T node);

  /**
   * @param a a node in the graph
   * @param b a neighbor of a
   * @return the cost of going from a to b (assuming this is a weighted graph)
   */
  protected abstract double cost(T a, T b);

  protected abstract double heuristic(T a, T b);

  public static class PQEntry<T> implements Comparable<PQEntry> {
    private T value;
    private double priority;

    public PQEntry(T value, double priority) {
      this.value = value;
      this.priority = priority;
    }

    public T getValue() {
      return value;
    }

    public double getPriority() {
      return priority;
    }

    @Override
    public int compareTo(PQEntry o) {
      return Double.compare(this.priority, o.priority);
    }

    @Override
    public String toString() {
      return String.valueOf(priority) + ": " + value;
    }
  }
}
