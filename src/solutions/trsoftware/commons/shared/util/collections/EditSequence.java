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

package solutions.trsoftware.commons.shared.util.collections;

import solutions.trsoftware.commons.client.util.CollectionUtils;
import solutions.trsoftware.commons.client.util.Levenshtein;
import solutions.trsoftware.commons.client.util.ListUtils;
import solutions.trsoftware.commons.client.util.LogicUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Generalizes {@link Levenshtein.EditSequence} to lists with elements of any type (rather than just {@link String}s)
 *
 * @author Alex, 4/12/2016
 */
public abstract class EditSequence<T> implements Iterable<EditSequence.EditOperation<T>>, Comparable<EditSequence<T>> {

  /** Compares two sequences by their lengths */
  public int compareTo(EditSequence o) {
    return length() - o.length();
  }

  @Override
  public String toString() {
    return getOperations().toString();
  }

  /**
   * Applies the operations represented by this instance to a copy of the given collection.
   * @param input The collection to transform using this edit sequence.
   * @return the result after the transformations were applied.
   */
  public List<T> transformCopy(Collection<T> input) {
    ArrayList<T> result = new ArrayList<T>(input);
    transform(result);
    return result;
  }

  /**
   * Transforms the given collection (in-place) using the operations represented by this instance.
   * @param input The list to transform using this edit sequence.
   */
  public void transform(List<T> input) {
    for (EditOperation<T> op : this) {
      op.apply(input);
    }
  }

  public Iterator<EditOperation<T>> iterator() {
    return getOperations().iterator();
  }

  /**
   * @return length of the edit sequence, which is equal to the Levenshtein
   * distance if this is the optimal sequence.
   */
  public abstract List<EditOperation<T>> getOperations();

  /**
   * @return length of the edit sequence, which is equal to the Levenshtein
   * distance if this is the optimal sequence.
   */
  public abstract int length();

  /**
   * Returns an analogous edit sequence, but with each operation
   * shifted n positions to the right.
   */
  public EditSequence<T> shift(int n) {
    return new ShiftedEditSequence<T>(this, n);
  }

  /**
   * Represents either inserting, deleting, or substituting a particular character
   * at a specific position.
   */
  public static abstract class EditOperation<T> {
    protected int pos;
    protected T value;

    protected EditOperation(int pos, T value) {
      this.pos = pos;
      this.value = value;
    }

    public abstract List<T> apply(List<T> seq);

    public abstract String getPrettyName();

    @Override
    public String toString() {
      return getPrettyName() + "(" + pos + ", " + value + ")";
    }

    public T getValue() {
      return value;
    }

    public int getPosition() {
      return pos;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      EditOperation that = (EditOperation)o;

      if (pos != that.pos) return false;
      if (value != null ? !value.equals(that.value) : that.value != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = pos;
      result = 31 * result + (value != null ? value.hashCode() : 0);
      return result;
    }
  }

  public static class Insertion<T> extends EditOperation<T> {
    public Insertion(int pos, T value) {
      super(pos, value);
    }
    public List<T> apply(List<T> seq) {
      seq.add(pos, value);
      return seq;
    }
    public String getPrettyName() {
      return "+";
    }
  }

  public static class Deletion<T> extends EditOperation<T> {
    public Deletion(int pos, T value) {
      super(pos, value);
    }
    public List<T> apply(List<T> seq) {
      seq.remove(pos);
      return seq;
    }
    public String getPrettyName() {
      return "-";
    }
  }

  public static class Substitution<T> extends EditOperation<T> {
    public Substitution(int pos, T value) {
      super(pos, value);
    }
    public List<T> apply(List<T> seq) {
      seq.set(pos, value);
      return seq;
    }
    public String getPrettyName() {
      return "$";
    }
  }



  static class ArrayListEditSequence<T> extends EditSequence<T> {
    protected ArrayList<EditOperation<T>> operations;
    ArrayListEditSequence(ArrayList<EditOperation<T>> operations) {
      this.operations = operations;
    }
    public List<EditOperation<T>> getOperations() {
      return operations;
    }
    public int length() {
      return operations.size();
    }
  }


