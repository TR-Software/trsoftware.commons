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
