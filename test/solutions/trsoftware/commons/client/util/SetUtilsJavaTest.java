package solutions.trsoftware.commons.client.util;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.LinkedHashSet;

import static solutions.trsoftware.commons.client.util.SetUtils.*;

public class SetUtilsJavaTest extends TestCase {

  public void testParse() throws Exception {
    assertEquals(newSet("a", "b", "c"), parse("a,b,c"));
    assertEquals(newSet("a", "b", "c"), parse("  a  ,b,   c"));
    assertEquals(newSet("a", "b", "c"), parse("a,  b  ,c,,"));
    assertEquals(newSet("a"), parse("a"));
    assertEquals(newSet("a"), parse("  a  "));
    assertEquals(newSet("a"), parse("  a , , ,,, "));
    assertEquals(Collections.<String>emptySet(), parse(""));
    assertEquals(Collections.<String>emptySet(), parse("  "));
    assertEquals(Collections.<String>emptySet(), parse("  , , ,,, "));
  }

  public void testPrint() throws Exception {
    assertEquals("a,b,c", print(newSet("a", "b", "c")));
    assertEquals("a", print(newSet("a")));
    assertEquals("", print(newSet()));
  }
}