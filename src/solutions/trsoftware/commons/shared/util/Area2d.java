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

package solutions.trsoftware.commons.shared.util;

import com.google.gwt.text.shared.AbstractRenderer;

import java.io.Serializable;
import java.text.ParseException;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author Alex, 9/20/2017
 */
public class Area2d implements Serializable, Comparable<Area2d> {

  public static final Area2d EMPTY_AREA = new Area2d(0, 0);

  private int width, height;

  /** A square area with the given side dimension */
  public Area2d(int side) {
    this(side, side);
  }

  public Area2d(int width, int height) {
    checkArgument(width >= 0, "Negative width (%s)", width);
    checkArgument(height >= 0, "Negative height (%s)", height);
    this.width = width;
    this.height = height;
  }

  private Area2d() {
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getArea() {
    return width * height;
  }

  @Override
  public String toString() {
    return "" + width + "x" + height;
  }

  public static Area2d parse(String str) {
    if (str.isEmpty())
      return EMPTY_AREA;
    String widthStr;
    String heightStr;
    int xIdx = str.indexOf('x');
    if (xIdx >= 0) {
      widthStr = str.substring(0, xIdx);
      heightStr = str.substring(xIdx+1);
    }
    else {
      // there's no 'x' symbol in the string, so we infer that both width and height are the same
      widthStr = heightStr = str;
    }
    if (widthStr.isEmpty())
      widthStr = "0";
    if (heightStr.isEmpty())
      heightStr = "0";
    return new Area2d(Integer.parseInt(widthStr), Integer.parseInt(heightStr));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Area2d area2d = (Area2d)o;

    if (width != area2d.width) return false;
    return height == area2d.height;
  }

  @Override
  public int hashCode() {
    int result = width;
    result = 31 * result + height;
    return result;
  }

  @Override
  public int compareTo(Area2d o) {
    return getArea() - o.getArea();
  }

  private static Renderer renderer;
  private static Parser parser;
  
  
  public synchronized static Renderer getRenderer() {
    if (renderer == null)
      renderer = new Renderer();
    return renderer;
  }
  
  
  public synchronized static Parser getParser() {
    if (parser == null)
      parser = new Parser();
    return parser;
  }
  
  public static class Renderer extends AbstractRenderer<Area2d> implements Serializable {
    @Override
    public String render(Area2d object) {
      // to support all possible usages of the renderer in GWT widgets (such as ValueListBox), it must accept null values
      if (object == null)
        return "";
      return object.toString();
    }
  }

  public static class Parser implements com.google.gwt.text.shared.Parser<Area2d>, Serializable{
    @Override
    public Area2d parse(CharSequence text) throws ParseException {
      try {
        return Area2d.parse(text.toString());
      }
      catch (Exception e) {
        throw new ParseException(e.getMessage(), 0);
      }
    }
  }
}
