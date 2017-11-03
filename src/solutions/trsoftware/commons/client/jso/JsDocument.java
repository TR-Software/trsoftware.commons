package solutions.trsoftware.commons.client.jso;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Element;

/**
 * Overlays the native browser document object.
 *
 * @author Alex, 10/6/2015
 */
public class JsDocument extends JsObject {

  protected JsDocument() {
  }

  public static JsDocument get() {
    return Document.get().cast();
  }

  /**
   * @return {@code document.activeElement}, which is the currently focused element
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/activeElement#Browser_compatibility">Document.activeElement browser compatibility</a>
   */
  public final Element getActiveElement() {
    return (Element)getObject("activeElement");
  }

  /**
   * @return the result of invoking the JS DOM method <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/getSelection">document.getSelection()</a>
   * or {@code null} if the browser doesn't support this API.
   */
  public final native JsSelection getSelection()/*-{
    if (this.getSelection)
      return this.getSelection();
    return null;
  }-*/;
}
