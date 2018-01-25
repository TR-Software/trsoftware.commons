package solutions.trsoftware.commons.shared.util.text;

import com.google.gwt.regexp.shared.MatchResult;

import java.util.ArrayList;

/**
 * @author Alex
 * @since 12/25/2017
 */
public class RegexUtils {

  public static ArrayList<String> getGroups(MatchResult matchResult) {
    int n = matchResult.getGroupCount();
    ArrayList<String> groups = new ArrayList<String>(n);
    for (int i = 0; i < n; i++)
      groups.add(matchResult.getGroup(i));
    return groups;
  }

}
