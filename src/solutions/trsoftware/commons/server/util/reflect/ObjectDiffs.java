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

package solutions.trsoftware.commons.server.util.reflect;

import solutions.trsoftware.commons.shared.util.SimplePair;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;

import static solutions.trsoftware.commons.shared.util.Assert.assertNotNull;
import static solutions.trsoftware.commons.shared.util.Assert.assertTrue;


/**
 * Provides the method {@link #diffValues(Object, Object)} which computes the diffs of two instances of a class by going
 * through the members selected for that class by a {@link MemberSet} and checking their values for equality.
 *
 * To use this class, simply create an instance, then specify some classes whose instances are to be
 * compared member-by-member by calling {@link #addReflectionSpec(MemberSet)} for each such class,
 * then finally call {@link #diffValues(Object, Object)}.  An instance of this class can be safely reused between
 * successive invocations of {@link #diffValues(Object, Object)}.
 *
 * @author Alex, 3/30/2016
 */
public class ObjectDiffs {


  /**
   * {@link #diffValues(Object, Object)} will use diff each instance member individually when the value type is
   * contained in this map, otherwise will use {@link Object#equals(Object)}.
   */
  private ClassMap<MemberSet> memberSelectors = new ClassMap<MemberSet>();

  /**
   * {@link #diffValues(Object, Object)} will use the functions in this map instead of {@link Object#equals(Object)} for
   * values whose class has an entry in this map.
   */
  private ClassMap<EqualsFunction> equalsFunctions = new ClassMap<EqualsFunction>();

  public ObjectDiffs() {
  }


  public ObjectDiffs(MemberSet... reflectionSpecs) {
    for (MemberSet spec : reflectionSpecs)
      addReflectionSpec(spec);
  }



  public ObjectDiffs addReflectionSpec(MemberSet spec) {
    Class type = spec.getType();
    assertNotNull(type);
    assertTrue(!type.isPrimitive());
    assertTrue(!type.isArray());
    memberSelectors.put(type, spec);
    return this;  // for method chaining
  }

  public MemberSet getReflectionSpecFor(Class type) {
    return memberSelectors.get(type);
  }

  // method provided for unit testing
  MemberSet removeMemberQuery(Class cls) {
    return memberSelectors.remove(cls);
  }

  public <T> ObjectDiffs addEqualsFunction(Class<T> type, EqualsFunction<T> eqFcn) {
    assertNotNull(type);
    assertNotNull(eqFcn);
    equalsFunctions.put(type, eqFcn);
    return this;  // for method chaining
  }

  /** Decides whether two objects should be considered equal. */
  public static interface EqualsFunction<T> {
    /** @return true iff there are no diffs between a and b */
    boolean eq(T a, T b);
  }

  /**
   * Computes the difference between two values, recursively. Will use {@link Object#equals(Object)} unless the class of the values
   * matches either a {@link MemberSet} that has been passed to {@link #addReflectionSpec(MemberSet)} or an {@link EqualsFunction}
   * passed to {@link #addEqualsFunction(Class, EqualsFunction)}.  If the class matches a {@link MemberSet},
   * will do a member-by-member comparison of the two instances.  If the class matches an {@link EqualsFunction}, will
   * use that instead of {@link Object#equals(Object)}.
   *
   * Compares array and {@link Collection} elements (excluding {@link Set}s) according to their iteration order, recursively
   * diffing corresponding pairs of elements. If the RHS (right-hand-side) array or collection contains more elements than
   * LHS, the result will include an instance of {@link Added} for every extra index.  If RHS contains fewer elements,
   * on the other hand, will include an instance {@link Removed} for every index that's not in LHS.  If the array/collection
   * element type does not match a {@link MemberSet}, the result will include an instance of {@link Diff} for every
   * pair of corresponding indexes that are not equal according to {@link Object#equals(Object)}.
   * {@link Map} entries are treated similar to collections, returning {@link Added} for every RHS key that's not in LHS,
   * {@link Removed} for every LHS key that's not in RHS, and {@link Diff} for matching entries (equal keys) whose
   * values are different.  Map entry values are diffed recursively, while the keys are always compared according to
   * their version of {@link Object#equals(Object)}.
   *
   * NOTE: since collections are compared according to their iteration order, this method could produce unexpected
   * results if the collection is not ordered, which is why this method does not process instances of {@link Set}
   * element-by-element like it does for other {@link Collection}s.  Instead, if two {@link Set}s are not equal
   * according to {@link Set#equals(Object)}, just one instance of {@link Diff} is returned.
   */
  public Result diffValues(Object o1, Object o2) throws InvocationTargetException, IllegalAccessException {
    return new Result(o1, o2, new DiffTreeBuilder().buildTree(o1, o2));
  }


