package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

public class ImageHyperlink extends Composite {

  public ImageHyperlink(String href, final String imgSrc, final String imgSrcMouseover) {
    SimplePanel pnlMain = new SimplePanel();
    initWidget(pnlMain);

    Element a = DOM.createAnchor();
    DOM.setElementAttribute(a, "href", href);

    final Element img = DOM.createImg();
    DOM.setElementAttribute(img, "src", imgSrc);
    DOM.setElementAttribute(img, "border", "0");

    DOM.appendChild(a, img);
    DOM.appendChild(pnlMain.getElement(), a);

    DOM.sinkEvents(img, DOM.getEventsSunk(img) | Event.MOUSEEVENTS);

    if (imgSrcMouseover != null && !imgSrc.equals(imgSrcMouseover)) {
      DOM.setEventListener(img, new EventListener() {
        public void onBrowserEvent(Event event) {
          switch (DOM.eventGetType(event)) {
            case Event.ONMOUSEOVER:
              DOM.setElementAttribute(img, "src", imgSrcMouseover);
              break;
            case Event.ONMOUSEOUT:
              DOM.setElementAttribute(img, "src", imgSrc);
              break;
          }
        }
      });
    }
  }
}
