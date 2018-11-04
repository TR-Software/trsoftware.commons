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

package solutions.trsoftware.commons.server.testutil.htmlunit;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import solutions.trsoftware.commons.server.io.StringInputStream;
import solutions.trsoftware.commons.server.io.file.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static solutions.trsoftware.commons.server.io.file.FileUtils.deleteOnExit;

/**
 * Saves the pages loaded by a {@link WebClient} to a directory on the file system.
 *
 * @author Alex
 * @since 5/9/2018
 */
public class RecordingWebWindowListener extends CurrentWebWindowContentChangeListener {

  private Path outputDir;
  private AtomicInteger nextId = new AtomicInteger(1);


  /**
   * Will write the loaded pages to a new directory in the default temporary-file directory ({@value FileUtils#TEMP_DIR_PROP})
   * @param webClient the web client whose pages are to be written to disk
   * @throws IOException if the output directory could not be created
   */
  public RecordingWebWindowListener(WebClient webClient) throws IOException {
    this(webClient, deleteOnExit(Files.createTempDirectory(RecordingWebWindowListener.class.getSimpleName())));
  }

  /**
   * @param webClient the web client whose pages are to be written to disk
   * @param outputDir directory where to save the pages (will be created if doesn't exist)
   * @throws IOException if the directory had to be created, but was unable to do so
   * @throws NotDirectoryException if the given path already exists, but is not a directory
   */
  public RecordingWebWindowListener(WebClient webClient, Path outputDir) throws IOException {
    super(webClient);
    this.outputDir = outputDir;
    FileUtils.maybeCreateDirectory(outputDir);
  }

  @Override
  protected void onCurrentWindowContentChanged(WebWindowEvent event) {
    Page newPage = event.getNewPage();
    if (newPage.isHtmlPage()) {
      try {
        savePage((HtmlPage)newPage);
      }
      catch (Throwable ex) {
        // suppress all exceptions
        ex.printStackTrace();
      }
    }
  }

  private void savePage(HtmlPage newPage) throws IOException {
    WebResponse webResponse = newPage.getWebResponse();
    String contentCharset = webResponse.getContentCharset();
    String content = webResponse.getContentAsString();
    // add a comment containing the request info at the top of the output file
    WebRequest request = webResponse.getWebRequest();
    String comment = String.format("<!--%n%s%n-->%n", request);
    String output = comment + content;
    String outFilename = String.format("page_%03d.html", nextId.getAndIncrement());
    Path outFile = outputDir.resolve(outFilename);
    Files.copy(new StringInputStream(output, contentCharset), outFile);
    System.out.printf("%s saved %s response page (for %s) to:%n >> %s%n",
        getClass().getSimpleName(),
        webClient.getClass().getSimpleName(),
        request,
        outFile
    );
  }

}
