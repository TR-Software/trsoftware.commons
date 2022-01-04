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
package solutions.trsoftware.gwt.junit;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.gargoylesoftware.htmlunit.util.WebClientUtils;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.shell.HostedModePluginObject;
import com.google.gwt.junit.DoNotRunWith;
import com.google.gwt.junit.JUnitShell;
import com.google.gwt.junit.Platform;
import com.google.gwt.junit.RunStyle;
import com.google.gwt.thirdparty.guava.common.collect.ImmutableSet;
import com.google.gwt.thirdparty.guava.common.collect.Maps;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Custom version of {@link com.google.gwt.junit.RunStyleHtmlUnit} that allows using <b>HtmlUnit 2.55</b>
 * instead of the default version that's bundled with GWT (currently HtmlUnit 2.18).
 * <p></p>
 * <h3>Usage:</h3>
 * After making sure that the HtmlUnit 2.55 jars are on the classpath (and overriding the GWT jars), replace the
 * <pre>{@code -runStyle HtmlUnit:<BROWSER_LIST>}</pre> argument in {@code gwt.args} with:
 * <pre>{@code -runStyle solutions.trsoftware.gwt.junit.RunStyleHtmlUnit255:<BROWSER_LIST>}</pre>
 * <em>For example:</em>
 * <pre>{@code -runStyle solutions.trsoftware.gwt.junit.RunStyleHtmlUnit255:Chrome,FF,IE}</pre>
 *
 * <hr>
 * <p>
 * <strong>NOTE:</strong> <em>This implementation hasn't been properly tested and is not guaranteed to work.</em>
 * Therefore, we recommend sticking with the default version and running the tests that fail under HtmlUnit
 * with <nobr>"{@code -runStyle Manual}"</nobr>. Such tests can also be annotated with
 * {@link DoNotRunWith @DoNotRunWith(Platform.HtmlUnitUnknown)} to be excluded from a suite that runs under HtmlUnit.
 *
 * @see <a href="https://github.com/gwtproject/gwt/blob/2.8.2/dev/build.xml"><code>gwt/dev/build.xml</code></a>
 *   (to check which version of HtmlUnit is bundled with your GWT release)
 * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideTestingHtmlUnit.html">Running tests with HtmlUnit</a>
 * @see <a href="http://www.gwtproject.org/doc/latest/DevGuideTesting.html#Manual_Mode">Running tests in Manual mode</a>
 *
 */
public class RunStyleHtmlUnit255 extends RunStyle {

