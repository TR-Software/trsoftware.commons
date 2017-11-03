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

package solutions.trsoftware.commons.client.event;

/**
 * Date: Nov 14, 2007
* Time: 9:52:53 PM
*
* @author Alex
*/
public class DataChangeEvent<T> {
  /** The old value for the piece of data that changed */
  private T oldData;
  /** The new value for the piece of data that changed */
  private T newData;

  public DataChangeEvent(T oldData, T newData) {
    this.oldData = oldData;
    this.newData = newData;
  }

  public T getOldData() {
    return oldData;
  }

  public T getNewData() {
    return newData;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("DataChangeEvent");
    sb.append("(oldData=").append(oldData);
    sb.append(", newData=").append(newData);
    sb.append(")");
    return sb.toString();
  }
}
