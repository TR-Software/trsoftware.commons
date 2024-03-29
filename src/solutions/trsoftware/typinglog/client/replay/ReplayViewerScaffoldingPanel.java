/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.typinglog.client.replay;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import solutions.trsoftware.commons.shared.text.TypingLog;
import solutions.trsoftware.commons.shared.text.TypingLogFormatV1;
import solutions.trsoftware.commons.shared.util.StringUtils;

import java.util.Arrays;
import java.util.List;

import static solutions.trsoftware.commons.client.widgets.Widgets.horizontalPanel;
import static solutions.trsoftware.commons.client.widgets.Widgets.verticalPanel;

/**
 * Provides a scaffolding for experimenting with the TypingReplayPlayer
 * during its development.
 * 
 * Nov 24, 2012
 *
 * @author Alex
 */
public class ReplayViewerScaffoldingPanel extends Composite {

  private static ReplayViewerScaffoldingPanel instance;

  public static ReplayViewerScaffoldingPanel getInstance() {
    if (instance == null)
      instance = new ReplayViewerScaffoldingPanel();
    return instance;
  }

//  private TextBox txtLog = textBox(40, Integer.MAX_VALUE);
  private TextArea txtLog = new TextArea();
  {
    txtLog.setCharacterWidth(80);
    txtLog.setVisibleLines(4);
  }
  private ListBox lstPresets = new ListBox();

  private List<String> typingLogPresetValues = Arrays.asList(
      // set the default values to be the current problem we're trying to solve
      "TLv1,en,184,T443h2028e190 243p0i193c103k184p320o251c517k290e135t319s160'697 0p238r97o177f151e393s0s237i154o83n100 98d1229e0m0a0n0d0s200 0e131x249t271r225a93o321r203d330i164n223a121r345y0 111n255i216m216b144l263e137n295e1620s389s218 222a267n0d68 344a0g200i349l229i0t135y116,292 130a129 126t247e176r128r235i326f0i134c148 156c697e111r242t320a0i177n198t364y927 0o220f113 91m222o143v92e313m1083e111n472t88,347 110n225o168t75 81t290o0 100m337e93n185t101i137o170n0 94a251 134r286e0a178d160y238 98w263i136t311,616 137a344 326t355a143l1600e136n316t65 395f207o274r0 118o394b71s433e0r104v407a154t249i133o68n124 249a374n696d1264 448s889t122r1045a95i410n264e113d318 161a494t193t257e113n376t89i318o0n3487.0|0,14,443,0+T,126,1+H,162,2+e,123,3+ ,166,4+p,140,5+i,312,5-i,185,4-p,159,3- ,156,2-e,163,1-H,336,1+h,190,2+e,243,3+ 4+p,4,11,193,1+i,103,2+c,184,3+k,320,4+p,251,5+o,517,6+c,290,7+k,135,8+e,319,9+t,160,10+s,697,11+'12+ ,17,10,238,0+p,97,1+r,177,2+o,151,3+f,393,4+e5+s,237,5+s,154,7+i,83,8+o,100,9+n,98,10+ ,28,2,1229,0+d1+e2+m3+a4+n5+d,200,6+s7+ ,36,13,131,0+e,249,1+x,271,2+t,225,3+r,93,4+a,321,5+o,203,6+r,330,7+d,164,8+i,223,9+n,121,10+a,345,11+r12+y,111,13+ ,50,16,255,0+n,216,1+i,216,2+m,144,3+b,263,4+l,137,5+e,295,6+n,252,7+s8+e,262,9+s,524,9-s,202,8-e,186,7-s,194,7+e,389,8+s,218,8+s,222,10+ ,61,3,267,0+a1+n,68,2+d,344,3+ 4+a,65,7,200,1+g,349,2+i,229,2+i3+l,135,5+t,116,6+y,292,7+,,130,8+ ,74,2,129,0+a,126,1+ ,76,8,247,0+t,176,1+e,128,2+r,235,2+r,326,4+i5+f,134,6+i,148,7+c,156,8+ ,85,10,697,0+c,111,1+e,242,2+r,320,3+t4+a,177,5+i,198,6+n,364,7+t,155,8+l,399,8-l,373,8+y9+ ,95,3,220,0+o,113,1+f,91,2+ ,98,10,222,0+m,143,1+o,92,2+v,313,3+e,1083,4+m,111,5+e,472,6+n,88,7+t,347,8+,,110,9+ ,108,4,225,0+n,168,1+o,75,2+t,81,3+ ,112,2,290,0+t1+o,100,2+ ,115,7,337,0+m,93,1+e,185,2+n,101,3+t,137,4+i,170,5+o6+n,94,7+ ,123,2,251,0+a,134,1+ ,125,5,286,0+r1+e,178,2+a,160,3+d,238,4+y,98,5+ ,131,5,263,0+w,136,1+i,311,2+t,616,3+,,137,4+ ,136,2,344,0+a,326,1+ ,138,8,355,0+t,143,1+a,423,1-a,1177,1+a2+l,136,3+e,316,4+n,65,5+t,395,6+ ,145,3,207,0+f,274,1+o2+r,118,3+ ,149,11,394,0+o,71,1+b,433,2+s3+e,104,4+r,407,5+v,154,6+a,249,7+t,133,8+i,68,9+o,124,10+n,249,11+ ,161,6,374,0+a,696,1+n,352,1-n,529,1+n,383,2+d,448,3+ ,165,15,266,0+a,518,0-a,105,0+s,122,1+t,202,2+a,124,3+r,400,3-r,182,2-a,137,2+r,95,3+a,410,4+i,264,5+n,113,6+e,318,7+d,161,8+ ,174,8,494,0+a,193,1+t,257,1+t,113,3+e,376,4+n,89,5+t,318,6+i7+o,3487,8+n9+.,",
      "TLv1,en,172,C4225o375m285f831o0r660t0a135b669l257e180,1521 15720M0u0l0l0e2837t0?0 0I-27695t0 0s0e0e0m0s0 0s0a0d0l0y0 0i0r0o0n0i0c0 0t0h0a0t0 0i0t0'0s0 0t0h0a0t0 0t0i0e0 0t0h0a0t0'0s0 0g0o0t0 0y0o0u0 0i0n0t0o0 0t0h0i0s0 0p0i0c0k0l0e0.0 0N0o0w0 0y0o0u0 0j0u0s0t0 0t0a0k0e0 0a0l0l0 0t0h0e0 0t0i0m0e0 0y0o0u0 0w0a0n0t0.0 0Y0o0u0 0c0a0n0 0p0l0a0y0 0s0o0m0e0 0m0u0s0i0c0 0i0f0 0y0o0u0 0l0i0k0e0.0|0,19,4225,0+C,375,1+o,285,2+m,831,3+f4+o,660,5+r6+t,135,7+a,669,8+b,257,9+l,180,10+e,1521,11+,,491,12+M,278,13+u,342,14+l15+l,1028,14+l,163,17+e,1287,18+t,530,19+?,3493,20+ ,8108,12+ ,13,1,2837,2-l,"
  );

