package solutions.trsoftware.commons.client.dom;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Allows dynamically adding a stylesheet to the DOM.
 * 
 * Jan 9, 2013
 * 
 * @author Alex
 */
public class DynamicStylesheet {
  private JavaScriptObject stylesheet = createDynamicStylesheet();


  public DynamicStylesheet() {
  }

  /** Appends the given css markup to the stylesheet object represented by this class */
  public void append(String css) {
    appendCssToStyleElement(stylesheet, css);
  }

  /**
   * Add a dynamic stylesheet element into the head of the page.
   * @return a reference to that style element (can be used for appending individual rules later)
   */
  // NOTE: this duplicates some code in dynamic_content.js
  private static native JavaScriptObject createDynamicStylesheet() /*-{
    // code borrowed from http://stackoverflow.com/questions/524696/how-to-create-a-style-tag-with-javascript
    var head = $wnd.document.getElementsByTagName('head')[0];
    var style = $wnd.document.createElement('style');
    style.type = 'text/css';
    head.appendChild(style);
    return style;
  }-*/;

  private static native void appendCssToStyleElement(JavaScriptObject style, String css) /*-{
    // code borrowed from http://stackoverflow.com/questions/524696/how-to-create-a-style-tag-with-javascript
    if (style.styleSheet) {
      style.styleSheet.cssText += css;
    } else {
      style.appendChild(document.createTextNode(css));
    }
  }-*/;


}
