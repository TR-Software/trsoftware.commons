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

package solutions.trsoftware.commons.shared.util.trees;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Alex
 * @since 2/9/2018
 */
public class BSTNode<K extends Comparable<K>, V> extends AbstractNode<Map.Entry<K, V>> {
  private K key;           // sorted by key
  private V value;         // associated data
  private BSTNode<K, V> parent;
  /** left and right subtrees */
  private BSTNode<K, V> left, right;
  /** number of nodes in subtrees */
  private int size;
  /**
   * The height of a node is the number of edges on the longest path between that node and a leaf.
   *
   * @see <a href="https://en.wikipedia.org/wiki/Tree_(data_structure)#Terminology">Tree Data Structure terminology</a>
   */
  private int height;

  public BSTNode(K key, V value, BSTNode<K, V> parent, int size) {
    this.key = key;
    this.value = value;
    this.parent = parent;
    this.size = size;
  }

  @Override
  public Collection<BSTNode<K, V>> getChildren() {
    ArrayList<BSTNode<K, V>> children = new ArrayList<BSTNode<K, V>>();
    if (left != null)
      children.add(left);
    if (right != null)
      children.add(right);
    return children;
  }

  /**
   * @return {@code true} iff this node doesn't have any children
   */
  @Override
  public boolean isLeaf() {
    return left == null && right == null;
  }

  @Override
  public BSTNode<K, V> getParent() {
    return parent;
  }

  public void setParent(BSTNode<K, V> parent) {
    this.parent = parent;
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  public int size() {
    return size;
  }

  @Override
  public Map.Entry<K, V> getData() {
    return new AbstractMap.SimpleImmutableEntry<K, V>(key, value); // TODO: create this in constructor (or replace the key/value fields with this Map.Entry object)
  }

  public BSTNode<K, V> getLeft() {
    return left;
  }

  public BSTNode<K, V> getRight() {
    return right;
  }

  public void setLeft(BSTNode<K, V> left) {
    this.left = left;
  }

  public void setRight(BSTNode<K, V> right) {
    this.right = right;
  }

  public void setValue(V value) {
    this.value = value;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  @Override
  public String toString() {
    return "(" + key + ", " + value + ')';
  }
}
