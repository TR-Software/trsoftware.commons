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

package solutions.trsoftware.commons.shared.util.trees;

/**
 * A <a href="https://en.wikipedia.org/wiki/Visitor_pattern">visitor</a> that can be used to traverse a
 * <a href="https://en.wikipedia.org/wiki/Tree_(data_structure)">tree</a> of {@link Node}{@code s}.
 *
 * <p>
 *   <b>NOTE: this visitor doesn't support polymorphism, and is therefore most useful for trees with only 1 type of node.</b>
 *   For polymorphic trees, it might be more useful to create a custom visitor type
 *   (see {@link com.google.gwt.dev.js.ast.JsSuperVisitor} for an example).
 * </p>
 *
 * @param <N> The supertype for the nodes in the type of tree on which this visitor operates.
 * @see Node
 * @author Alex, 10/31/2017
 */
public interface Visitor<N extends Node> {

  void beginVisit(N node);
  void visit(N node);
  void endVisit(N node);

  TraversalStrategy getStrategy();
}
