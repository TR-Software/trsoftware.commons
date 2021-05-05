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

package solutions.trsoftware.commons.server.bridge.util;

import com.google.common.net.PercentEscaper;
import com.google.common.net.UrlEscapers;
import solutions.trsoftware.commons.client.bridge.util.URIComponentEncoder;
import solutions.trsoftware.commons.server.servlet.UrlUtils;

/**
 * Server-side implementation of {@link URIComponentEncoder}
 *
 * @author Alex
 */
public class URIComponentEncoderJavaImpl extends URIComponentEncoder {

  private static final URIComponentEncoderJavaImpl instance = new URIComponentEncoderJavaImpl();

  public static URIComponentEncoderJavaImpl getInstance() {
    return instance;
  }

  /**
   * This is similar to {@link UrlEscapers#urlFragmentEscaper()}, but uses a different set of {@code safeChars} to
   * match the behavior of the JavaScript {@code encodeURIComponent} function.
   */
  private final PercentEscaper escaper = new PercentEscaper(
      "!'()*-._~",
      false
  );

  private URIComponentEncoderJavaImpl() {
    // this class is a singleton
  }

  /**
   * {@inheritDoc}
   */
  public String encode(String value) {
    return escaper.escape(value);
  }

  /**
   * {@inheritDoc}
   */
  public String decode(String value) {
    return UrlUtils.urlDecode(value);
  }
}