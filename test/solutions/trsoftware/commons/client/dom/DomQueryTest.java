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

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import solutions.trsoftware.commons.client.CommonsGwtTestCase;
import solutions.trsoftware.commons.shared.util.CollectionUtils;
import solutions.trsoftware.commons.shared.util.LogicUtils;
import solutions.trsoftware.commons.shared.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static solutions.trsoftware.commons.client.dom.DomQuery.*;

/**
 * @author Alex
 * @since 12/7/2021
 */
public class DomQueryTest extends CommonsGwtTestCase {

  protected HTML html;

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    html = new HTML("<b>Hello</b> world. <span id='span1'>This <span id='span2'>is a <i>test</i></span> case</span>!");
    RootPanel.get().add(html);
  }

  public void testWalkNodeTree() throws Exception {
    // 1) test a full traversal and compare its output to the Stream version of the same method
    {
      RecordingNodeVisitor visitor = new RecordingNodeVisitor("Full traversal NodeVisitor");
      // finally verify the actual output
      walkWithVisitor(visitor, new String[]{
          "0: <DIV>",
          "  1: <B>",
          "    2: <#text:'Hello'>",
          "  1: <#text:' world. '>",
          "  1: <SPAN id=span1>",
          "    2: <#text:'This '>",
          "    2: <SPAN id=span2>",
          "      3: <#text:'is a '>",
          "      3: <I>",
          "        4: <#text:'test'>",
          "    2: <#text:' case'>",
          "  1: <#text:'!'>"
      });
      log(visitor.name + ".nodes: \n" + visitor.nodes.stream().map(DomQueryTest::nodeToString).collect(Collectors.toList()));
      log(visitor.name + ".nodesByDepth: \n" + visitor.nodesByDepth.entries().stream()
          .map(entry -> StringUtils.parenthesize(nodeToString(entry.getValue(), entry.getKey())))
          .collect(Collectors.joining(", ")));
      // make sure the stream version of this method returns the same nodes in the same sequence
      List<Node> streamNodes = walkNodeTree(html.getElement()).collect(Collectors.toList());
      log("Stream: " + streamNodes.stream()
              .map(DomQueryTest::nodeToString)
              .collect(Collectors.joining(", ")));
      assertEquals(visitor.nodes, streamNodes);
    }

    // 2) test the various early breakout conditions during the traversal
    Element span2 = Document.get().getElementById("span2");

    // terminate the traversal immediately after reaching span2:
    walkWithVisitor(new RecordingNodeVisitor("Early-terminating NodeVisitor") {
      @Nonnull
      @Override
      public int visit(Node node, int depth) {
        super.visit(node, depth);
        // break out of visit when reached the <#text:"is a "> node
        if (node.equals(span2)) {
          log("Terminating traversal after " + nodeToString(node) + " at depth " + depth);
          return TERMINATE;
        }
        return CONTINUE;
      }
    }, new String[]{
        "0: <DIV>",
        "  1: <B>",
        "    2: <#text:'Hello'>",
        "  1: <#text:' world. '>",
        "  1: <SPAN id=span1>",
        "    2: <#text:'This '>",
        "    2: <SPAN id=span2>",
    });

    // skip the subtree of span2 but still visit its siblings:
    walkWithVisitor(new RecordingNodeVisitor("Subtree-skipping NodeVisitor") {
      @Override
      public int visit(Node node, int depth) {
        super.visit(node, depth);
        // break out of visit when reached the <#text:"is a "> node
        if (node.equals(span2)) {
          log("Skipping subtree after " + nodeToString(node) + " at depth " + depth);
          return SKIP_SUBTREE;
        }
        return CONTINUE;
      }
    }, new String[]{
        "0: <DIV>",
        "  1: <B>",
        "    2: <#text:'Hello'>",
        "  1: <#text:' world. '>",
        "  1: <SPAN id=span1>",
        "    2: <#text:'This '>",
        "    2: <SPAN id=span2>",
        "    2: <#text:' case'>",
        "  1: <#text:'!'>"
    });

    // skip both subtree and siblings of span2
    walkWithVisitor(new RecordingNodeVisitor("Subtree and sibling-skipping NodeVisitor") {
      @Override
      public int visit(Node node, int depth) {
        super.visit(node, depth);
        if (node.equals(span2)) {
          log("Skipping subtree and siblings after " + nodeToString(node) + " at depth " + depth);
          return SKIP_SUBTREE_AND_SIBLINGS;
        }
        return CONTINUE;
      }
    }, new String[]{
        "0: <DIV>",
        "  1: <B>",
        "    2: <#text:'Hello'>",
        "  1: <#text:' world. '>",
        "  1: <SPAN id=span1>",
        "    2: <#text:'This '>",
        "    2: <SPAN id=span2>",
        "  1: <#text:'!'>"
    });

    // skip only siblings of span2, but still visit its subtree
    walkWithVisitor(new RecordingNodeVisitor("Sibling-skipping NodeVisitor") {
      @Override
      public int visit(Node node, int depth) {
        super.visit(node, depth);
        if (node.equals(span2)) {
          log("Skipping siblings after " + nodeToString(node) + " at depth " + depth);
          return SKIP_SIBLINGS;
        }
        return CONTINUE;
      }
    }, new String[]{
        "0: <DIV>",
        "  1: <B>",
        "    2: <#text:'Hello'>",
        "  1: <#text:' world. '>",
        "  1: <SPAN id=span1>",
        "    2: <#text:'This '>",
        "    2: <SPAN id=span2>",
        "      3: <#text:'is a '>",
        "      3: <I>",
        "        4: <#text:'test'>",
        "  1: <#text:'!'>"
    });
  }

  public void testNodeTreeIterator() throws Exception {
    NodeTreeIterator it = new NodeTreeIterator(html.getElement());
    ArrayList<Node> nodes = CollectionUtils.asList(it);
    assertEquals("<DIV>, <B>, <#text:'Hello'>, <#text:' world. '>, <SPAN id=span1>, <#text:'This '>, <SPAN id=span2>, <#text:'is a '>, <I>, <#text:'test'>, <#text:' case'>, <#text:'!'>",
        nodes.stream().map(DomQueryTest::nodeToString).collect(Collectors.joining(", ")));
  }

  private RecordingNodeVisitor walkWithVisitor(RecordingNodeVisitor visitor, String[] expectedOutput) {
    walkNodeTree(html.getElement(), visitor);
    String visitorOutput = visitor.out.toString();
    log(visitor.name + " output: \n" + visitorOutput);
    assertEquals(StringUtils.join("\n", expectedOutput), visitorOutput);
    return visitor;
  }

  private class RecordingNodeVisitor implements NodeVisitor {
    private final String name;
    private final StringBuilder out = new StringBuilder();
    private final ArrayList<Node> nodes = new ArrayList<>();
    private final ListMultimap<Integer, Node> nodesByDepth = MultimapBuilder.linkedHashKeys().arrayListValues().build();

    private RecordingNodeVisitor(String name) {
      this.name = name;
    }

    @Override
    public int visit(Node node, int depth) {
      nodes.add(node);
      nodesByDepth.put(depth, node);
      String msg = nodeToString(node, depth);
      if (out.length() > 0)
        out.append('\n');
      out.append(StringUtils.indent(depth*2, msg));
      return CONTINUE;
    }
  }

  private static String nodeToString(Node node) {
    StringBuilder out = new StringBuilder().append(node.getNodeName());
    String value = node.getNodeValue();
    if (value != null)
      out.append(":'").append(value).append("'");
    else if (node.getNodeType() == Node.ELEMENT_NODE) {
      String id = node.<Element>cast().getId();
      if (StringUtils.notEmpty(id)) {
        out.append(" id=").append(id);
      }
    }
    return StringUtils.bracket(out.toString(), '<');
  }

  private static String nodeToString(Node node, int depth) {
    return depth + ": " + nodeToString(node);
  }

}