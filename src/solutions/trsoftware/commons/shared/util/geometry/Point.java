/*
 * Copyright 2025 TR Software Inc.
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

package solutions.trsoftware.commons.shared.util.geometry;

/**
 * @author Alex
 * @since 12/18/2017
 */
public interface Point {

  double getX();
  double getY();

  /**
   * Creates an instance of the default {@link Point} implementation class.
   * This factory method can be used with {@code import static} for less-verbose code.
   *
   * @return a point representing the given {@code (x, y)} coordinates
   */
  // TODO: GWT legacy DevMode doesn't support static methods in interface
  static Point point(double x, double y) {
    return new RealPoint(x, y);
  }
}
