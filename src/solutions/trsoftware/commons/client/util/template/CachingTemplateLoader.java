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

package solutions.trsoftware.commons.client.util.template;

import solutions.trsoftware.commons.client.cache.CachingFactory;
import solutions.trsoftware.commons.client.util.callables.Function1;

/**
 * Dec 10, 2008
 *
 * @author Alex
 */
public abstract class CachingTemplateLoader {

  /** Flyweight mapping of resource names to parsed templates */
  private final CachingFactory<String, Template> cachedResourceTemplates = new CachingFactory<String, Template>(
      new Function1<String, Template>() {
        public Template call(String name) {
          return loadTemplate(name);
        }
      }
  );

  /** This class should not be instantiated */
  protected CachingTemplateLoader() {
  }

  /** Subclasses should override to implement loading a template by name */
  protected abstract Template loadTemplate(String name);
}
