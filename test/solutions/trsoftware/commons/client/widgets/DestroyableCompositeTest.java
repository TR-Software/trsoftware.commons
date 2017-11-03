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

package solutions.trsoftware.commons.client.widgets;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.client.event.DataChangeEvent;
import solutions.trsoftware.commons.client.event.DataChangeListener;
import solutions.trsoftware.commons.client.event.ListenerSet;
import solutions.trsoftware.commons.client.util.mutable.MutableInteger;

/**
 * Oct 31, 2009
 *
 * @author Alex
 */
public class DestroyableCompositeTest extends CommonsGwtTestCase {

  /**
   * A widget extending DestroyableComposite should run whatever cleanup actions
   * were specified when it's destroyed (i.e. either removed from the view
   * hierarchy or destroy method explicitly called).  Here we make sure the
   * destroy logic is invoked when the widget is removed from the view
   * hierarchy.
   */
  public void testCleanupAfterRemovalFromWidgetHierarchy() throws Exception {
    DestroyableComposite widget = new DestroyableLabel("Foo");
    RootPanel.get().add(widget);
    // this flag should be set by DestroyableComposite.destroy() in this test
    final MutableInteger flag1 = new MutableInteger(0);
    final MutableInteger flag2 = new MutableInteger(0);
    widget.addCleanupAction(new Command() {
      public void execute() {
        flag1.incrementAndGet();
      }
    });
    widget.addCleanupAction(new Command() {
      public void execute() {
        flag2.incrementAndGet();
      }
    });
    assertEquals(0, flag1.get());
    assertEquals(0, flag2.get());
    // make sure that each cleanup action was run exactly once after the widget is removed
    RootPanel.get().remove(widget);
    assertEquals(1, flag1.get());
    assertEquals(1, flag2.get());
  }


  /**
   * Checks that any listeners registered are removed when the widget is removed
   * from the view hierarchy.
   */
  public void testListenerCleanupAfterRemovalFromWidgetHierarchy() throws Exception {
    ListenerSet<String> listenerSet = new ListenerSet<String>();
    final MutableInteger flag1 = new MutableInteger(0);
    final MutableInteger flag2 = new MutableInteger(0);
    final MutableInteger flag3 = new MutableInteger(0);
    final MutableInteger otherFlag = new MutableInteger(0);
    DestroyableComposite widget = new DestroyableLabel("Foo");
    // the first listener will increment either flag1 or flag2
    widget.registerDataChangeListener(listenerSet, new DataChangeListener<String>() {
      public void onChange(DataChangeEvent<String> event) {
        if ("flag1".equals(event.getNewData()))
          flag1.incrementAndGet();
        else if ("flag2".equals(event.getNewData()))
          flag2.incrementAndGet();
        else
          otherFlag.incrementAndGet();
      }
    });
    // the first listener will increment either flag2 or flag3
    widget.registerDataChangeListener(listenerSet, new DataChangeListener<String>() {
      public void onChange(DataChangeEvent<String> event) {
        if ("flag2".equals(event.getNewData()))
          flag2.incrementAndGet();
        else if ("flag3".equals(event.getNewData()))
          flag3.incrementAndGet();
        else
          otherFlag.incrementAndGet();
      }
    });
    // checks that the listeners have been registered but not fired
    assertEquals(2, listenerSet.size());
    assertEquals(0, otherFlag.get());

    RootPanel.get().add(widget);

    // fire some events
    listenerSet.fireChange(new DataChangeEvent<String>(null, "flag1"));
    listenerSet.fireChange(new DataChangeEvent<String>(null, "flag2"));
    assertEquals(1, flag1.get());  // flag 1 was incremented by the first listener
    assertEquals(2, flag2.get());  // flag 2 was incremented by both listeners
    assertEquals(0, flag3.get());
    assertEquals(1, otherFlag.get());  // other flag incremented once by the second listener
    // make sure that each cleanup removes the listeners
    RootPanel.get().remove(widget);
    assertEquals(0, listenerSet.size());
    // any future events should go unnoticed
    listenerSet.fireChange(new DataChangeEvent<String>(null, "flag2"));
    // nothing changed:
    assertEquals(1, flag1.get());
    assertEquals(2, flag2.get());
    assertEquals(0, flag3.get());
    assertEquals(1, otherFlag.get());
  }

  public static class DestroyableLabel extends DestroyableComposite {
    public DestroyableLabel(String text) {
      initWidget(new Label(text));
    }
  }

}