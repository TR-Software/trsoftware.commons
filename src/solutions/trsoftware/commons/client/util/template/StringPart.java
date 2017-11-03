package solutions.trsoftware.commons.client.util.template;

import java.util.Map;

/**
 * Dec 10, 2008
*
* @author Alex
*/
public class StringPart implements TemplatePart {
  private final String str;

  public StringPart(String str) {
    this.str = str;
  }

  public StringBuilder write(StringBuilder buffer, Map<String, String> substitutions) {
    return buffer.append(str);
  }

  @Override
  public String toString() {
    return str;
  }
}