  static class ShiftedEditSequence<T> extends ArrayListEditSequence<T> {
    /**
     * Creates a copy of the given sequence but with each operation right-shifted
     * by the given number of places.
     */
    ShiftedEditSequence(EditSequence<T> baseSequence, int shift) {
      this(baseSequence.getOperations(), shift);
    }
    /**
     * Creates a copy of the given sequence but with each operation right-shifted
     * by the given number of places.
     */
    ShiftedEditSequence(List<EditOperation<T>> baseSequenceOps, int shift) {
      super(new ArrayList<EditOperation<T>>(baseSequenceOps.size()));
      for (EditOperation<T> op : baseSequenceOps) {
        int shiftedPos = op.pos + shift;
        if (op instanceof Deletion)
          operations.add(new Deletion<T>(shiftedPos, op.value));
        else if (op instanceof Insertion)
          operations.add( new Insertion<T>(shiftedPos, op.value));
        else {
          assert op instanceof Substitution;
          operations.add(new Substitution<T>(shiftedPos, op.value));
        }
      }
    }
  }

  /** Immutable edit sequence implemented using a linked structure. */
  private static class LinkedEditSequence<T> extends EditSequence<T> {
    // Benchmark Results (Intel Core 2 Duo T9300):
    // sequence(lengths 683, 823) took 250 ms.
    // sequence(lengths 823, 683) took 140 ms.

    private EditSequence<T> prev;
    private EditOperation<T> op;
    private int length;

    /** Copy constructor, extending the given sequence with the given operation */
    private LinkedEditSequence(EditSequence<T> s, EditOperation<T> op) {
      prev = s;
      this.op = op;
      length = s.length() + 1;
    }

    @Override
    public int length() {
      return length;
    }

    @Override
    public List<EditOperation<T>> getOperations() {
      ArrayList<EditOperation<T>> ret = ListUtils.fill(new ArrayList<EditOperation<T>>(length), length, null);
      EditSequence<T> cursor = this;
      // to avoid too much recursion (stack overflow, go as far back as possible using iteration
      int i;
      for (i = length-1; i >= 0; i--) {
        if (cursor instanceof LinkedEditSequence) {
          LinkedEditSequence<T> linkedCursor = (LinkedEditSequence<T>)cursor;
          ret.set(i, linkedCursor.op);
          cursor = linkedCursor.prev;
        }
        else
          break;
      }
      if (i >= 0) {
        // if reached the beginning i would have been -1
        // didn't reach the beginning because cursor isn't a LinkedEditSequence, must use recursion now
        List<EditOperation<T>> priorOps = cursor.getOperations();
        // append what we achieved above to the recursive result
        for (i = i+1; i < ret.size(); i++) {
          priorOps.add(ret.get(i));
        }
        return priorOps;
      }
      else
        return ret;
    }
  }

  /**
   * This class uses less memory than {@link LinkedEditSequence} for long edit sequences consisting of the same operation.
   */
  private static abstract class UniformSequence<T> extends EditSequence<T> {
    private final List<T> input;

    /**
     * Creates a sequence to construct the given string from scratch using
     * repeated insertions, or a sequence to go from the given
     * string to the empty string via repeated deletions.
     */
    private UniformSequence(List<T> input) {
      this.input = input;
    }

    protected abstract EditOperation<T> makeOp(int pos, T value);

    public List<EditOperation<T>> getOperations() {
      ArrayList<EditOperation<T>> ret = new ArrayList<EditOperation<T>>(length());
      for (int i = 0; i < length(); i++) {
        ret.add(makeOp(i, input.get(i)));
      }
      return ret;
    }

    public int length() {
      return input.size();
    }
  }

  private static class InsertionSequence<T> extends UniformSequence<T> {
    /** Creates a sequence to construct the given list from scratch using repeated insertions. */
    InsertionSequence(List<T> input) {
      super(input);
    }

    protected EditOperation<T> makeOp(int pos, T value) {
      return new Insertion<T>(pos, value);  // always insert at the end
    }
  }

  private static class DeletionSequence<T> extends UniformSequence<T> {
    /** Creates a sequence to go from the given list to an empty list via repeated deletions. */
    private DeletionSequence(List<T> input) {
      super(input);
    }

    protected EditOperation<T> makeOp(int pos, T value) {
      return new Deletion<T>(0, value);  // always delete at the beginning
    }
  }

