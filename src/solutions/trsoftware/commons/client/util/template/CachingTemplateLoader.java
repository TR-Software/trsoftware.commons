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