  /** Encapsulates the {@link Diff}s that were found during the deep comparison between {@link #getLeft()} and {@link #getRight()} */
  public static class Result extends SimplePair<Object, Object> {
    /** The root of the diff tree in the comparison between {@link #getLeft()} and {@link #getRight()} */
    private Diff root;

    /** A list of all the leaf nodes of the tree */
    private List<Diff> diffs;

    private Result(Object left, Object right, Diff root) {
      super(left, right);
      this.root = root;
      if (root != null)
        root.accept(new DiffVisitor() {
          @Override
          public void visit(Diff node) {
            if (node.isLeaf()) {
              if (diffs == null)
                diffs = new ArrayList<Diff>();
              diffs.add(node);
            }
          }
        });
    }

    /**
     * @return false iff any diffs were found
     */
    public boolean isEmpty() {
      return diffs == null;
    }

    /**
     * @return A list of the leaf nodes in the diff tree between the two objects, or to put it simply,
     * the list of the diffs between the two objects.
     */
    public List<Diff> getDiffs() {
      return diffs;
    }

    /**
     * @return The list of the leaf nodes in the diff tree between the two objects.
     */
    public Diff getDiffTreeRoot() {
      return root;
    }

    @Override
    public String toString() {
      StringBuilder str = new StringBuilder();
      str.append(ObjectDiffs.class.getSimpleName()).append(" found ");
      int nDiffs = isEmpty() ? 0 : diffs.size();
      if (isEmpty())
        str.append("no");
      else
        str.append(diffs.size());
      str.append(" ").append(StringUtils.pluralize("difference", nDiffs)).append(" between a=");
      Diff.printValue(str, getLeft());
      str.append(" and b=");
      Diff.printValue(str, getRight());
      if (!isEmpty()) {
        str.append(":");
        int i = 1;
        for (Diff diff : diffs) {
          str.append("\n  ").append(i++).append(") ");
          if (!diff.getPath().isEmpty())
            str.append("a");
          str.append(diff);
        }
      }
      return str.toString();
    }
  }


  private class DiffTreeBuilder {

    private Stack stack = new Stack();

    public Diff buildTree(Object o1, Object o2) throws InvocationTargetException, IllegalAccessException {
      Diff root = new Diff(o1, o2);
      if (expand(root))
        return root;
      return null; // no diffs were found
    }

    /**
     * Expands the given node by adding children for its not-equal elements, recursively.
     * @return {@code true} iff this node should be kept (either a leaf node whose values are not equal, or
     * it's an internal node that has at least one descendant whose values are not equal).
     */
    private boolean expand(Diff node) throws InvocationTargetException, IllegalAccessException {
      Object v1 = node.getLeft(), v2 = node.getRight();
      if (v1 != null && v2 != null) {
        // we first have to check the stack to be sure we don't expand nodes representing pairs of values
        // that we've already expanded (to avoid infinite recursion)
        if (stack.contains(v1, v2))
          return false;
        stack.push(node);
        boolean ret;
        MemberSet memberQuery = memberSelectors.getBestAssignableFrom(v1.getClass(), v2.getClass());
        if (memberQuery != null)
          ret = maybeAddChildren(node, memberQuery, v1, v2);
        else {
          EqualsFunction eqFcn = equalsFunctions.getBestAssignableFrom(v1.getClass(), v2.getClass());
          if (eqFcn != null)
            // this is a leaf node (we don't expand it any further since the user provided an EqualsFunction to test the equality of values of this type)
            ret = !eqFcn.eq(v1, v2);
          else if (v1.getClass().isArray() && v2.getClass().isArray())
            ret = maybeAddChildren(node, new ArrayIterator(v1), new ArrayIterator(v2));
          else if (v1 instanceof Collection && v2 instanceof Collection && !(v1 instanceof Set))
            // we expand all collections according to their iteration order (except sets, which are unordered, so we punt on them, leaving the comparison up to Set.equals)
            ret = maybeAddChildren(node, ((Collection)v1).iterator(), ((Collection)v2).iterator());
          else if (v1 instanceof Map && v2 instanceof Map)
            ret = maybeAddChildren(node, ((Map)v1), ((Map)v2));
          else
            ret = !v1.equals(v2);  // this is a leaf node (either a primitive or we don't have a MemberQuery spec for it, so we use Object.equals)
        }
        stack.pop(node);
        node.trimToSize();
        return ret;
      }
      else if (v1 == null && v2 == null)
        return false;
      return true;  // one of the values is null, so the are not equal and this node should be kept
    }

