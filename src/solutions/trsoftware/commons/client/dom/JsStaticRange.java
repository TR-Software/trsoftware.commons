/*
 * Copyright 2022 TR Software Inc.
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

package solutions.trsoftware.commons.client.dom;

/**
 * JSNI overlay for the native {@code StaticRange} interface.
 *
 * <blockquote cite="https://developer.mozilla.org/en-US/docs/Web/API/StaticRange">
 * The DOM StaticRange interface extends AbstractRange to provide a method to specify a range of content in the DOM
 * whose contents don't update to reflect changes which occur within the DOM tree.
 * </blockquote>
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/StaticRange">MDN Reference</a>
 * @see <a href="https://dom.spec.whatwg.org/#interface-staticrange">DOM Specification</a>
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/StaticRange#browser_compatibility">Browser
 *     compatibility</a>
 */
public class JsStaticRange extends JsAbstractRange {

  protected JsStaticRange() {
  }
}
