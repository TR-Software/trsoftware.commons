/*
 * Copyright 2021 TR Software Inc.
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

package solutions.trsoftware.commons.server.stats;

import solutions.trsoftware.commons.shared.util.trees.AbstractNode;
import solutions.trsoftware.commons.shared.util.trees.Node;
import solutions.trsoftware.commons.shared.util.trees.TraversalStrategy;
import solutions.trsoftware.commons.shared.util.trees.Visitor;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static solutions.trsoftware.commons.shared.util.LogicUtils.firstNonNull;

/**
 * An adapter for a {@link Counter} (i.e. the {@link #delegate}) that allows it to be used in a
 * hierarchy of {@link Counter counters}. All counting ops are delegated to both {@link #delegate} and {@link #parent}.
 *
 * @since Mar 23, 2011
 * @author Alex
 */
public class HierarchicalCounter extends Counter implements Node<Counter> {

  /** Counting ops will be delegated to this counter as well as {@link #parent}  */
  private final Counter delegate;

  /**
   * Counting ops will be delegated to this counter as well as {@link #delegate}.
   * If {@code null} then {@link HierarchicalCounter this} is the root of the hierarchy.
   */
  private final HierarchicalCounter parent;

  /**
   * The counters at the next level of the hierarchy, sorted by name.  This map shouldn't be modified except by the
   * {@link #HierarchicalCounter(Counter, HierarchicalCounter) constructor}, which creates
   * the child &rarr; parent binding (i.e. delegation from child to parent).
   */
  private final SortedMap<String, HierarchicalCounter> children = new ConcurrentSkipListMap<>();

  public HierarchicalCounter(Counter delegate, HierarchicalCounter parent) {
    super(delegate.getName());
    this.delegate = delegate;
    this.parent = parent;
    if (parent != null) {
      parent.children.put(name, this);
    }
  }

  public void add(int delta) {
    delegate.add(delta);
    if (parent != null)
      parent.add(delta);
  }

  public int getCount() {
    return delegate.getCount();
  }


  /**
   * @return the number of nested {@link HierarchicalCounter counters}
   */
  public int getNumChildren() {
    return children.size();
  }

  /**
   * @return {@code true} iff has a nested {@link HierarchicalCounter counter} with the given name
   */
  public boolean containsChild(String name) {
    return children.containsKey(name);
  }

  /**
   * @return a nested {@link HierarchicalCounter counter} with the given name,
   * or {@code null} if doesn't have a child with the given name
   */
  public HierarchicalCounter getChild(String name) {
    return children.get(name);
  }
//
//  public void accept(Visitor visitor) {
//    visitor.visit(this);
//    for (HierarchicalCounter child : children.values()) {
//      child.accept(visitor);
//    }
//  }

  @Override
  public <V extends Visitor> void accept(V visitor) {
    TraversalStrategy strategy = firstNonNull(visitor.getStrategy(), TraversalStrategy.PRE_ORDER);
    strategy.traverse(this, visitor);
  }

  @Override
  public Collection<Node<Counter>> getChildren() {
    return Collections.unmodifiableCollection(children.values());
  }

  @Override
  public boolean isLeaf() {
    return children.isEmpty();
  }

  @Override
  public Counter getData() {
    return delegate;
  }

  @Override
  public int depth() {
    return AbstractNode.computeDepth(this);
  }

  public HierarchicalCounter getParent() {
    return parent;
  }


//  public interface Visitor {
//    void visit(HierarchicalCounter counter);
//  }

}