    /** Adds children to a node representing a pair of {@link Map} instances. */
    private boolean maybeAddChildren(Diff node, Map m1, Map m2) throws InvocationTargetException, IllegalAccessException {
      boolean ret = false;
      // add entries for all the keys in m1
      for (Object key : m1.keySet()) {
        ret |= maybeAddChild(node, m2.containsKey(key)
            ? new Diff(node, new MapEntry(key), m1.get(key), m2.get(key))
            : new Removed(node, new MapEntry(key), m1.get(key)));
      }
      // now add entries for all the keys in m2 that are not in m1
      for (Object key : m2.keySet()) {
        if (!m1.containsKey(key))
          ret |= maybeAddChild(node, new Added(node, new MapEntry(key), m2.get(key)));
      }
      return ret;
    }

    private boolean maybeAddChildren(Diff node, MemberSet<?> spec, Object o1, Object o2) throws InvocationTargetException, IllegalAccessException {
      boolean ret = false;
      for (Member member : spec)
        ret |= maybeAddChild(node, new Diff(node, new ClassMember(member), evalMember(member, o1), evalMember(member, o2)));
      return ret;
    }

    private boolean maybeAddChildren(Diff node, Iterator it1, Iterator it2) throws InvocationTargetException, IllegalAccessException {
      boolean ret = false;
      for (int i = 0; it1.hasNext() || it2.hasNext(); i++) {
        Element elt = new Element(i);
        Diff child;
        if (it1.hasNext() && it2.hasNext())
          child = new Diff(node, elt, it1.next(), it2.next());
        else if (it1.hasNext())
          child = new Removed(node, elt, it1.next());
        else
          child = new Added(node, elt, it2.next());
        ret |= maybeAddChild(node, child);
      }
      return ret;
    }

    private boolean maybeAddChild(Diff parent, Diff child) throws InvocationTargetException, IllegalAccessException {
      if (expand(child)) {  // recursively expand the child node, and add it if it or one of its descendants
        parent.addChild(child);
        return true;
      }
      return false;
    }

  }

  private static class Stack {
    private LinkedList<StackFrame> frames = new LinkedList<StackFrame>();

    private boolean contains(Object left, Object right) {
      for (StackFrame frame : frames) {
        if (frame.matches(left, right))
          return true;
      }
      return false;
    }

    private StackFrame push(Diff node) {
      StackFrame frame = new StackFrame(node);
      frames.push(frame);
      return frame;
    }

    private StackFrame pop(Diff node) {
      StackFrame frame = frames.pop();
      assert frame.matches(node.getLeft(), node.getRight());
      return frame;
    }

  }

  private static class StackFrame extends SimplePair<Object, Object> {

    public StackFrame(Diff node) {
      super(node.getLeft(), node.getRight());
    }

    public boolean matches(Object left, Object right) {
      return same(left, getLeft()) && same(right, getRight())
          || same(right, getLeft()) && same(left, getRight());
    }

    private static boolean same(Object a, Object b) {
      if (a == null)
        return b == null;
      else if (b == null)
        return false;
      return a == b;
    }
  }

  /**
   * @return The value of the given member in the given class.
   */
  private static Object evalMember(Member member, Object instance) throws IllegalAccessException, InvocationTargetException {
    if (member instanceof Field)
      return ((Field)member).get(instance);
    else if (member instanceof Method)
      return ((Method)member).invoke(instance);
    throw new IllegalArgumentException();
  }


  public static interface PathElement {
  }


  public static class ClassMember implements PathElement {
    private Member member;

    public ClassMember(Member member) {
      this.member = member;
    }

    public Member getMember() {
      return member;
    }

    @Override
    public String toString() {
      StringBuilder str = new StringBuilder().append('.').append(member.getName());
      if (member instanceof Method)
        str.append("()");
      return str.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ClassMember that = (ClassMember)o;

      if (!member.equals(that.member)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return member.hashCode();
    }
  }

  /**
   * Represents an element of an array, list, or other collection.
   */
  public static class Element implements PathElement {
    private final int index;

