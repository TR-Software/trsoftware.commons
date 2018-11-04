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

package solutions.trsoftware.commons.shared.util.template;

import com.google.gwt.core.client.GWT;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.rebind.util.template.TemplateBundleGenerator;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;

/**
 * Tests a {@link TemplateBundle} generated with {@link TemplateBundleGenerator}
 *
 * @author Alex
 * @since 3/20/2018
 */
public class TemplateBundleTest extends CommonsGwtTestCase {

  public void testTemplates() throws Exception {
    TestBundle bundle = GWT.create(TestBundle.class);

    assertEquals("foo x 123",
        bundle.dummy_template1().render(
            "a", "foo",
            "b", "123").trim());

    // for the next template, we have to verify the result against a regex, because that file might have platform-specific line separators
    AssertUtils.assertThat(
        bundle.fileTemplateParserTest_template().render(
            "NAME", "Foo",
            "ACCT_NUM", "123").trim())
        .matchesRegex("Hello Foo,\\s*Your account number is 123.\\s*Take care!");
  }

  public interface TestBundle extends TemplateBundle {
    /**
     * Parsed from file of the same name in the current package
     */
    Template dummy_template1();

    /**
     * Parsed from a file in another package
     */
    @Resource("solutions/trsoftware/commons/server/util/FileTemplateParserTest_template.txt")
    Template fileTemplateParserTest_template();

  }
}