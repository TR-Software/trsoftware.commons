/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.typinglog.client.replay;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.client.jso.JsDocument;
import solutions.trsoftware.commons.client.jso.JsObject;
import solutions.trsoftware.commons.client.jso.JsSelection;
import solutions.trsoftware.commons.client.jso.JsWindow;
import solutions.trsoftware.commons.client.styles.CellPanelStyle;
import solutions.trsoftware.commons.client.widgets.DestroyableComposite;
import solutions.trsoftware.commons.client.widgets.ImageButton;
import solutions.trsoftware.commons.client.widgets.ImageToggleButton;
import solutions.trsoftware.commons.client.widgets.clocks.TimeDisplay;
import solutions.trsoftware.commons.client.widgets.text.TypingSpeedLabel;
import solutions.trsoftware.commons.shared.text.*;
import solutions.trsoftware.commons.shared.util.Levenshtein;
import solutions.trsoftware.gcharts.client.Chart;
import solutions.trsoftware.gcharts.client.Chart.SelectionHandler;
import solutions.trsoftware.gcharts.client.ColumnChart;
import solutions.trsoftware.gcharts.client.DataTable;
import solutions.trsoftware.gcharts.client.GoogleCharts;
import solutions.trsoftware.typinglog.client.replay.icons.PlayerIconsBundle;

import java.util.List;
import java.util.SortedSet;

import static solutions.trsoftware.commons.client.widgets.Widgets.*;

/**
 * Nov 26, 2012
 *
 * @author Alex
 */
public class TypingLogReplayPlayer extends Composite {

  private static final PlayerIconsBundle PLAYER_ICONS_BUNDLE = GWT.create(PlayerIconsBundle.class);

  /** Used to format the accuracy */
  private static final NumberFormat percentFormat = NumberFormat.getFormat("#,##0.#%");

  // the text and log to be animated
  private TypingLog typingLog;
  /** The full text that was typed */
  private String textStr;
  private Language textLanguage;

  // display prefs
  private TypingSpeed.Unit speedFormat;

  // animation prefs
  /** The number of millis to sleep between the frames (sets the frame-rate) */
  private int delayBetweenFrames = 33;  // 33 millis == 30 FPS
  /** The number of seconds skipped by the fast-forward button */
  private int ffSeconds = 5;

  // animation:
  /** Duration of the currently-running animation */
  private Duration animationDuration;
  /** The time in the {@link #replayState} when the current animation was started */
  private int animationStartTime;
  /** The timer which will run the animation */
  private Timer animationTimer = new Timer() {
    public void run() {
      try {
        double animationSpeed = animationSpeedSelector.getAnimationSpeed();
        replayState.seekToTime(animationStartTime + (int)(animationSpeed * animationDuration.elapsedMillis()));
        renderState();
        if (replayState.isReplayFinished())
          stop();
      }
      catch (RuntimeException ex) {
        // kill the animation if any exception occurs while rendering the next frame
        stop();
        // TODO: handle exceptions
        throw ex;
      }
    }
  };

  /** The current state of the replay (e.g. time, position, the entered text */
  private TypingLogReplayState replayState;


  // UI elements
  /** Displays the original text, highlighting all the correct chars up to the current point of the replay */
  private TextDisplay textDisplay;
  /** Displays the actual typing edit buffer */
  private TypingEditDisplay typingEditDisplay;
  /** Sets the playback speed of the auto-type animation (e.g. 2x, 4x, .5x) */
  private AnimationSpeedSelector animationSpeedSelector = new AnimationSpeedSelector();
  /** Displays the time (into the race) at the current replay point */
  private TimeDisplay timeDisplay = new TimeDisplay(0, true);
  private IncrementSpeedChart incrementSpeedChart;

  {
    timeDisplay.setStyleName("statusIndicator");
  }
  /** Displays the user's WPM at the current replay point */
  private TypingSpeedLabel wpmDisplay;
  /** Displays the user's WPM at the current replay point */
  private Label accuracyDisplay = new Label();
  {
    accuracyDisplay.setStyleName("statusIndicator");
  }
  // player control buttons:

  private ImageToggleButton btnPlayPause = new ImageToggleButton(PLAYER_ICONS_BUNDLE.play(), "Play", PLAYER_ICONS_BUNDLE.pause(), "Pause", false) {

    /**
     * We want to be able toggle the button both manually (UI click) as well as programmatically (e.g. when
     * activating the stepper needs to pause the replay animation), hence we override this
     * method instead of the {@link #toggle(boolean)} method.  This enables us to have all control of playback
     * go through the enclosing {@link TypingLogReplayPlayer} class, making the embedded instance of {@link ImageToggleButton}
     * just a dumb UI element, whose display state changes in accordance with the state of the playback.
     */
    @Override
    public void onClick(ClickEvent event) {
      super.onClick(event);  // let the superclass handle updating the toggle state
      // just handle the playback state transition here in response to the change in the toggle state of the button
      if (on)
        play();
      else
        stop();
    }
  };

  public TypingLogReplayPlayer(final TypingLog typingLog) {
    this(typingLog, TypingSpeed.Unit.WPM);
  }

  public TypingLogReplayPlayer(final TypingLog typingLog, TypingSpeed.Unit speedFormat) {
    this.typingLog = typingLog;
    this.textStr = typingLog.getText();
    this.textLanguage = typingLog.getTextLanguage();
    this.speedFormat = speedFormat;
    this.replayState = new TypingLogReplayState(typingLog);
    wpmDisplay = new TypingSpeedLabel(speedFormat, 0);
    wpmDisplay.setStyleName("statusIndicator");
    initWidget(verticalPanel(
        label("Race text:", "lblSectionHeading"),
        textDisplay = new TextDisplay(),
        horizontalPanel(
            label("Time:", "lblStatusIndicator"),
            timeDisplay,
            label("Speed:", "lblStatusIndicator"),
            wpmDisplay,
            label("Accuracy:", "lblStatusIndicator"),
            accuracyDisplay
        ),
        // player controls:
        horizontalPanel(
            btnPlayPause,
            animationSpeedSelector,
            new ImageButton(PLAYER_ICONS_BUNDLE.rew_full(), "Reset", new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                stop();
                replayState.reset();
                renderState();
              }
            }),
            new ImageButton(PLAYER_ICONS_BUNDLE.rew(), "Rewind " + ffSeconds + " seconds", new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                seekToTimeOffset(-(TypingLogReplayPlayer.this.ffSeconds*1000));
              }
            }),
            new ImageButton(PLAYER_ICONS_BUNDLE.ff(), "Fast forward " + ffSeconds + " seconds", new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                seekToTimeOffset(TypingLogReplayPlayer.this.ffSeconds*1000);
              }
            }),
            new ImageButton(PLAYER_ICONS_BUNDLE.ff_full(), "Fast forward to the end", new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                stop();
                replayState.seekToEnd();
                renderState();
              }
            })
        ),
        label("Typing replay:", "lblSectionHeading"),
        typingEditDisplay = new TypingEditDisplay(),
        horizontalPanel(
            // move 1 step backward in the edit log
            new ImageButton(PLAYER_ICONS_BUNDLE.step_back(), "Previous edit", new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                stop();
                replayState.seekToEditCursor(replayState.getEditCursor() - 1);
                renderState();
              }
            }),
            // move 1 step forward in the edit log
            new ImageButton(PLAYER_ICONS_BUNDLE.step_forward(), "Next edit", new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                stop();
                replayState.seekToEditCursor(replayState.getEditCursor() + 1);
                renderState();
              }
            })
        ),
        label("Analysis:", "lblSectionHeading"),
        horizontalPanel(new CellPanelStyle().setWidth("100%"),
            new WordsWithErrors(),
            incrementSpeedChart = new IncrementSpeedChart()
        )
    ));
    setStyleName("TypingLogReplayPlayer");
  }

  private void seekToTimeOffset(int timeOffsetMillis) {
    boolean resumeAnimationAfterSeek = isPlaying();
    stop();
    replayState.seekToTime(replayState.getTime() + timeOffsetMillis);
    renderState();
    if (resumeAnimationAfterSeek)
      play();
  }

  private void seekToCharCursor(int charPosWithinText) {
    boolean resumeAnimationAfterSeek = isPlaying();
    stop();
    replayState.seekToCharCursor(charPosWithinText);
    renderState();
    if (resumeAnimationAfterSeek)
      play();
  }

  /**
   * Run the automatic replay animation from the current point in the replay.
   */
  public void play() {
    stop();  // kill the previous animation before starting a new one
    if (!replayState.isReplayFinished()) { // start the timer only if there's something to animate
      animationDuration = new Duration();
      animationStartTime = replayState.getTime();
      animationTimer.scheduleRepeating(delayBetweenFrames);
      btnPlayPause.toggle(true);
    }
  }

  public void stop() {
    btnPlayPause.toggle(false);
    animationTimer.cancel();
    animationDuration = null;
  }

  public boolean isPlaying() {
    return animationDuration != null;
  }

  public TypingSpeed.Unit getSpeedFormat() {
    return speedFormat;
  }

  public void setSpeedFormat(TypingSpeed.Unit speedFormat) {
    this.speedFormat = speedFormat;
    wpmDisplay.setUnit(speedFormat);
    incrementSpeedChart.drawChart();
    // TODO: cont here: update the chart and the speed display label
  }

  private void renderState() {
    textDisplay.update();
    typingEditDisplay.update();
    int time = replayState.getTime();
    timeDisplay.setTime(time);
    wpmDisplay.setValue(new TypingSpeed(replayState.getCharCursor(), time, textLanguage));
    double accuracy = replayState.getAccuracy();
    // when replayState is at its initial position, the accuracy it returns is NaN
    if (Double.isNaN(accuracy))
      accuracyDisplay.setText("N/A");
    else
      accuracyDisplay.setText(percentFormat.format(accuracy));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    // give typingEditDisplay the same height as textDisplay (to avoid the other UI objects jumping around when more text is typed (entered into typingEditDisplay)
    // we do this in a deferred command to be sure that the height of textDisplay is fully determined
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        adjustTypingEditDisplayHeight();
      }
    });
  }

  @Override
  protected void onUnload() {
    // make sure the timer will not keep firing when this widget has been removed from the DOM
    stop();
    super.onUnload();
  }

  private void adjustTypingEditDisplayHeight() {
    // give typingEditDisplay the same height as textDisplay (to avoid the other UI objects jumping around when more text is typed (entered into typingEditDisplay)
    // NOTE: this workaround will only work if browser supports Window.getComputedStyle (most modern browsers do)
    Style textDisplayComputedStyle = JsWindow.get().getComputedStyle(textDisplay.getElement());
    String textDisplayComputedHeight = null;
    if (textDisplayComputedStyle != null) {
      textDisplayComputedHeight = textDisplayComputedStyle.getHeight();
    }
    Style typingEditDisplayStyle = typingEditDisplay.getElement().getStyle();
    if (textDisplayComputedHeight != null)
      typingEditDisplayStyle.setProperty("height", textDisplayComputedHeight);
    else
      typingEditDisplayStyle.setHeight(textDisplay.getOffsetHeight(), Style.Unit.PX);
  }

  private class AnimationSpeedSelector extends Composite implements ChangeHandler {
    private ListBox lstAnimationSpeed = new ListBox();

    public AnimationSpeedSelector() {
      lstAnimationSpeed.addItem("1/4x", ".25");
      lstAnimationSpeed.addItem("1/2x", ".5");
      lstAnimationSpeed.addItem("1x", "1");
      lstAnimationSpeed.setSelectedIndex(2);
      lstAnimationSpeed.addItem("2x", "2");
      lstAnimationSpeed.addItem("4x", "4");
      lstAnimationSpeed.addItem("8x", "8");
      lstAnimationSpeed.setTitle("Change playback speed");
      initWidget(lstAnimationSpeed);
      setStyleName("AnimationSpeedSelector");
      lstAnimationSpeed.addChangeHandler(this);
    }

    public double getAnimationSpeed() {
      return Double.parseDouble(lstAnimationSpeed.getValue(lstAnimationSpeed.getSelectedIndex()));
    }

    @Override
    public void onChange(ChangeEvent event) {
      boolean animationPlaying = isPlaying();
      // reset the animation if it's playing (changing the animation speed while it's playing throws things off)
      stop();
      if (animationPlaying)
        play();
    }
  }

  /**
   * Will display the full text in two sections: a span of accepted chars (highlighted accordingly) at the current time point,
   * followed by the rest of the text (not highlighted)
   */
  private class TextDisplay extends Composite implements ClickHandler {
    private InlineLabel acceptedCharsSpan = new InlineLabel();
    private InlineLabel remainingCharsSpan = new InlineLabel();

    private TextDisplay() {
      FlowPanel container = flowPanel(
          acceptedCharsSpan,
          remainingCharsSpan
      );
      initWidget(container);
      setStyleName("TextDisplay");
      addStyleName("TypingDisplaySection");

      acceptedCharsSpan.setStyleName("acceptedChars");
      remainingCharsSpan.setStyleName("remainingChars");

      // TODO: before adding the click handlers, verify that the browser supports the document.getSelection() API: https://developer.mozilla.org/en-US/docs/Web/API/Document/getSelection
      acceptedCharsSpan.addClickHandler(this);
      remainingCharsSpan.addClickHandler(this);

      update();
    }

    /** Places the replay state at the selected char position */
    @Override
    public void onClick(ClickEvent event) {
      JsSelection s = JsDocument.get().getSelection();
      if (s != null) {
        int focusOffset = s.getFocusOffset();
        JsObject focusNode = (JsObject)s.getObject("focusNode");
        Element parentElement = (Element)focusNode.getObject("parentElement");
        int charPosWithinText;
        if (parentElement == acceptedCharsSpan.getElement())
          charPosWithinText = focusOffset;
        else if (parentElement == remainingCharsSpan.getElement())
          charPosWithinText = acceptedCharsSpan.getText().length() + focusOffset;
        else {
          // the parent element of the click (i.e. selection node) is neither of our nested spans, so ignore the click
          System.out.println("WARNING: unrecognized parent element of document.getSelection().focusNode");
          return;
        }
        // now move the replay to the selected char position
        seekToCharCursor(charPosWithinText);
      }
    }

    private void update() {
      // render the current char cursor by updating the text in the two spans (which come before and after it)
      String correctText = textStr.substring(0, replayState.getCharCursor());
      String restOfText = textStr.substring(replayState.getCharCursor());
      acceptedCharsSpan.setText(correctText);
      remainingCharsSpan.setText(restOfText);
    }
  }

  /**
   * Will display the current state of the edit buffer, with errors highlighted
   */
  private class TypingEditDisplay extends Composite {
    private FlowPanel pnlMain = new FlowPanel();

    private TypingEditDisplay() {
      initWidget(pnlMain);
      setStyleName("TypingEditDisplay");
      addStyleName("TypingDisplaySection");
      update();
    }

    public void update() {
      pnlMain.clear();
      String typedText = replayState.getEditBuffer().toString();
      List<Levenshtein.EditOperation> errors = replayState.findErrors();
      int startPos = 0;
      /*
        NOTE: the error list returned by replayState contains edit ops returned by Levenshtein.editSequence,
        so we have to adjust the position of consecutive edits accordingly.  That's what the following correctionOffset
        variable is used for.  Example: if the text is "Comfortable, Mullet?" and the user typed "Comfortable,Mulll",
        then replayState.findErrors() returns [+(11, ' '), $(17, 'e')], but the $'e' should really be applied at pos 16
        in the text typed by the user (it's 17 in the edit sequence because the +(11, ' ') shifts all the subsequent
        chars to the right by 1.
      */
      int correctionOffset = 0;
      if (!errors.isEmpty()) {
        for (Levenshtein.EditOperation error : errors) {
          int pos = error.getPosition() + correctionOffset;
          String correctTextRun = typedText.substring(startPos, pos);
          if (correctTextRun.length() > 0)
            pnlMain.add(inlineLabel(correctTextRun, "correctChars"));
          InlineLabel errCharSpan = inlineLabel(String.valueOf(typedText.charAt(pos)), "errChar");
          pnlMain.add(errCharSpan);
          if (error instanceof Levenshtein.Insertion) {
            // we want to render a vertical line indicating that a char should be inserted at this location
            errCharSpan.addStyleDependentName("ins");
            correctionOffset--;
          }
          else if (error instanceof Levenshtein.Deletion) {
            // we want to render a vertical line indicating that a char should be inserted at this location
            errCharSpan.addStyleDependentName("del");
            correctionOffset++;
          }
          else if (error instanceof Levenshtein.Substitution) {
            // we want to render a vertical line indicating that a char should be inserted at this location
            errCharSpan.addStyleDependentName("sub");
          }
          startPos = pos+1;
        }
      }
      String correctTextRun = typedText.substring(startPos);
      if (correctTextRun.length() > 0)
        pnlMain.add(inlineLabel(correctTextRun, "correctChars"));
    }
  }


  private class WordsWithErrors extends Composite {
    private FlowPanel pnlMain = new FlowPanel();

    private WordsWithErrors() {
      initWidget(disclosurePanel("Mistakes:", true, pnlMain));
      setStyleName("WordsWithErrors");
      replayState.seekToEnd();  // go to end of the replay because at that point it has the full set of words containing errors
      SortedSet<TypingLogReplayState.Word> wordsWithErrors = replayState.getWordsWithErrors();
      replayState.reset();
      if (wordsWithErrors.isEmpty())
        setVisible(false);  // don't show this panel when there are no errors
      else {
        for (final TypingLogReplayState.Word word : wordsWithErrors) {
          HTML lblWord = new HTML("&uarr; " + word.getWordStr());
          lblWord.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              boolean resumeAnimationAfterSeek = isPlaying();
              stop();
              replayState.seekToEditCursor(word.getEditCursor());
              renderState();
              if (resumeAnimationAfterSeek)
                play();
              renderState();
            }
          });
          lblWord.setStyleName("replayWord");
          lblWord.setTitle("click on the word to jump there in the replay");
          pnlMain.add(lblWord);
        }
      }
    }
  }

  private class IncrementSpeedChart extends DestroyableComposite {

    private ColumnChart columnChart;
    private final List<TypingLogAnalyzer.TextSegment> dataPoints;

    private IncrementSpeedChart() {
      initWidget(new SimplePanel());
      TypingLogAnalyzer typingLogAnalyzer = new TypingLogAnalyzer(typingLog);
      dataPoints = typingLogAnalyzer.getSegmentWPMs(8);
      GoogleCharts.get().runWhenLoaded(new Command() {
          @Override
          public void execute() {
            columnChart = ColumnChart.create(getElement());
            columnChart.addSelectionHandler(new SelectionHandler() {
              @Override
              public void onSelection(Chart.Selection selection) {
                int row = selection.getRow();
                if (row >= 0) {
                  seekToCharCursor(dataPoints.get(row).getStartPos());
                }
              }
            });
            drawChart();
          }
        });
    }

    private void drawChart() {
      if (dataPoints.size() < 2)
        return;  // it doesn't make sense to display this segment chart if there are fewer than 2 segments
      DataTable dataTable = DataTable.create();
      dataTable.addColumn("number", "Segment");
      dataTable.addColumn("number", speedFormat.name());
      for (int i = 0; i < dataPoints.size(); i++) {
        TypingLogAnalyzer.TextSegment textSegment = dataPoints.get(i);
        dataTable.addRow();
        dataTable.setCell(i, 0, i + 1, textSegment.getTextStr());
        double speedValue = TypingSpeed.Unit.WPM.to(speedFormat, textSegment.getWpm(), typingLog.getTextLanguage());
        dataTable.setCell(i, 1, speedValue, null);
      }
      columnChart.draw(dataTable, createChartOptions());
    }

    /**
     * Returns a new object.
     */
    private native JavaScriptObject createChartOptions() /*-{
      return {
        title: 'Speed Throughout the Race',
        hAxis: {
          title: 'Segment',
          viewWindow: {
            min: 0,
            max: 9
          },
          ticks: [1,2,3,4,5,6,7,8]
        },
        vAxis: {
          title: 'Speed'
        },
        legend: 'none',
        width: 400,
        height: 200
      };
    }-*/;
  }

}