    public Element(int index) {
      this.index = index;
    }

    /** @return the index of the array or list element represented by a {@link Diff} */
    public int getIndex() {
      return index;
    }

    @Override
    public String toString() {
      return new StringBuilder().append('[').append(index).append(']').toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Element)) return false;

      Element that = (Element)o;

      if (index != that.index) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return index;
    }
  }

  public static class MapEntry implements PathElement {
    private Object key;

    public MapEntry(Object key) {
      this.key = key;
    }

    public Object getKey() {
      return key;
    }

    @Override
    public String toString() {
      StringBuilder str = new StringBuilder();
      str.append(".get(");
      Diff.printValue(str, key);
      str.append(')');
      return str.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof MapEntry)) return false;

      MapEntry mapEntry = (MapEntry)o;

      if (key != null ? !key.equals(mapEntry.key) : mapEntry.key != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return key != null ? key.hashCode() : 0;
    }
  }


  public interface DiffVisitor {
    void visit(Diff node);
  }

  /**
   * Represents a node in the diff tree, which encapsulates the difference between two values being compared:
   * {@link #getLeft()} and {@link #getRight()}.
   */
  public static class Diff extends SimplePair<Object, Object> {
    private final PathElement pathElement;
    private Diff parent;
    private ArrayList<Diff> children;

    private Diff(Diff parent, PathElement pathElement, Object left, Object right) {
      super(left, right);
      this.parent = parent;
      this.pathElement = pathElement;
    }

    /** Constructor for unit testing */
    Diff(Object left, Object right) {
      this(null, null, left, right);
    }

    public List<PathElement> getPath() {
      ArrayList<PathElement> path = new ArrayList<PathElement>();
      for (Diff next = this; next != null; next = next.parent) {
        PathElement elt = next.pathElement;
        if (elt == null)
          break;
        path.add(elt);
      }
      Collections.reverse(path);
      return path;
    }

    public boolean isLeaf() {
      return children == null;
    }

    public Diff addChild(Diff node) {
      if (children == null)
        children = new ArrayList<Diff>();
      children.add(node);
      return this; // for method chaining
    }

    public void accept(DiffVisitor visitor) {
      visitor.visit(this);
      if (children != null)
        for (Diff child : children) {
          child.accept(visitor);
        }
    }

    public void trimToSize() {
      if (children != null)
        children.trimToSize();
    }

    @Override
    public final String toString() {
      StringBuilder str = new StringBuilder(128);
      if (isLeaf()) {
        // print the entire path for a leaf node
        List<PathElement> path = getPath();
        if (!path.isEmpty()) {
          for (PathElement p : path)
            str.append(p);
          str.append(": ");
        }
      }
      else {
        // internal node (not leaf) - print just the last path element
        str.append("... ");
        if (pathElement != null) {
          str.append(pathElement);
          str.append(": ");
        }
      }
      printValues(str);
      return str.toString();
    }

    /**
     * Subclasses should override this method to include their type-specific info in the {@link #toString()} result.
     */
    protected void printValues(StringBuilder str) {
      printValue(str, getLeft());
      str.append(" vs. ");
      printValue(str, getRight());
    }

    protected static void printValue(StringBuilder str, Object value) {
      if (value == null)
        str.append(value);
      else {
        Class<?> type = value.getClass();
        if (type.isPrimitive() || ReflectionUtils.isPrimitiveWrapper(type))
          str.append(value);
        else {
          if (type != String.class)
            str.append('(').append(type.getSimpleName()).append(')');
          str.append('"').append(value).append('"');
        }
      }
    }
  }


  public static class Added extends Diff {
    private Added(Diff parent, PathElement pathElement, Object value) {
      super(parent, pathElement, null, value);
    }

    /** Constructor for unit testing */
    Added(Object value) {
      this(null, null, value);
    }

    public Object getValue() {
      return getRight();
    }

    @Override
    protected void printValues(StringBuilder str) {
      str.append("added ");
      printValue(str, getValue());
    }
  }

  public static class Removed extends Diff {

    private Removed(Diff parent, PathElement pathElement, Object value) {
      super(parent, pathElement, value, null);
    }

    /** Constructor for unit testing */
    Removed(Object value) {
      this(null, null, value);
    }

    public Object getValue() {
      return getLeft();
    }

    @Override
    protected void printValues(StringBuilder str) {
      str.append("removed ");
      printValue(str, getValue());
    }

  }


}
