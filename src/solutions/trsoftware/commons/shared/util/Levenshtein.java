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

package solutions.trsoftware.commons.shared.util;

import solutions.trsoftware.commons.shared.util.stats.ArgMax;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static solutions.trsoftware.commons.shared.util.ListUtils.arrayList;

/**
 * Two algorithms for calculating the Levenshtein distance between two strings.
 * A slower algorithm that returns the edit sequence along with the distance,
 * and a faster algorithm (by a constant factor) that returns just the distance.
 *
 * Mar 14, 2011
 *
 * @author Alex Epshteyn
 *
 * @see <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">Wikipedia article on Levenshtein distance</a>
 */
public class Levenshtein {

  /**
   * Represents either inserting, deleting, or substituting a particular character
   * at a specific position.
   */
  public static abstract class EditOperation {
    protected int pos;
    protected char c;

    protected EditOperation(int pos, char c) {
      this.pos = pos;
      this.c = c;
    }

    public abstract StringBuilder apply(StringBuilder str);

    public abstract String getPrettyName();

    @Override
    public String toString() {
      return getPrettyName() + "(" + pos + ", " + c + ")";
    }

    public char getChar() {
      return c;
    }

    public int getPosition() {
      return pos;
    }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof EditOperation)) return false;

      EditOperation that = (EditOperation)o;

      if (c != that.c) return false;
      if (pos != that.pos) return false;
      if (getClass() != that.getClass()) return false;
      return true;
    }

    public int hashCode() {
      int result;
      result = pos;
      result = 31 * result + (int)c;
      return result;
    }
  }

  public static class Insertion extends EditOperation {
    public Insertion(int pos, char c) {
      super(pos, c);
    }
    public StringBuilder apply(StringBuilder str) {
      str.ensureCapacity(pos);
      return str.insert(pos, c);
    }
    public String getPrettyName() {
      return "+";
    }
  }

  public static class Deletion extends EditOperation {
    public Deletion(int pos, char c) {
      super(pos, c);
    }
    public StringBuilder apply(StringBuilder str) {
      return str.deleteCharAt(pos);
    }
    public String getPrettyName() {
      return "-";
    }
  }

  public static class Substitution extends EditOperation {
    public Substitution(int pos, char c) {
      super(pos, c);
    }
    public StringBuilder apply(StringBuilder str) {
      str.setCharAt(pos, c);
      return str;
    }
    public String getPrettyName() {
      return "$";
    }
  }

  public static abstract class EditSequence implements Iterable<EditOperation>, Comparable<EditSequence> {
    /** Compares two sequences by their lengths */
    public int compareTo(EditSequence o) {
      return length() - o.length();
    }

    @Override
    public String toString() {
      return getOperations().toString();
    }

    /**
     * Applies the operations represented by this instance to the given string.
     * @param s The string to transform using this sequence.
     * @return the result after the transformations were applied.
     * @throws ArrayIndexOutOfBoundsException is possible (but not guaranteed)
     * if the given string is not the same string used to compute the sequence.
     */
    public String apply(String s) {
      StringBuilder buf = new StringBuilder(s);
      for (EditOperation op : this) {
        op.apply(buf);
      }
      return buf.toString();
    }

    public Iterator<EditOperation> iterator() {
      return getOperations().iterator();
    }

    /**
     * @return length of the edit sequence, which is equal to the Levenshtein
     * distance if this is the optimal sequence.
     */
    public abstract List<EditOperation> getOperations();

    /** The length of the edit sequence (number of operations) */
    public abstract int length();

    /**
     * Returns an analogous edit sequence, but with each operation
     * shifted n positions to the right.
     */
    public EditSequence shift(int n) {
      return new ShiftedEditSequence(this, n);
    }
  }

  static class ArrayListEditSequence extends EditSequence {
    protected ArrayList<EditOperation> operations;
    ArrayListEditSequence(ArrayList<EditOperation> operations) {
      this.operations = operations;
    }
    public List<EditOperation> getOperations() {
      return operations;
    }
    public int length() {
      return operations.size();
    }
  }


  static class ShiftedEditSequence extends ArrayListEditSequence {
    /**
     * Creates a copy of the given sequence but with each operation right-shifted
     * by the given number of places.
     */
    ShiftedEditSequence(EditSequence baseSequence, int shift) {
      this(baseSequence.getOperations(), shift);
    }
    /**
     * Creates a copy of the given sequence but with each operation right-shifted
     * by the given number of places.
     */
    ShiftedEditSequence(List<EditOperation> baseSequenceOps, int shift) {
      super(new ArrayList<EditOperation>(baseSequenceOps.size()));
      for (EditOperation op : baseSequenceOps) {
        int shiftedPos = op.pos + shift;
        if (op instanceof Deletion)
          operations.add(new Deletion(shiftedPos, op.c));
        else if (op instanceof Insertion)
          operations.add( new Insertion(shiftedPos, op.c));
        else {
          assert op instanceof Substitution;
          operations.add(new Substitution(shiftedPos, op.c));
        }
      }
    }
  }

  /** Immutable edit sequence implemented using a linked structure. */
  private static class LinkedEditSequence extends EditSequence {
    // Benchmark Results (Intel Core 2 Duo T9300):
    // sequence(lengths 683, 823) took 250 ms.
    // sequence(lengths 823, 683) took 140 ms.

    private EditSequence prev;
    private EditOperation op;
    private int length;

    /** Copy constructor, extending the given sequence with the given operation */
    private LinkedEditSequence(EditSequence s, EditOperation op) {
      prev = s;
      this.op = op;
      length = s.length() + 1;
    }

    public int length() {
      return length;
    }

    /**
     * @return length of the edit sequence, which is equal to the Levenshtein
     * distance if this is the optimal sequence.
     */
    public List<EditOperation> getOperations() {
      EditOperation[] ret = new EditOperation[length];
      EditSequence cursor = this;
      // to avoid too much recursion (stack overflow, go as far back as possible using iteration
      int i;
      for (i = length-1; i >= 0; i--) {
        if (cursor instanceof LinkedEditSequence) {
          LinkedEditSequence linkedCursor = (LinkedEditSequence)cursor;
          ret[i] = linkedCursor.op;
          cursor = linkedCursor.prev;
        }
        else
          break;
      }
      if (i >= 0) {
        // if reached the beggining i would have been -1
        // didn't reach the beginning because cursor isn't a LinkedEditSequence, must use recursion now
        List<EditOperation> priorOps = cursor.getOperations();
        // append what we achieved above to the recursive result
        for (i = i+1; i < ret.length; i++) {
          priorOps.add(ret[i]);
        }
        return priorOps;
      }
      else
        return arrayList(ret);
    }
  }

  /**
   * This class uses less memory for long edit sequences consisting of the
   * same operation that LinkedEditSequence.
   */
  private static abstract class UniformSequence extends EditSequence {
    private final String str;

    /**
     * Creates a sequence to construct the given string from scratch using
     * repeated insertions, or a sequence to go from the given
     * string to the empty string via repeated deletions.
     */
    private UniformSequence(String str) {
      this.str = str;
    }

    protected abstract EditOperation makeOp(int pos, char c);

    public List<EditOperation> getOperations() {
      ArrayList<EditOperation> ret = new ArrayList<EditOperation>(length());
      for (int i = 0; i < length(); i++) {
        ret.add(makeOp(i, str.charAt(i)));
      }
      return ret;
    }

    public int length() {
      return str.length();
    }
  }

  static class InsertionSequence extends UniformSequence {
    /**
     * Creates a sequence to construct the given string from scratch using
     * repeated insertions.
     */
    InsertionSequence(String str) {
      super(str);
    }
    protected EditOperation makeOp(int pos, char c) {
      return new Insertion(pos, c);  // always insert at the end
    }
  }

  private static class DeletionSequence extends UniformSequence {
    /**
     * Creates a sequence to go from the given string to the empty string
     * via repeated deletions.
     */
    private DeletionSequence(String str) {
      super(str);
    }
    protected EditOperation makeOp(int pos, char c) {
      return new Deletion(0, c);  // always delete at the beginning
    }
  }



  /**
   * Computes the Levenshtein distance between two strings as well as the
   * corresponding edit sequence.
   *
   * @return The shortest edit sequence to transform s into t (which also
   * gives the Levenshtein distance).
   * @throws NullPointerException if either string is null
   * @throws OutOfMemoryError if the strings are too long (this method uses O(n^2) memory)
   * @see <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">Wikipedia article on Levenshtein distance</a>
   * @author Alex Epshteyn
   */
  public static EditSequence editSequence(String s, String t) {
    // NOTE: see http://en.wikipedia.org/wiki/Levenshtein_distance to learn how the algorithm works
    // this implementation differs from the simple implementaion given by Wikipedia in 2 ways:
    // 1) only the first two rows of the matrix are kept in memory (to use O(md) <= O(n^2) space instead of O(mdn) <= O(n^3) space, where n is the length of the longest input string, m is the length of the shortest input, and d is the edit distance)
    //    - if we didn't need to keep the edit histories (i.e. just wanted the distance), we'd need only O(m) space, but with edit histories, can't do better than O(n^2) worst case, without getting fancy (see http://stackoverflow.com/questions/4057513/levenshtein-distance-algorithm-better-than-onm for ideas)
    //    - either way, the run time is O(nm) <= O(n^2)
    // 2) we store the best edit sequence along with the best distance in each cell of the matrix

    int n = s.length();
    int m = t.length();

    // base case optimizations
    if (n == 0)
      return new InsertionSequence(t);
    if (m == 0)
      return new DeletionSequence(s);

//    System.out.println("Computing edit sequence on strings lengths " + n + "," + m + ": \"" + s + "\", \"" + t + "\"");
    // NOTE: to save memory, could put the shorter string into columns to save memory (i.e. if s < t, call d(t,s) and reverse the output)

    EditSequence p[] = new EditSequence[m + 1]; // 'previous' row of the matrix
    EditSequence d[] = new EditSequence[m + 1]; // latest row of the matrix

    // Algorithm Overview: at each step we're asking how to go from the first i chars of t to the first j chars of s
    // in the dynamic programming matrx, string s is in rows (indices i) and string t is in columns (indices j)

    // compute row 0: how to go from the empty string (i.e. first 0 chars of s) to each char of t
    for (int j = 0; j <= m; j++) {
      p[j] = new InsertionSequence(t.substring(0, j));
    }

    for (int i = 1; i <= n; i++) {
      d[0] = new DeletionSequence(s.substring(0, i));  // how to go from the first i chars of s to the empty string (first 0 chars of t)
      for (int j = 1; j <= m; j++) {
        int jMinus1 = j - 1; // extracted frequently used calculation to speed things up
        char s_i = s.charAt(i - 1);
        char t_j = t.charAt(jMinus1);
        // we are looking for the best way to go from char s_i to char t_j

        if (s_i == t_j) {
          d[j] = p[jMinus1]; // already equal, so no operation required
          continue;
        }
        // we want the best operation we can make at this point, the cost of which will be 1 + the cost of the previous best operation
        // to determine this, we'll need to consider the best sequences up to this point
        EditSequence del = p[j];      // if we're gonna delete, the best previous sequence is stored in the cell directly above
        EditSequence ins = d[jMinus1];  // if we're gonna insert, the best previous sequence is stored in the cell directly to the left
        EditSequence sub = p[jMinus1];  // if we're gonna substitute, the best previous sequence is stored in the cell diagonally up and to the left
        // so which one is the best (lowest cost)? let's find out:
        EditSequence best;
        EditOperation op;  // which operation brings us to the best sequence?
        int insLen = ins.length();  // extracting frequently accessed data to speed things up
        int delLen = del.length();
        int subLen = sub.length();
        if (insLen <= delLen && insLen <= subLen) {
          best = ins;
          op = new Insertion(jMinus1, t_j);
        }
        else if (delLen <= insLen && delLen <= subLen) {
          best = del;
          op = new Deletion(j, s_i);
        }
        else {
          best = sub;
          op = new Substitution(jMinus1, t_j);
        }
        d[j] = new LinkedEditSequence(best, op);
      }
      // swap the last row and previous row references
      EditSequence _d[] = p; // placeholder to assist in swapping p and d
      p = d;
      d = _d;  // reuse the old array for the next row to avoid extra allocation
    }

    // our last action in the above loop was to switch d and p, so p now
    // actually has the most recent cost counts
    return p[m];
  }


  /**
   * Finds the Levenshtein distance between two Strings without reconstructing
   * the edit sequence.  This is faster than editSequence(s,t) by a constant factor
   * (approximately 25x).
   *
   * <p>
   * NOTES(Alex E):
   * <br>
   * This code is based on org.apache.commons.lang.StringUtils.getLevenshteinDistance
   * (which is distributed under the Apache 2.0 license).
   * <br>
   * The space complexity is O(m), where m is the length of the shorter string,
   * while time complexity is O(n*m), i.e. O(n^2), where n is the length of the longer string.
   * The linear space complexity is achieved by only maintaining the last two rows of
   * the dynamic programming table.
   * </p>
   *
   * @param s  the first String, must not be null
   * @param t  the second String, must not be null
   * @return result distance
   * @throws IllegalArgumentException if either String input <code>null</code>
   * @see <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein Distance (Wikipedia)</a>
   */
  public static int editDistance(String s, String t) {
    int n = s.length();
    int m = t.length();
    // base case optimizations
    if (n == 0)
      return m;
    if (m == 0)
      return n;
    // we want to have columns corresponding to the shorter string, in order to use less memory
    IncrementalEditDistanceResult result = (m <= n)
        ? computeEditDistance(s, t)
        : computeEditDistance(t, s);
    return result.editDistance;
  }


  /**
   * Implements the dynamic programming algorithm for editDistance.
   * @param s a string of positive length
   * @param t a string of positive length
   * @return the last row of the dynamic programming table for transforming
   * s into t using Levenshtein's string edit operations.
   */
  private static IncrementalEditDistanceResult computeEditDistance(String s, String t) {
    int n = s.length();
    int m = t.length();
    // Dynamic programming algorithm:
    // A[i,j] = editDistance(s[0..i], t[0..j])
    // (the rows pertain to string s and columns to string t)
    // We don't need to store the entire matrix in memory:
    // we can discard everything but the last two rows (thus using only linear space).
    // The arrays p[] and d[] here represent the last two rows of A.
    // To save additional memory, we put the shorter string horizontally (in colums)
    // and the longer string in rows, such that the total # of iterations will
    // be the same but the arrays will be shorter
    int[] p = new int[m + 1]; //'previous' cost array, horizontally
    int[] d = new int[m + 1]; // cost array, horizontally
    int[] c = new int[n + 1]; // the last column of the matrix (needs to be saved for IncrementalEditDistanceResult)
    // init the first row
    for (int j = 0; j <= m; j++) {
      p[j] = j;  // A[0,j] = editDistance("", t[0..j]) = j
    }
    c[0] = p[m];
    // now fill out the dynamic programming matrix
    fillEditDistanceMatrix(s, t, null, m, p, d, c, 1, n, 0, m);
    return new IncrementalEditDistanceResult(s, t, c, p, p[m]);  // the answer is in the last row (A[n,m], to be exact)
  }

  /**
   * Result of a call to editDistanceIncremental.
   * Immutable, so can be cached and shared safely.
   */
  public static class IncrementalEditDistanceResult {
    /** First argument to editDistance */
    public final String s;
    /** Second argument to editDistance */
    public final String t;
    /** The last column of the matrix computed for editDistance(s,t) */
    public final int[] c;
    /** The last row of the matrix computed for editDistance(s,t) */
    public final int[] d;
    /** The result of editDistance(s,t) */
    public final int editDistance;

    public IncrementalEditDistanceResult(String s, String t, int[] c, int[] d, int editDistance) {
      this.s = s;
      this.t = t;
      this.c = c;
      this.d = d;
      this.editDistance = editDistance;
    }


    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append("IncrementalEditDistanceResult");
      sb.append("(s='").append(s).append('\'');
      sb.append(", t='").append(t).append('\'');
      sb.append(", editDistance=").append(editDistance);
      sb.append(')');
      return sb.toString();
    }
  }

  /**
   * This is an incremental version the editDistance method that continues from
   * where a prior computation on shorter strings left off.  If the strings
   * are appended to over time.
   *
   * The following example demonstrates the redundant computations that this method
   * can save even after the common prefix and suffix has been removed from the strings.
   *
   * args -> args after prefix stripped -> args after suffix stripped:
   * ("Hellr W", "Hello World") -> ("r Wo", "o Wo") -> ("r", "o")
   * ("Hellr Wo", "Hello World") -> ("r Wo", "o Wo") -> ("r", "o")
   * ("Hellr Woz", "Hello World") -> ("r Woz", "o Wor") -> ("r Woz", "o Wor")
   * ("Hellr Wozni", "Hello World") -> ("r Wozn", "o Worl") -> ("r Wozn", "o Worl")
   * ("Hellr Wozni", "Hello World") -> ("r Wozni", "o World") -> ("r Wozni", "o World")
   *
   * @param s The resulting distance will be for transforming this string into t
   * @param t The resulting distance will be for getting this string from s
   * @param priorResult The result of a prior computation on substrings (proper
   * prefixes of s and t). Can pass null if no prior result is available.
   * @return
   */
  public static IncrementalEditDistanceResult editDistanceIncremental(String s, String t, IncrementalEditDistanceResult priorResult) {
    int n = s.length();
    int m = t.length();

    // we do not have a prior result for these strings, compute it now
    if (priorResult == null) {
      // we want to have columns corresponding to the shorter string, in order to use less memory
      if (n < m) {
        String tmp = s;
        s = t;
        t = tmp;
      }
      return computeEditDistance(s, t);
    }

    final String s0 = priorResult.s;
    final String t0 = priorResult.t;
    final int n0 = s0.length();
    final int m0 = t0.length();

    // confirm assumptions
    assert s.startsWith(s0);
    assert t.startsWith(t0);
    assert priorResult.d.length == m0 + 1;

    // Dynamic programming algorithm:
    // A[i,j] = editDistance(s[0..i], t[0..j])
    // (the rows pertain to string s and columns to string t)
    // We don't need to store the entire matrix in memory:
    // we can discard everything but the last two rows (thus using only linear space).
    // The arrays p[] and d[] here represent the last two rows of A.
    // To save additional memory, we put the shorter string horizontally (in colums)
    // and the longer string in rows, such that the total # of iterations will
    // be the same but the arrays will be shorter

    int[] p = new int[m + 1]; //'previous' cost array, horizontally,
    int[] d = new int[m + 1]; // cost array, horizontally
    int[] c = new int[n + 1]; // the last column of the matrix (needs to be saved for IncrementalEditDistanceResult)

    // now we need to fill out row p based on the last column of the prior result
    /* Here a diagram of the new matrix superimposed onto the prior matrix:

        0  1  2 .. m0 j1 j2 .. jm
       ---------------------------
     0 | _  _  _  _  r  y  y  y  y
     1 | _  _  _  _  r  y  y  y  y
     ..| _  _  _  _  r  y  y  y  y
     n0| r  r  r  r  r  y  y  y  y
     i1| X
     i2|
     ..|
     in|

     We need to compute all the values marked with "y" based on the available
     data from the prior result (marked with "r") in order to continue
     filling out the matrix at position "X".
     */

    // compute all the y-values
    // first, init row 0
    p[m0] = priorResult.c[0];
    for (int j = m0+1; j <= m; j++) {   // iterates through t
      p[j] = j; // A[0,j] = editDistance("", t[0..j]) = j
    }
    c[0] = p[m];
    // now continue filling out the dynamic programming matrix from A[n0, m0+1], i.e. d[m0+1]
    fillEditDistanceMatrix(s, t, priorResult, m, p, d, c, 1, n0, m0, m);
    // fill in the lower part of the last row to as much of the prior result as is available
    System.arraycopy(priorResult.d, 0, p, 0, priorResult.d.length);
    // now p[] has all the entries we need to continue filling out d[] at position A[n0+1, m0+1]

    // continue filling out the dynamic programming matrix from A[n0+1, 0]
    fillEditDistanceMatrix(s, t, null, m, p, d, c, n0+1, n, 0, m);
    // our last action in the above loop was to switch d and p, so p now actually has the most recent cost counts
    return new IncrementalEditDistanceResult(s, t, c, p, p[m]);  // the answer is in A[n,m]
  }

  /**
   * Common code used in all for all editDistance computations: fills out
   * the dynamic programming matrix.
   *
   * The last two rows of the matrix are passed by reference in p[] and d[].
   * This method doesn't return any explicit result, but its actual return
   * values are by reference in p[] and d[].
   */
  private static void fillEditDistanceMatrix(String s, String t, IncrementalEditDistanceResult priorResult, int m, int[] p, int[] d, int[] c, int iStart, int iLimit, int jStart, int jLimit) {
    // save the original reference to compute the by-reference return values at the end
    int[] pRef = p;
    int[] dRef = d;

    for (int i = iStart; i <= iLimit; i++) { // iterates through s
      char s_i = s.charAt(i - 1);  // i-th char of s
      if (priorResult == null)
        d[jStart] = i; // A[i,0] = editDistance(s[0..i], "") = i
      else
        d[jStart] = priorResult.c[i];
      for (int j = jStart + 1; j <= jLimit; j++) {   // iterates through t
        int jLast = j - 1;
        char t_j = t.charAt(jLast); // j-th char of t
        if (s_i == t_j)
          // minimum of cell to the left+1, to the top+1, diagonally left and up
          d[j] = Math.min(Math.min(d[jLast]+1, p[j]+1), p[jLast]);
        else
          d[j] = Math.min(Math.min(d[jLast]+1, p[j]+1), p[jLast]+1); // +1 when chars don't match
      }
      c[i] = d[m];
      // swap the last row and previous row references
      int[] _d = p;
      p = d;
      d = _d; // recycle the array for the next iteration, so we don't have to allocate a new object
    }

    // at this point, the referents of p and d variables may or may not match the orignal
    // by-reference parameters; if they don't match, we have to swap the values
    // of the physical arrays (overwriting their values)
    if (p != pRef) {
      for (int i = 0; i < pRef.length; i++) {
        int tmp = pRef[i];
        pRef[i] = dRef[i];
        dRef[i] = tmp;
      }
    }
  }


  /**
   * @param strings: an array of 2 strings.
   * @return the length of the common suffix, 0 if none.
   */
  public static int stripCommonSuffix(String[] strings) {
    // method exposed for unit testing
    String s = strings[0], t = strings[1];
    String suffix = StringUtils.commonSuffix(s, t);
    int suffixLen = suffix.length();
    if (suffixLen > 0) {
      // the suffix can be safely ignored without affecting the edit sequence
      strings[0] = s.substring(0, s.length() - suffixLen);
      strings[1] = t.substring(0, t.length() - suffixLen);
    }
    return suffixLen;
  }

  /**
   * @param strings: an array of 2 strings.
   * @return the length of the common prefix, 0 if none.
   */
  public static int stripCommonPrefix(String[] strings) {
    // method exposed for unit testing
    String s = strings[0], t = strings[1];
    String prefix = StringUtils.commonPrefix(s, t);
    int prefixLen = prefix.length();
    if (prefixLen > 0) {
      // remove the shared prefix from the strings; we'll account for it later
      strings[0] = s.substring(prefixLen);
      strings[1] = t.substring(prefixLen);
    }
    return prefixLen;
  }

  /**
   * A version of editDistance optimized for strings sharing a common prefix and/or suffix.
   * Since the time of the algorithm is O(n^2), anything that reduces n helps.
   * @param commonPrefixPossible pass true to activate the common prefix optimization
   * @param commonSuffixPossible pass true to activate the common suffix optimization
   * @return the edit distance between s and t
   */
  public static int editDistance(String s, String t, boolean commonPrefixPossible, boolean commonSuffixPossible) {
    // if the strings share a common prefix and/or suffix, we can speed up the
    // algorithm by first stripping those off and accounting for the prefix
    // later (the suffix has no bearing on the edit sequence)
    String[] strings = new String[]{s, t};
    if (commonSuffixPossible)
      stripCommonSuffix(strings);
    if (commonPrefixPossible)
      stripCommonPrefix(strings);
    return editDistance(strings[0], strings[1]);
  }

  /**
   * A version of editSequence optimized for strings sharing a common prefix and/or suffix.
   * Since the time of the algorithm is O(n^2), anything that reduces n helps.
   * @param commonPrefixPossible pass true to activate the common prefix optimization
   * @param commonSuffixPossible pass true to activate the common suffix optimization
   * @return the edit sequence transforming s into t
   *  */
  public static EditSequence editSequence(String s, String t, boolean commonPrefixPossible, boolean commonSuffixPossible) {
    // if the strings share a common prefix and/or suffix, we can speed up the
    // algorithm by first stripping those off and accounting for the prefix
    // later (the suffix has no bearing on the edit sequence)
    String[] strings = new String[]{s, t};
    if (commonSuffixPossible)
      stripCommonSuffix(strings); // we can forget about the suffix now, since it has no bearing on the output
    int prefixLen = 0;
    if (commonPrefixPossible)
      prefixLen = stripCommonPrefix(strings);
    EditSequence sequence = editSequence(strings[0], strings[1]);
    if (prefixLen > 0)
      sequence = sequence.shift(prefixLen); // we need to shift all the indices in the edit sequence by the length of the common prefix
    return sequence;
  }


  /**
   * @return true if the s is a proper subsequence of t.
   * @see <a href="http://en.wikipedia.org/wiki/Longest_common_subsequence_problem">LCS problem</a>
   */
  static boolean isSubsequence(String s, String t) {
    int j = -1; // i and j are indices into s and t, respectively
    for (int i = 0; i < s.length(); i++) {
      char s_i = s.charAt(i);
      // fast-forward t to the position of this shared char
      int tStart = j + 1;
      boolean foundInT = false;
      for (j = tStart; j < t.length(); j++) {
        if (t.charAt(j) == s_i) {
          foundInT = true;
          break;
        }
      }
      if (!foundInT)
        return false;
    }
    return true;
  }

  /** A flag that can be used to keep the dynamic programming matrix of the LCS algorithm in memory for debugging */
  private static final boolean KEEP_MATRIX_IN_MEMORY = false;

  /**
   * Solves the longest common subsequence problem, which is necessary to compute
   * the diffs between two strings.
   *
   * @see <a href="http://en.wikipedia.org/wiki/Longest_common_subsequence_problem">LCS problem</a>
   */
  public static String longestCommonSubsequence(String s, String t) {
    int n = s.length();
    int m = t.length();

    // base case optimizations
    if (n == 0 || m == 0)
      return "";
    // a common real-world scenario is when the shorter string is a subsequence of the longer string
    // we check for that here because if it is, the O(n*m) time becomes O(n+m)
    if (n <= m && isSubsequence(s, t))
      return s;
    else if (n > m && isSubsequence(t, s))
      return t;

    // we'll use simple dynamic programming:
    // A[i,j] = LCS(s[0..i], t[0..j])
    String[][] A = null; // next row of A
    String[] a = null; // next row of A: row A[i]
    String[] p = null; // previous row of A:  row A[i-1]
    if (KEEP_MATRIX_IN_MEMORY) {
      A = new String[n + 1][m + 1];
    }
    else {
      a = new String[m + 1];
      p = new String[m + 1];
    }

    for (int j = 0; j <= m; j++) { // iterates through t
      if (KEEP_MATRIX_IN_MEMORY)
        A[0][j] = ""; // p[i] = A[0,i] = LCS("", t[0..i])
      else
        p[j] = ""; // p[i] = A[0,i] = LCS("", t[0..i])
    }

    for (int i = 1; i <= n; i++) { // iterates through s
      if (KEEP_MATRIX_IN_MEMORY)
        A[i][0] = "";  // a[0] = A[i,0] = LCS(s[0..i], "")
      else
        a[0] = "";  // a[0] = A[i,0] = LCS(s[0..i], "")
      for (int j = 1; j <= m; j++) {   // iterates through t
        // A[i,j] = (A[i-1, j-1] + s[i]) if s[i] == t[j] else A[i-1, j-1]
        char s_i = s.charAt(i-1);
        char t_j = t.charAt(j-1);
        // choose the best of the adjacent sequences
        class BestSequence extends ArgMax<String, Integer> {
          public Integer update(String arg) {
            return super.update(arg, arg.length());
          }
        }
        BestSequence bestPicker = new BestSequence();
        if (KEEP_MATRIX_IN_MEMORY) {
          bestPicker.update(A[i-1][j-1]);
          if (s_i != t_j) {
            bestPicker.update(A[i][j-1]);
            bestPicker.update(A[i-1][j]);
          }
        }
        else {
          bestPicker.update(p[j-1]);
          if (s_i != t_j) {
            bestPicker.update(a[j-1]);
            bestPicker.update(p[j]);
          }
        }
        String best = bestPicker.get();
        int bestLen = best.length();
        if (bestLen == n || bestLen == m) {
          // we already have the longest possible sequence; return early
          return best;
        }
        if (s_i == t_j) {
          if (KEEP_MATRIX_IN_MEMORY)
            A[i][j] = best + s_i;
          else
            a[j] = best + s_i;
        }
        else {
          if (KEEP_MATRIX_IN_MEMORY)
            A[i][j] = best;
          else
            a[j] = best;
        }
      }
      if (!KEEP_MATRIX_IN_MEMORY) {
        // copy current row to previous row
        String[] tmp = p;
        p = a;
        a = tmp; // avoid instantiating a new array: recycle the old one
      }
    }
    if (KEEP_MATRIX_IN_MEMORY) {
//    System.out.println(StringUtils.matrixToPrettyString(a));  // print the matrix
      return A[n][m];
    }
    else {
      // our last action in the above loop was to switch d and p, so p now
      // actually has the most recent cost counts
      return p[m];
    }
  }


  /**
   * Produces a list of TextRuns, which account for all the similarities
   * and differences between strings s and t (i.e. how to transform s into t).
   * Uses longestCommonSubsequence to come up with an answer.
   */
  public static Diffs diff(String s, String t) {
    Diffs diffs = diffHelperUnmergedDiffsGivenLCS(s, t, longestCommonSubsequence(s, t));
    List<TextRun> runs = diffs.getRuns();
    // now just need to merge consecutive runs of the same type (e.g. [+a, +b] should become [+ab])
    boolean keepGoing = true;
    while (keepGoing) {
      // keep merging consecutive runs until no consecutive runs left
      keepGoing = false;
      for (int i = 0; i < runs.size() - 1; i++) {
        TextRun run = runs.get(i);
        TextRun nextRun = runs.get(i + 1);
        if (run.getClass() == nextRun.getClass()) {
          // merge consecutive run into this one
          run.text += nextRun.text;
          runs.remove(i + 1);
          keepGoing = true;
          break;
        }
      }
    }
    return diffs;
  }

  /**
   * Produces a list of un-merged TextRuns, which account for all the similarities
   * and differences between strings s and t (i.e. how to transform s into t).
   * Uses the given longestCommonSubsequence to come up with an answer.
   */
  public static Diffs diffHelperUnmergedDiffsGivenLCS(String s, String t, String lcs) {
    // From a longest common subsequence it's only a small step to get diff-like output:
    // if an item is absent in the subsequence but present in the original, it must have been deleted.
    // if it is absent in the subsequence but present in the second sequence, it must have been added in.

    // Example 1:
    // s: abc
    // t: a12b345
    // L: ab
    // iterating over L:
    // 1): a is found immediately at s_0 and t_0: [=a]
    // 2): b is found at s_1 and t_4, diff everything in-between: [=a, +12, =b]
    // 3): end of LCS; diff everything left over: [=a, +12, =b, $3, +45]

    // Example 2:
    // s: frabcodo
    // t: 0a12b345
    // L: ab
    // iterating over L:
    // 1): scan both strings until "a" is found: got s_2 and t_1: [$0, -r, =a)]
    // 2): found "b" at s_3 and t_4: + [+12, =b)]
    // 3): end of LCS; diff remainders: "codo" and "345": + [$345, -o]

    ArrayList<TextRun> runs = new ArrayList<TextRun>();
    int i = -1, j = -1; // i, j, and k are indices into s, t, and lcs, respectively
    for (int k = 0; k < lcs.length(); k++) {
      char lcsNext = lcs.charAt(k);
      // fast-forward each string to the position of this shared char
      int sStart = i + 1;
      int tStart = j + 1;
      for (i = sStart; i < s.length(); i++)
        if (s.charAt(i) == lcsNext)
          break;
      for (j = tStart; j < t.length(); j++)
        if (t.charAt(j) == lcsNext)
          break;
      diffHelperAddDiffsNoCS(runs, s.substring(sStart, i), t.substring(tStart, j));
      runs.add(new TextRunMatch(s.substring(i, i+1)));
    }
    // the are no more chars in the common subsequence; add whatever is left
    diffHelperAddDiffsNoCS(runs, s.substring(i+1), t.substring(j+1));
    return new Diffs(runs);
  }

  /**
   * Adds the diffs between s and t, which are strings sharing no
   * common subsequences, to the given list.
   * @return the given list, for call chaining
   * Method exposed for unit testing.
   */
  static List<TextRun> diffHelperAddDiffsNoCS(List<TextRun> runs, String s, String t) {
//    System.out.println("Calling " + StringUtils.methodCallToString("diffsHelper", s, t));
    int n = s.length();
    int m = t.length();
    if (n == 0 && m == 0)
      return runs;
    else if (n == 0 && m > 0) {
      runs.add(new TextRunInsert(t));
      return runs;
    }
    else if (n > 0 && m == 0) {
      runs.add(new TextRunDelete(s));
      return runs;
    }
    // if got this far, neither string has 0 length
    if (n <= m) {
      // t is the same or longer than s
      runs.add(new TextRunSubstitute(t.substring(0, n)));  // treat all overlap as swap
      if (n < m)
        runs.add(new TextRunInsert(t.substring(n)));  // and everything left over as inserted
    }
    else {
      // s is longer than t
      runs.add(new TextRunSubstitute(t));  // treat all overlap as swap
      runs.add(new TextRunDelete(s.substring(m))); // and everything left over as deleted
    }
    return runs;
  }

  public static class Diffs implements Iterable<TextRun> {
    private ArrayList<TextRun> runs = new ArrayList<TextRun>();
    public Diffs(ArrayList<TextRun> runs) {
      this.runs = runs;
    }
    public Iterator<TextRun> iterator() {
      return runs.iterator();
    }
    public int editDistance() {
      // count the number of non-matching chars in the diff sequence,
      int c = 0;
      for (TextRun run : this) {
        if (!(run instanceof TextRunMatch))
          c += run.text.length();
      }
      return c;
    }
    public List<TextRun> getRuns() {
      return runs;
    }
    public String toString() {
      return runs.toString();
    }
  }

  public static class TextRun {
    String text;
    public TextRun(String text) {
      this.text = text;
    }
    public String getText() {
      return text;
    }
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof TextRun)) return false;
      if (getClass() != o.getClass()) return false;
      TextRun textRun = (TextRun)o;
      return !(text != null ? !text.equals(textRun.text) : textRun.text != null);
    }
    public int hashCode() {
      return (text != null ? text.hashCode() : 0);
    }
    @Override
    public String toString() {
      return text;
    }
  }

  public static class TextRunMatch extends TextRun {
    public TextRunMatch(String text) {
      super(text);
    }

    @Override
    public String toString() {
      return "=" + super.toString();
    }
  }

  public static class TextRunSubstitute extends TextRun {
    public TextRunSubstitute(String text) {
      super(text);
    }

    @Override
    public String toString() {
      return "$" + super.toString();
    }
  }

  public static class TextRunInsert extends TextRun {
    public TextRunInsert(String text) {
      super(text);
    }
    @Override
    public String toString() {
      return "+" + super.toString();
    }
  }

  public static class TextRunDelete extends TextRun {
    public TextRunDelete(String text) {
      super(text);
    }
    @Override
    public String toString() {
      return "-" + super.toString();
    }
  }

}
