package solutions.trsoftware.commons.client.jso;

/**
 * Overlays the native browser <a href="https://developer.mozilla.org/en-US/docs/Web/API/Selection">Selection</a> object.
 *
 * A Selection object represents the range of text selected by the user or the current position of the caret. To obtain a Selection object for examination or modification, call window.getSelection().
 *
 * A user may make a selection from left to right (in document order) or right to left (reverse of document order).
 *
 * The anchor is where the user began the selection and the focus is where the user ends the selection.
 *
 * If you make a selection with a desktop mouse, the anchor is placed where you pressed the mouse button and the focus is placed where you released the mouse button. Anchor and focus should not be confused with the start and end positions of a selection, since anchor can be placed before the focus or vice versa, depending on the direction you made your selection.
 *
 * @author Alex, 10/6/2015
 */
public class JsSelection extends JsObject {

  protected JsSelection() {
  }

  /**
   * @return the <a href="https://developer.mozilla.org/en-US/docs/Web/API/Node">Node</a> in which the selection begins.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Selection/anchorNode">Selection.anchorNode</a>
   */
  public final JsObject getAnchorNode() {
    return (JsObject)getObject("anchorNode");
  }

  /**
   * @return the offset of the selection's anchor within the anchorNode. If anchorNode is a text node, this is the
 * number of characters within anchorNode preceding the anchor. If anchorNode is an element, this is the number of
   * child nodes of the anchorNode preceding the anchor.  The anchor is where the user began the selection.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Selection/anchorOffset">Selection.anchorOffset</a>
   */
  public final int getAnchorOffset() {
    return (int)getNumber("anchorOffset");
  }

  /**
   * @return the <a href="https://developer.mozilla.org/en-US/docs/Web/API/Node">Node</a> in which the selection ends.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Selection/focusNode">Selection.focusNode</a>
   */
  public final JsObject getFocusNode() {
    return (JsObject)getObject("focusNode");
  }

  /**
   * @return the offset of the selection's focus within the focusNode. If focusNode is a text node, this is the
   * number of characters within focusNode preceding the focus. If focusNode is an element, this is the number of
   * child nodes of the focusNode preceding the focus.  The focus is where the user ends the selection.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Selection/focusOffset">Selection.focusOffset</a>
   */
  public final int getFocusOffset() {
    return (int)getNumber("focusOffset");
  }

}