  public static <T> boolean equal(T a, T b) {
    // TODO: impl this properly (allow passing custom equality testers)
    return LogicUtils.eq(a, b);
  }


  /**
   * Computes the edit sequence that transforms the first list into the second list, corresponding to the Levenshtein
   * distance between the two.
   * @return The shortest edit sequence to transform s into t (the length of this edit sequence is the Levenshtein distance
   * between the two inputs).
   * @throws NullPointerException if either list is null
   * @throws OutOfMemoryError if the lists are too long (this method uses O(n^2) memory)
   * @see <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">Wikipedia article on Levenshtein distance</a>
   */
  public static <T> EditSequence<T> create(List<T> s, List<T> t) {
    // NOTE: see http://en.wikipedia.org/wiki/Levenshtein_distance to learn how the algorithm works
    // this implementation differs from the simple implementation given by Wikipedia in 2 ways:
    // 1) only the first two rows of the matrix are kept in memory (to use O(md) <= O(n^2) space instead of O(mdn) <= O(n^3) space, where n is the length of the longest input string, m is the length of the shortest input, and d is the edit distance)
    //    - if we didn't need to keep the edit histories (i.e. just wanted the distance), we'd need only O(m) space, but with edit histories, can't do better than O(n^2) worst case, without getting fancy (see http://stackoverflow.com/questions/4057513/levenshtein-distance-algorithm-better-than-onm for ideas)
    //    - either way, the run time is O(nm) <= O(n^2)
    // 2) we store the best edit sequence along with the best distance in each cell of the matrix

    int n = s.size();
    int m = t.size();

    // base case optimizations
    if (n == 0)
      return new InsertionSequence<T>(t);
    if (m == 0)
      return new DeletionSequence<T>(s);

//    System.out.println("Computing edit sequence on strings lengths " + n + "," + m + ": \"" + s + "\", \"" + t + "\"");
    // NOTE: to save memory, could put the shorter string into columns to save memory (i.e. if s < t, call d(t,s) and reverse the output)

    int width = m + 1;
    ArrayList<EditSequence<T>> p = ListUtils.fill(new ArrayList<EditSequence<T>>(width),
        width, null); // 'previous' row of the matrix
    ArrayList<EditSequence<T>> d = ListUtils.fill(new ArrayList<EditSequence<T>>(width),
        width, null); // latest row of the matrix

    // Algorithm Overview: at each step we're asking how to go from the first i elements of t to the first j elements of s
    // in the dynamic programming matrix; s is represented by rows (indices i) and t is represented by columns (indices j)

    // compute row 0: how to go from the empty list (i.e. first 0 elements of s) to each possible prefix of t
    for (int j = 0; j <= m; j++) {
      p.set(j, new InsertionSequence<T>(t.subList(0, j)));
    }

    for (int i = 1; i <= n; i++) {
      d.set(0, new DeletionSequence<T>(s.subList(0, i)));  // how to go from the first i elements of s to the empty list (first 0 elts of t)
      for (int j = 1; j <= m; j++) {
        int jMinus1 = j - 1; // extracted frequently used calculation to speed things up
        T s_i = s.get(i - 1);
        T t_j = t.get(jMinus1);
        // we are looking for the best way to go from s_i to t_j
        if (equal(s_i, t_j)) {
          d.set(j, p.get(jMinus1)); // already equal, so no operation required
          continue;
        }
        // we want the best operation we can make at this point, the cost of which will be 1 + the cost of the previous best operation
        // to determine this, we'll need to consider the best sequences up to this point
        EditSequence<T> del = p.get(j);      // if we're gonna delete, the best previous sequence is stored in the cell directly above
        EditSequence<T> ins = d.get(jMinus1);  // if we're gonna insert, the best previous sequence is stored in the cell directly to the left
        EditSequence<T> sub = p.get(jMinus1);  // if we're gonna substitute, the best previous sequence is stored in the cell diagonally up and to the left
        // so which one is the best (lowest cost)? let's find out:
        EditSequence<T> best;
        EditOperation<T> op;  // which operation brings us to the best sequence?
        int insLen = ins.length();  // extracting frequently accessed data to speed things up
        int delLen = del.length();
        int subLen = sub.length();
        if (insLen <= delLen && insLen <= subLen) {
          best = ins;
          op = new Insertion<T>(jMinus1, t_j);
        }
        else if (delLen <= insLen && delLen <= subLen) {
          best = del;
          op = new Deletion<T>(j, s_i);
        }
        else {
          best = sub;
          op = new Substitution<T>(jMinus1, t_j);
        }
        d.set(j, new LinkedEditSequence<T>(best, op));
      }
      // swap the last row and previous row references
      ArrayList<EditSequence<T>> temp = p; // placeholder to assist in swapping p and d
      p = d;
      d = temp;  // reuse the old array for the next row to avoid extra allocation
    }

    // our last action in the above loop was to switch d and p, so p now
    // actually has the most recent cost counts
    return p.get(m);
  }

