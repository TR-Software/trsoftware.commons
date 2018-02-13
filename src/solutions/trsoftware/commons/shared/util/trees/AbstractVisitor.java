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
 * Base class for a {@link Visitor} that defines a {@link TraversalStrategy}
 *
 * @author Alex, 10/31/2017
 */
public abstract class AbstractVisitor<N extends Node> implements Visitor<N> {

  protected final TraversalStrategy strategy;

  public AbstractVisitor(TraversalStrategy strategy) {
    this.strategy = strategy;
  }

  public AbstractVisitor() {
    this(null);
  }

  @Override
  public TraversalStrategy getStrategy() {
    return strategy;
  }

  @Override
  public void beginVisit(N node) {
    // subclasses should override
  }

  @Override
  public void endVisit(N node) {
    // subclasses should override
  }
}
