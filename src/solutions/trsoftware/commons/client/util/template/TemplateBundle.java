package solutions.trsoftware.commons.client.util.template;

import java.lang.annotation.*;

/**
 * Dec 11, 2008
 *
 * @author Alex
 */
public interface TemplateBundle {
  /**
   * Explicitly specifies a file name or path to the image resource to be
   * associated with a method in an {@link TemplateBundle}. If the path is
   * unqualified (that is, if it contains no slashes), then it is sought in the
   * package enclosing the image bundle to which the annotation is attached. If
   * the path is qualified, then it is expected that the string can be passed
   * verbatim to <code>ClassLoader.getResource()</code>.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  public @interface Resource {
    String value();
  }
}