  /**
   * @see #create(List, List)
   */
  public static <T> EditSequence<T> create(Iterator<T> s, Iterator<T> t) {
    return create(CollectionUtils.asList(s), CollectionUtils.asList(t));
  }

//
//  /**
//   * A version of editSequence optimized for strings sharing a common prefix and/or suffix.
//   * Since the time of the algorithm is O(n^2), anything that reduces n helps.
//   * @param commonPrefixPossible pass true to activate the common prefix optimization
//   * @param commonSuffixPossible pass true to activate the common suffix optimization
//   * @return the edit sequence transforming s into t
//   */
//  public static <T> EditSequence<T> editSequence(List<T> s, List<T> t, boolean commonPrefixPossible, boolean commonSuffixPossible) {
//    // if the strings share a common prefix and/or suffix, we can speed up the
//    // algorithm by first stripping those off and accounting for the prefix
//    // later (the suffix has no bearing on the edit sequence)
//    String[] strings = new String[]{s, t};
//    if (commonSuffixPossible)
//      stripCommonSuffix(strings); // we can forget about the suffix now, since it has no bearing on the output
//    int prefixLen = 0;
//    if (commonPrefixPossible)
//      prefixLen = stripCommonPrefix(strings);
//    EditSequence sequence = editSequence(strings[0], strings[1]);
//    if (prefixLen > 0)
//      sequence = sequence.shift(prefixLen); // we need to shift all the indices in the edit sequence by the length of the common prefix
//    return sequence;
//  }
//
//  /**
//   * @return the length of the common prefix, 0 if none.
//   */
//  static <T> int stripCommonPrefix(List<T> s, List<T> t) {
//    // method exposed for unit testing
//    String prefix = StringUtils.commonPrefix(s, t);
//    int prefixLen = prefix.length();
//    if (prefixLen > 0) {
//      // remove the shared prefix from the strings; we'll account for it later
//      strings[0] = s.substring(prefixLen);
//      strings[1] = t.substring(prefixLen);
//    }
//    return prefixLen;
//  }
//
//  /**
//   * @param strings: an array of 2 strings.
//   * @return the length of the common suffix, 0 if none.
//   */
//  public static int stripCommonSuffix(String[] strings) {
//    // method exposed for unit testing
//    String s = strings[0], t = strings[1];
//    String suffix = StringUtils.commonSuffix(s, t);
//    int suffixLen = suffix.length();
//    if (suffixLen > 0) {
//      // the suffix can be safely ignored without affecting the edit sequence
//      strings[0] = s.substring(0, s.length() - suffixLen);
//      strings[1] = t.substring(0, t.length() - suffixLen);
//    }
//    return suffixLen;
//  }
//
//  /**
//   * @return the longest common prefix shared by the two given strings,
//   * which could be the empty string.
//   */
//  public static String commonPrefix(String s, String t) {
//    int prefixLast = -1;
//    int limit = Math.min(s.length(), t.length());  // will throw NPE if either string is null (as expected)
//    for (int i = 0; i < limit; i++) {
//      if (s.charAt(i) == t.charAt(i))
//        prefixLast = i;
//      else
//        break;
//    }
//    return s.substring(0, prefixLast+1);
//  }
//
//  /**
//   * @return the longest common suffix (at the end) shared by the two given strings,
//   * which could be the empty string.
//   */
//  public static String commonSuffix(String s, String t) {
//    int sNext = s.length();
//    int tNext = t.length();
//    while (--sNext >= 0 && --tNext >= 0) {
//      if (s.charAt(sNext) != t.charAt(tNext))
//        break;
//    }
//    return s.substring(sNext+1, s.length());
//  }
//




}
