package solutions.trsoftware.commons.server.util.xml.dom;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Provides {@code static} utility methods for working with the {@link org.w3c.dom} API.
 *
 * @author Alex
 * @since 3/13/2018
 */
public class XmlDomUtils {

  /**
   * @return the first {@link Element} from the given {@link NodeList}.
   * @throws IllegalArgumentException if the given {@link NodeList} is empty or contains more than 1 item
   * @throws ClassCastException if the item contained by the given {@link NodeList} is not an {@link Element}
   */
  public static Element getSingletonElement(NodeList nodeList) {
    if (nodeList.getLength() != 1)
      throw new IllegalArgumentException("The given NodeList is not a singleton");
    return (Element)nodeList.item(0);
  }
}
