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

package solutions.trsoftware.commons.client.util;

/**
 * Utilities for interacting with the clipboard using Javascript.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/Interact_with_the_clipboard">
 *   "Interact with the clipboard" guide on MDN</a>
 * @see <a href="https://stackoverflow.com/questions/400212/how-do-i-copy-to-the-clipboard-in-javascript">
 *   "How do I copy to the clipboard in JavaScript?" on StackOverflow</a>
 * @author Alex
 * @since 5/5/2021
 */
public class ClipboardUtils {

  private ClipboardUtils() {
  }

  /*
    TODO: implement support for Clipboard.writeText():
      - https://developer.mozilla.org/en-US/docs/Web/API/Clipboard/writeText
      - https://stackoverflow.com/a/30810322
   */

  /**
   * Determines whether the {@code window.clipboardData.setData} method is available in the current browser.
   * This is true only MSIE browsers (all versions).
   *
   * @return {@code true} if the {@code window.clipboardData.setData} method is available in the current browser.
   */
  public static native boolean canCopyToClipboardUsingClipboardData() /*-{
    return Boolean($wnd.clipboardData && $wnd.clipboardData.setData);
  }-*/;

  /**
   * Copies the given text to the clipboard using {@code window.clipboardData.setData}.
   * This method works only in MSIE browsers (all versions).
   */
  public static native boolean copyToClipboardUsingClipboardData(String text) /*-{
    $wnd.clipboardData.setData('Text', text);
  }-*/;

  /**
   * Determines whether the {@code document.execCommand('copy')} function is available in the current browser.
   *
   * @return {@code true} if the {@code document.execCommand('copy')} function is available in the current browser.
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/execCommand#browser_compatibility">Browser compatibility</a>
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/queryCommandSupported">Document.queryCommandSupported() on MDN</a>
   */
  public static native boolean canCopyToClipboardUsingExecCommand() /*-{
    return Boolean($doc.queryCommandSupported && $doc.queryCommandSupported("copy"));
  }-*/;

  /**
   * Copies the current selection to the clipboard using {@code document.execCommand('copy')}.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Document/execCommand#browser_compatibility">Document.execCommand() on MDN</a>
   */
  public static native boolean copyToClipboardUsingExecCommand() /*-{
    return $doc.execCommand('copy');
  }-*/;
}