  /**
   * Runs HTMLUnit in a separate thread.
   */
  protected static class HtmlUnitThread extends Thread implements AlertHandler,
      IncorrectnessListener, OnbeforeunloadHandler {

    private final BrowserVersion browser;
    private final boolean developmentMode;
    private final TreeLogger treeLogger;
    private final String url;
    private Object waitForUnload = new Object();

    public HtmlUnitThread(BrowserVersion browser, String url,
        TreeLogger treeLogger, boolean developmentMode) {
      this.browser = browser;
      this.url = url;
      this.treeLogger = treeLogger;
      this.setName("htmlUnit client thread");
      this.developmentMode = developmentMode;
    }

    public void handleAlert(Page page, String message) {
      treeLogger.log(TreeLogger.ERROR, "Alert: " + message);
    }

    public boolean handleEvent(Page page, String returnValue) {
      synchronized (waitForUnload) {
        waitForUnload.notifyAll();
      }
      return true;
    }

    public void notify(String message, Object origin) {
      if ("Obsolete content type encountered: 'text/javascript'.".equals(message) ||
          "Obsolete content type encountered: 'application/x-javascript'.".equals(message)) {
        // silently eat warning about text/javascript MIME type and application/x-javascript
        return;
      }
      treeLogger.log(TreeLogger.WARN, message);
    }

    @Override
    public void run() {
      WebClient webClient = new WebClient(browser);
      webClient.setAlertHandler(this);
      webClient.setIncorrectnessListener(this);
      webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
      // To receive exceptions from js side in the development mode, we need set this to 'true'.
      // However, as htmlunit dies after throwing the exception, we still want it to be 'false'
      // for web mode.
      webClient.getOptions().setThrowExceptionOnScriptError(developmentMode);
      webClient.setOnbeforeunloadHandler(this);
      webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {
        @Override
        public void loadScriptError(HtmlPage page, URL scriptUrl, Exception exception) {
            treeLogger.log(TreeLogger.ERROR,
              "Load Script Error: " + exception, exception);
        }

        @Override
        public void malformedScriptURL(HtmlPage page, String url, MalformedURLException malformedURLException) {
          treeLogger.log(TreeLogger.ERROR,
              "Malformed Script URL: " + malformedURLException.getLocalizedMessage());
        }

        @Override
        public void scriptException(HtmlPage page, ScriptException scriptException) {
          treeLogger.log(TreeLogger.DEBUG,
              "Script Exception: " + scriptException.getLocalizedMessage() +
               ", line " + scriptException.getFailingLine());
        }

        @Override
        public void timeoutError(HtmlPage page, long allowedTime, long executionTime) {
          treeLogger.log(TreeLogger.ERROR,
              "Script Timeout Error " + executionTime + " > " + allowedTime);
        }

        @Override
        public void warn(String message, String sourceName, int line, String lineSource, int lineOffset) {
          // message format copied from com.gargoylesoftware.htmlunit.javascript.DefaultJavaScriptErrorListener.warn
          StringBuilder msg = new StringBuilder()
              .append("warning: message=[").append(message)
              .append("] sourceName=[").append(sourceName)
              .append("] line=[").append(line)
              .append("] lineSource=[").append(lineSource)
              .append("] lineOffset=[").append(lineOffset)
              .append(']');
          treeLogger.log(TreeLogger.DEBUG, msg.toString());
        }
      });
      setupWebClient(webClient);
      try {
        Page page = webClient.getPage(url);
        webClient.waitForBackgroundJavaScriptStartingBefore(2000);
        if (treeLogger.isLoggable(TreeLogger.SPAM)) {
          treeLogger.log(TreeLogger.SPAM, "getPage returned "
              + ((HtmlPage) page).asXml());
        }
        // TODO(amitmanjhi): call webClient.closeAllWindows()
      } catch (FailingHttpStatusCodeException e) {
        treeLogger.log(TreeLogger.ERROR, "HTTP request failed", e);
      } catch (MalformedURLException e) {
        treeLogger.log(TreeLogger.ERROR, "Bad URL", e);
      } catch (IOException e) {
        treeLogger.log(TreeLogger.ERROR, "I/O error on HTTP request", e);
      }
    }

