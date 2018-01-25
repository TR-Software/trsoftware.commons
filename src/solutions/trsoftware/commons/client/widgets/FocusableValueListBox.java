package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.view.client.ProvidesKey;

/**
 * Exposes the underlying {@link ListBox} and its methods inherited from {@link Focusable}
 * @author Alex
 * @since 12/2/2017
 */
public class FocusableValueListBox<T> extends ValueListBox<T> implements Focusable {

  public FocusableValueListBox(Renderer<T> renderer) {
    super(renderer);
  }

  public FocusableValueListBox(Renderer<T> renderer, ProvidesKey<T> keyProvider) {
    super(renderer, keyProvider);
  }

  public ListBox getListBox() {
    return (ListBox)getWidget();
  }

  public HandlerRegistration addBlurHandler(BlurHandler handler) {
    return getListBox().addBlurHandler(handler);
  }

  public HandlerRegistration addFocusHandler(FocusHandler handler) {
    return getListBox().addFocusHandler(handler);
  }

  @Override
  public int getTabIndex() {
    return getListBox().getTabIndex();
  }

  @Override
  public void setAccessKey(char key) {
    getListBox().setAccessKey(key);
  }

  @Override
  public void setFocus(boolean focused) {
    getListBox().setFocus(focused);
  }

  @Override
  public void setTabIndex(int index) {
    getListBox().setTabIndex(index);
  }

}
