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

package solutions.trsoftware.commons.server.testutil.htmlunit;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import solutions.trsoftware.commons.server.io.StringInputStream;
import solutions.trsoftware.commons.server.io.file.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Saves the pages loaded by a {@link WebClient} to a directory on the file system.
 *
 * @author Alex
 * @since 5/9/2018
 */
public class RecordingWebWindowListener extends CurrentWebWindowContentChangeListener {

  private Path outputDir;
  private AtomicInteger nextId = new AtomicInteger(1);
  private ArrayList<SavedHtmlPage> savedHtmlPages = new ArrayList<>();

  /**
   * Will write the loaded pages to a new directory in the default temporary-file directory ({@value FileUtils#TEMP_DIR_PROP})
   * @param webClient the web client whose pages are to be written to disk
   * @throws IOException if the output directory could not be created
   */
  public RecordingWebWindowListener(WebClient webClient) throws IOException {
    this(webClient, Files.createTempDirectory(RecordingWebWindowListener.class.getSimpleName()));
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

  private void savePage(HtmlPage htmlPage) throws IOException {
    /*
      TODO: can try using HtmlPage#save(File), which supposedly also saves the images from the page
        see https://stackoverflow.com/q/2738464/
     */
    WebResponse webResponse = htmlPage.getWebResponse();
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
    savedHtmlPages.add(new SavedHtmlPage(webResponse, outFile));
  }

  /**
   * @return a history of the pages that were written to disk by this listener, which can be used to open the saved files
   * in a browser.
   *
   * @see SavedHtmlPage#openSavedFile()
   */
  public ArrayList<SavedHtmlPage> getSavedHtmlPages() {
    return savedHtmlPages;
  }

  /**
   * Record representing a file that was created by the {@link #savePage(HtmlPage)} method.
   *
   * @see #getSavedHtmlPages()
   */
  public static class SavedHtmlPage {
    /** Location of the saved file */
    private final Path outputFile;
    /** URL of the request that loaded the page */
    private final URL requestURL;
    /** Time when the page was saved (epoch millis) */
    private final long timestamp;

    public SavedHtmlPage(WebResponse webResponse, Path outputFile) {
      this.outputFile = outputFile;
      requestURL = webResponse.getWebRequest().getUrl();
      timestamp = System.currentTimeMillis();
    }

    public Path getOutputFile() {
      return outputFile;
    }

    public URL getRequestURL() {
      return requestURL;
    }

    public long getTimestamp() {
      return timestamp;
    }

    /**
     * Launches the associated application to open the saved file.
     * <p>
     * <i>Note:</i> if ".html" files are associated with a web browser, the outcome of this method is likely the same
     * as {@link #browseSavedFile()}
     *
     * @see #browseSavedFile()
     * @see Desktop#open(File)
     */
    public void openSavedFile() throws IOException {
      Desktop.getDesktop().open(outputFile.toFile());
    }

    /**
     * Launches the default web browser to open the saved file.
     * <p>
     * <i>Note:</i> if ".html" files are associated with a web browser, the outcome of this method is likely the same
     * as {@link #openSavedFile()}
     *
     * @see #openSavedFile()
     * @see Desktop#browse(URI)
     */
    public void browseSavedFile() throws IOException {
      Desktop.getDesktop().browse(outputFile.toUri());
    }
  }

}