    protected void setupWebClient(WebClient webClient) {
      if (developmentMode) {
        JavaScriptEngine hostedEngine = new HostedJavaScriptEngine(webClient,
            treeLogger);
        webClient.setJavaScriptEngine(hostedEngine);
      } else {
        JavaScriptEngine webEngine = new WebJavaScriptEngine(webClient);
        webClient.setJavaScriptEngine(webEngine);
      }
      if (System.getProperty("gwt.htmlunit.debug") != null) {
        WebClientUtils.attachVisualDebugger(webClient);
      }
    }
  }

  /**
   * JavaScriptEngine subclass that provides a hook of initializing the
   * __gwt_HostedModePlugin property on any new window, so it acts just like
   * Firefox with the XPCOM plugin installed.
   */
  private static class HostedJavaScriptEngine extends JavaScriptEngine {

    private static final long serialVersionUID = 3594816610842448691L;
    private final TreeLogger logger;

    public HostedJavaScriptEngine(WebClient webClient, TreeLogger logger) {
      super(webClient);
      this.logger = logger;
    }

    @Override
    public void initialize(WebWindow webWindow, Page page) {
      // Hook in the hosted-mode plugin after initializing the JS engine.
      super.initialize(webWindow, page);
      Window window = (Window) webWindow.getScriptableObject();
      window.defineProperty("__gwt_HostedModePlugin",
          new HostedModePluginObject(this, logger), ScriptableObject.READONLY);
    }
  }

  /**
   * NOTE(alex): this subclass of {@link JavaScriptEngine} is no longer necessary, but we're keeping the empty
   * stub just to facilitate comparing diffs with the GWT 2.8.2 version of {@link com.google.gwt.junit.RunStyleHtmlUnit},
   * which used this subclass to as a monkey patch for <a href="https://sourceforge.net/p/htmlunit/bugs/1924/">HtmlUnit bug #1924</a>.
   */
  private static class WebJavaScriptEngine extends JavaScriptEngine {

    public WebJavaScriptEngine(WebClient webClient) {
      super(webClient);
    }

  }

  private static final Map<String, BrowserVersion> BROWSER_MAP = Maps.newHashMap();
  private static final Map<BrowserVersion, String> USER_AGENT_MAP  = Maps.newHashMap();

  static {
    // “Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36 Edge/12.0″
    addBrowser(BrowserVersion.EDGE, "safari");
    addBrowser(BrowserVersion.FIREFOX, "gecko1_8");
    addBrowser(BrowserVersion.FIREFOX_78, "gecko1_8");
    addBrowser(BrowserVersion.CHROME, "safari");
    addBrowser(BrowserVersion.INTERNET_EXPLORER, "gecko1_8");  // IE11
    /*
      TODO: IE8 doesn't seem to be supported in HtmlUnit 2.55, but maybe we can rig it the same way as it was rigged in 2.19
      ("It exists as Internet Explorer 11 has Enterprise Mode, which behaves as Internet Explorer 8")

      addBrowser(BrowserVersion.INTERNET_EXPLORER_8, "ie8");
    */
  }

  private static void addBrowser(BrowserVersion browser, String userAgent) {
    BROWSER_MAP.put(browser.getNickname(), browser);
    USER_AGENT_MAP.put(browser, userAgent);
  }

  /*
   * as long as this number is greater than 1, GWTTestCaseTest::testRetry will
   * pass
   */
  private static final int DEFAULT_TRIES = 1;

  private static final Set<Platform> PLATFORMS = ImmutableSet.of(Platform.HtmlUnitBug,
      Platform.HtmlUnitLayout, Platform.HtmlUnitUnknown);

  private Set<BrowserVersion> browsers = new HashSet<BrowserVersion>();
  private boolean developmentMode;
  private final List<Thread> threads = new ArrayList<Thread>();

  /**
   * Create a RunStyle instance with the passed-in browser targets.
   */
  public RunStyleHtmlUnit255(JUnitShell shell) {
    super(shell);
  }

  @Override
  public Set<Platform> getPlatforms() {
    return PLATFORMS;
  }

  @Override
  public int initialize(String args) {
    if (args == null || args.length() == 0) {
      // If no browsers specified, default to Firefox 38.
      args = BrowserVersion.BEST_SUPPORTED.getNickname();
    }
    Set<BrowserVersion> browserSet = new HashSet<BrowserVersion>();
    Set<String> userAgentSet = new HashSet<String>();
    for (String browserName : args.split(",")) {
      BrowserVersion browser = BROWSER_MAP.get(browserName);
      if (browser == null) {
        getLogger().log(
            TreeLogger.ERROR,
            "RunStyleHtmlUnit255: Unknown browser " + "name " + browserName
                + ", expected browser name: one of " + BROWSER_MAP.keySet());
        return -1;
      }
      browserSet.add(browser);
      userAgentSet.add(USER_AGENT_MAP.get(browser));
    }
    browsers = Collections.unmodifiableSet(browserSet);
    setUserAgents(Collections.unmodifiableSet(userAgentSet));
    setTries(DEFAULT_TRIES); // set to the default value for this RunStyle
    return browsers.size();
  }

  @Override
  public void launchModule(String moduleName) {
    for (BrowserVersion browser : browsers) {
      String url = shell.getModuleUrl(moduleName);
      HtmlUnitThread hut = createHtmlUnitThread(browser, url);
      TreeLogger logger = shell.getTopLogger();
      if (logger.isLoggable(TreeLogger.INFO)) {
        logger.log(TreeLogger.INFO,
            "Starting " + url + " on browser " + browser.getNickname());
      }
      /*
       * TODO (amitmanjhi): Is it worth pausing here and waiting for the main
       * test thread to get to an "okay" state.
       */
      hut.start();
      threads.add(hut);
    }
  }

  public int numBrowsers() {
    return browsers.size();
  }

  @Override
  public boolean setupMode(TreeLogger logger, boolean developmentMode) {
    this.developmentMode = developmentMode;
    return true;
  }

  protected HtmlUnitThread createHtmlUnitThread(BrowserVersion browser,
      String url) {
    return new HtmlUnitThread(browser, url, shell.getTopLogger().branch(
        TreeLogger.SPAM, "logging for HtmlUnit thread"), developmentMode);
  }
}
