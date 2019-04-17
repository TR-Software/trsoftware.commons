package solutions.trsoftware.commons.client.dom;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Alex
 * @since 4/12/2019
 */
public class DomUtils {

  /**
   * Wraps the given {@link NodeList} to make it compatible with the Java Collections Framework.
   *
   * @return a new instance of {@link NodeListWrapper} for the given {@link NodeList}
   */
  public static <T extends Node> List<T> asList(@Nonnull NodeList<T> nodeList) {
    return new NodeListWrapper<>(nodeList);
  }
}
