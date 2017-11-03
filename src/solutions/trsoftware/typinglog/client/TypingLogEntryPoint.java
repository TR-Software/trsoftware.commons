package solutions.trsoftware.typinglog.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import solutions.trsoftware.commons.shared.text.TypingLog;
import solutions.trsoftware.commons.shared.text.TypingLogFormatV1;
import solutions.trsoftware.typinglog.client.replay.TypingLogReplayPlayer;

/**
 * @author Alex, 6/8/2017
 */
public class TypingLogEntryPoint implements EntryPoint {
  public void onModuleLoad() {
    String typingLogStr = getHostpageStringVariable("typingLog");
    if (typingLogStr != null) {
      TypingLog typingLog = TypingLogFormatV1.parseTypingLog(typingLogStr);
      RootPanel playerContainer = RootPanel.get("typingLogReplayPlayer");
      if (playerContainer != null)
        playerContainer.add(new TypingLogReplayPlayer(typingLog));
    }
  }

  protected static native String getHostpageStringVariable(String name)/*-{
    return $wnd[name];
  }-*/;
}
