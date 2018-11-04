/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.shared.util.trees;

import junit.framework.TestCase;
import solutions.trsoftware.commons.shared.data.CountryCodes;

import java.util.Collection;
import java.util.List;

/**
 * @author Alex
 * @since 2/10/2018
 */
public class BalancedBinarySearchTreeTest extends TestCase {

  public void testPut() throws Exception {
    BalancedBinarySearchTree<String, Integer> bst = new BalancedBinarySearchTree<String, Integer>();
    List<String> keys = CountryCodes.listAllCodes();
    for (int i = 0; i < keys.size(); i++) {
      String key = keys.get(i);
      bst.put(key, i);
    }

    BSTNode<String, Integer> root = bst.getRoot();
    // fix the parent links TODO: the tree should maintain these properly
    root.setParent(null);
    root.accept(new ParentReferenceSetter());

    // validate the parent links
    root.accept(new ParentReferenceAsserter());
    // print the tree
    root.accept(new PrintVisitor(TraversalStrategy.PRE_ORDER));
  }


  private static class ParentReferenceAsserter<N extends Node> extends AbstractVisitor<N> {

    public ParentReferenceAsserter() {
    }

    public ParentReferenceAsserter(TraversalStrategy strategy) {
      super(strategy);
    }

    @Override
    public void visit(N node) {
      Collection<N> children = node.getChildren();
      for (N child : children) {
        assertSame(node, child.getParent());
      }
    }
  }

  private static class ParentReferenceSetter extends AbstractVisitor<BSTNode> {

    public ParentReferenceSetter() {
    }

    public ParentReferenceSetter(TraversalStrategy strategy) {
      super(strategy);
    }

    @Override
    public void visit(BSTNode node) {
      Collection<BSTNode> children = node.getChildren();
      for (BSTNode child : children) {
        child.setParent(node);
      }
    }

  }
}