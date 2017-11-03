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

package solutions.trsoftware.commons.client.util.geometry;

import com.google.gwt.user.client.ui.Widget;

/**
 * Defines a position of a widget on screen relative to another widget which we call the "pivot".
 *
 * @author Alex, 11/17/2014
 */
public class RelativePosition {

  private final Widget pivot;
  private final int offsetX;
  private final int offsetY;
  private final Alignment[] alignmentPrefs;

  public RelativePosition(Widget pivot, int offsetX, int offsetY,
                          Alignment... alignmentPrefs) {
    this.pivot = pivot;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.alignmentPrefs = alignmentPrefs;
  }

  public RelativePosition(Widget pivot,
                          Alignment... alignmentPrefs) {
    this(pivot, 0, 0, alignmentPrefs);
  }

  /** Factory method */
  public static RelativePosition nextTo(Widget pivot) {
    return new RelativePosition(pivot, Alignment.NEXT_TO);
  }


  public Widget getPivot() {
    return pivot;
  }

  public int getOffsetX() {
    return offsetX;
  }

  public int getOffsetY() {
    return offsetY;
  }

  public Alignment[] getAlignmentPrefs() {
    return alignmentPrefs;
  }
}
