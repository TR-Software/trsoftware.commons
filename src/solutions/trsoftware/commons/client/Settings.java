package solutions.trsoftware.commons.client;

/**
 * Singleton defining the settings to be used by the various components of the TR Commons library.
 *
 * @author Alex, 10/25/2017
 */
public class Settings {

  // TODO: cont here:
  private static Settings instance;

  public static Settings get() {
    if (instance == null)
      instance = new Settings();
    return instance;
  }

  private String appName;
  private String supportEmail;
  private int stackTraceSizeLimit = Integer.MAX_VALUE;
  private String stackTraceDeobfuscatorServletUrl;

  private Settings() {
  }

  public String getAppName() {
    return appName;
  }
  
  public Settings setAppName(String appName) {
    this.appName = appName;
    return this;
  }

  public String getSupportEmail() {
    return supportEmail;
  }

  public Settings setSupportEmail(String supportEmail) {
    this.supportEmail = supportEmail;
    return this;
  }

  public int getStackTraceSizeLimit() {
    return stackTraceSizeLimit;
  }

  public Settings setStackTraceSizeLimit(int stackTraceSizeLimit) {
    this.stackTraceSizeLimit = stackTraceSizeLimit;
    return this;
  }

  public String getStackTraceDeobfuscatorServletUrl() {
    return stackTraceDeobfuscatorServletUrl;
  }

  public Settings setStackTraceDeobfuscatorServletUrl(String stackTraceDeobfuscatorServletUrl) {
    this.stackTraceDeobfuscatorServletUrl = stackTraceDeobfuscatorServletUrl;
    return this;
  }
}