  private SimplePanel replayPlayerHolder = new SimplePanel();
  private TypingLogReplayPlayer replayPlayer;

  public ReplayViewerScaffoldingPanel() {
    this("");
  }

  /**
   * @param typingLogStr the initial value to assign the typing log input text box
   */
  public ReplayViewerScaffoldingPanel(String typingLogStr) {
    initWidget(verticalPanel(
        horizontalPanel(
            new Label("Log: "), txtLog,
            new Button("Start Replay", new ClickHandler() {
              public void onClick(ClickEvent event) {
                startReplay();
              }
            })),
        lstPresets,
        replayPlayerHolder
    ));
    for (int i = 0; i < typingLogPresetValues.size(); i++) {
      String presetValue = typingLogPresetValues.get(i);
      TypingLog presetLog = TypingLogFormatV1.parseTypingLog(presetValue);
      lstPresets.addItem("Preset " + i + " (" + presetLog.getText() + ")", presetValue);
    }
    lstPresets.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        txtLog.setText(lstPresets.getValue(lstPresets.getSelectedIndex()));
        startReplay();
      }
    });

    txtLog.setText(StringUtils.notBlank(typingLogStr) ? typingLogStr : lstPresets.getValue(0));
  }

  private void startReplay() {
    if (replayPlayer != null)
      replayPlayer.stop();
    TypingLog typingLog = TypingLogFormatV1.parseTypingLog(txtLog.getText());
    replayPlayer = new TypingLogReplayPlayer(typingLog);
    replayPlayerHolder.setWidget(replayPlayer);
//                replayPlayer.play();
  }

  public void setLog(TypingLog typingLog) {
    txtLog.setText(TypingLogFormatV1.formatTypingLog(typingLog));
  }
}
