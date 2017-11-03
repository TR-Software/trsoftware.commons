package solutions.trsoftware.commons.server.stats;

import junit.framework.TestCase;
import solutions.trsoftware.commons.client.util.StringUtils;
import solutions.trsoftware.commons.shared.util.trees.AbstractVisitor;
import solutions.trsoftware.commons.shared.util.trees.Node;
import solutions.trsoftware.commons.shared.util.trees.TraversalStrategy;

/**
 * Mar 23, 2011
 *
 * @author Alex
 */
public class HierarchicalCounterTest extends TestCase {


  public void testCounting() throws Exception {
    HierarchicalCounter root = new HierarchicalCounter(new SimpleCounter("root"), null);
    HierarchicalCounter c1 = new HierarchicalCounter(new SimpleCounter("c1"), root);
    HierarchicalCounter c1_1 = new HierarchicalCounter(new SimpleCounter("c1_1"), c1);
    HierarchicalCounter c1_2 = new HierarchicalCounter(new SimpleCounter("c1_2"), c1);
    HierarchicalCounter c1_3 = new HierarchicalCounter(new SimpleCounter("c1_3"), c1);
    HierarchicalCounter c2 = new HierarchicalCounter(new SimpleCounter("c2"), root);
    HierarchicalCounter c2_1 = new HierarchicalCounter(new SimpleCounter("c2_1"), c2);
    HierarchicalCounter c2_2 = new HierarchicalCounter(new SimpleCounter("c2_2"), c2);

    printCounters(root, "Counters initialized");
    // all counters should be at 0 before we start counting
    {
      Assert0Visitor visitor = new Assert0Visitor();
      root.accept(visitor);
      assertEquals(8, visitor.nVisited);
    }

    // now test incrementing some of the counters
    root.incr();
    printCounters(root, "root++");
    assertEquals(1, root.getCount());
    // the rest of the counters should still be at 0
    {
      Assert0Visitor visitor = new Assert0Visitor();
      c1.accept(visitor);
      c2.accept(visitor);
      assertEquals(7, visitor.nVisited);
    }

    c1.incr();
    printCounters(root, "c1++");
    assertEquals(2, root.getCount());
    assertEquals(1, c1.getCount());
    // the rest of the counters should still be at 0
    {
      Assert0Visitor visitor = new Assert0Visitor();
      for (Node<Counter> c1Child : c1.getChildren()) {
        c1Child.accept(visitor);
      }
      c2.accept(visitor);
      assertEquals(6, visitor.nVisited);
    }

    c2.decr();
    printCounters(root, "c2--");
    assertEquals(1, root.getCount());
    assertEquals(1, c1.getCount());
    assertEquals(-1, c2.getCount());
    // the rest of the counters should still be at 0
    {
      Assert0Visitor visitor = new Assert0Visitor();
      for (Node<Counter> c1Child : c1.getChildren()) {
        c1Child.accept(visitor);
      }
      for (Node<Counter> c2Child : c2.getChildren()) {
        c2Child.accept(visitor);
      }
      assertEquals(5, visitor.nVisited);
    }


    c1_3.incr();
    printCounters(root, "c1_3++");
    assertEquals(1, c1_3.getCount());
    assertEquals(0, c1_2.getCount());
    assertEquals(0, c1_1.getCount());
    assertEquals(2, c1.getCount());
    assertEquals(2, root.getCount());
    assertEquals(-1, c2.getCount());
    assertEquals(0, c2_1.getCount());
    assertEquals(0, c2_2.getCount());
  }


  public static void printCounters(HierarchicalCounter root, String headerMsg) {
    if (headerMsg == null)
      headerMsg = root.getName();
    char hBorderChar = '\u2550';
    String topBorderSegment = StringUtils.repeat(hBorderChar, 6);
    String header = topBorderSegment + " (" + headerMsg + ") " + topBorderSegment;
    System.out.println(header);
    root.accept(new PrintVisitor(TraversalStrategy.PRE_ORDER));
    System.out.println(StringUtils.repeat(hBorderChar, header.length()));
  }

  private static class PrintVisitor extends AbstractVisitor<HierarchicalCounter> {

    public PrintVisitor(TraversalStrategy strategy) {
      super(strategy);
    }

    @Override
    public void visit(HierarchicalCounter node) {
      System.out.println(StringUtils.repeat(' ', node.getLevel()*2) + "+ \"" + node.getName() + "\": " + node.getCount());
    }
  }

  /**
   * Asserts that all counts in the given hierarchy are {@code 0}.
   */
  private static class Assert0Visitor extends AbstractVisitor<HierarchicalCounter> {
    private int nVisited;
    @Override
    public void visit(HierarchicalCounter node) {
      nVisited++;
      assertEquals(0, node.getCount());
    }
  }
}