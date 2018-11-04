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

package solutions.trsoftware.tools.gwt.artifacts;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.soyc.CompilerMetricsXmlFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import solutions.trsoftware.commons.server.io.file.FileUtils;
import solutions.trsoftware.commons.server.util.xml.dom.NodeListAdapter;
import solutions.trsoftware.commons.server.util.xml.dom.XmlDomUtils;
import solutions.trsoftware.commons.shared.util.collections.DefaultArrayListMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @see CompilerMetricsXmlFormatter#writeCompilationMetricsAsXml
 *
 * @author Alex
 * @since 3/9/2018
 */
public class GwtSoycReports {

  public static final String COMPILER_METRICS_FILES_GLOB = "**/soycReport/compilerMetrics.xml";

  private Path extrasDir;
  private Map<String, Path> compilerMetricsFilesByModuleName = new LinkedHashMap<>();
  private DefaultArrayListMap<String, CompilationUnitMetrics> compilerMetricsByModuleName = new DefaultArrayListMap<>();


  /**
   *
   * @param extrasDir
   * @throws IllegalArgumentException if the given directory is not found
   * @throws IllegalStateException if the SOYC reports are missing from this directory, or any error occurs
   * while parsing the SOYC reports
   * @throws IOException
   */
  public GwtSoycReports(Path extrasDir) throws IOException {
    this.extrasDir = extrasDir;
    FileUtils.assertIsDirectory(extrasDir);
    PathMatcher compilerMetricsXmlFileMatcher = extrasDir.getFileSystem().getPathMatcher("glob:" + COMPILER_METRICS_FILES_GLOB);
    List<Path> compilerMetricsXmlFiles;
    try (Stream<Path> stream = Files.walk(extrasDir)) {
      compilerMetricsXmlFiles = stream.filter(compilerMetricsXmlFileMatcher::matches).collect(Collectors.toList());
    }
    if (compilerMetricsXmlFiles.isEmpty())
      throw new IllegalStateException(String.format("No compiler metrics found (%s / %s)", extrasDir, COMPILER_METRICS_FILES_GLOB));
    System.out.println(compilerMetricsXmlFiles);
    // the parent directory of /soycReport/compilerMetrics.xml path should have the same name as the module
    for (Path path : compilerMetricsXmlFiles) {
      String moduleName = path.getParent().getParent().getFileName().toString();
      assert !compilerMetricsFilesByModuleName.containsKey(moduleName);
      compilerMetricsFilesByModuleName.put(moduleName, path);
    }
//    System.out.println(compilerMetricsFilesByModuleName);
    try {
      parseCompilerMetrics();
    }
    catch (ParserConfigurationException | SAXException e) {
      throw new IllegalStateException(e);
    }
  }

  private void parseCompilerMetrics() throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = factory.newDocumentBuilder();
    for (Map.Entry<String, Path> entry : compilerMetricsFilesByModuleName.entrySet()) {
      String moduleName = entry.getKey();
      Path compilerMetricsFile = entry.getValue();
      try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(compilerMetricsFile))) {
        Document xml = documentBuilder.parse(inputStream);
        Element compilationsElt = XmlDomUtils.getSingletonElement(xml.getElementsByTagName("compilations"));
        for (Element compilationElt : new NodeListAdapter<Element>(compilationsElt.getElementsByTagName("compilation"))) {
          compilerMetricsByModuleName.get(moduleName).add(new CompilationUnitMetrics(compilationElt));
        }
      }
    }
  }

  public Map<String, List<CompilationUnitMetrics>> getCompilerMetricsByModuleName() {
    return compilerMetricsByModuleName;
  }

  /**
   * Encapsulates compiler metrics for a permutation.
   */
  public static class CompilationUnitMetrics {
    /**
     * Identifies the permutation
     */
    private int id;
    /**
     * Time elapsed (in millis) for compiling this permutation
     */
    private int compilationTime;
    /**
     * All of the binding properties used for the compilation
     */
    private Multimap<String, String> bindingProperties = LinkedHashMultimap.create();
    /**
     * Total size (in bytes) of the generated javascript, including all the fragments.
     */
    private int jsSize;


    CompilationUnitMetrics(Element elt) {
      id = Integer.parseInt(elt.getAttribute("id"));
      compilationTime = Integer.parseInt(elt.getAttribute("elapsed"));
      Element javascript = XmlDomUtils.getSingletonElement(elt.getElementsByTagName("javascript"));
      jsSize = Integer.parseInt(javascript.getAttribute("size"));
      String description = elt.getAttribute("description");
      String[] propNameValuePairs = description.split("[,;]");
      for (String nvPairStr : propNameValuePairs) {
        String[] nvPair = nvPairStr.split("=");
        assert nvPair.length == 2;
        bindingProperties.put(nvPair[0], nvPair[1]);
      }
    }

    public int getId() {
      return id;
    }

    public int getCompilationTime() {
      return compilationTime;
    }

    public Multimap<String, String> getBindingProperties() {
      return bindingProperties;
    }

    public int getJsSize() {
      return jsSize;
    }
  }

}
