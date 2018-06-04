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

package solutions.trsoftware.commons.client;

import com.google.gwt.user.client.rpc.StatusCodeException;
import solutions.trsoftware.commons.client.useragent.UserAgent;
import solutions.trsoftware.commons.shared.util.StringUtils;

/**
 * Singleton that can be used to generate certain common UI messages for the webapp.
 *
 * TODO: add support for i18n (see http://www.gwtproject.org/doc/latest/DevGuideI18n.html)
 *
 * @author Alex
 */
public class Messages {

  private static final String CUSTOMER_SUPPORT = "customer support";

  private static Messages instance;

  public static Messages get() {
    if (instance == null)
      instance = new Messages();
    return instance;
  }

  /**
   * Technically, having this method violates the "singleton" contract of this class, but we provide it just in case
   * the application wishes to extend this class.
   *
   * @param impl an instance of the overriding class.
   */
  public static void setInstance(Messages impl) {
    if (Messages.class.equals(impl.getClass()))
      throw new IllegalArgumentException("Messages is a singleton, therefore setInstance should only be used to provide a different impl.");
    Messages.instance = impl;
  }

  private Messages() {
  }

  public static String exceptionTypeAndMessageToString(Throwable ex) {
    return StringUtils.template("$1 ($2)", ex.getMessage(), exceptionTypeToString(ex));
  }

  /** Convenience method for getting the name of the class of the given Exception */
  public static String exceptionTypeToString(Throwable ex) {
    String type = "Unknown";
    // these null checks are probably not necessary, but just in case...
    if (ex.getClass() != null) {
      String className = ex.getClass().getName();
      if (StringUtils.notBlank(className))
        type = className;
    }
    return type;
  }

  public String requestFailed() {
    return "There was an error processing your request.  Please try again later.\n\n" + notifySupportIfProblemPersists();
  }

  /**
   * @return the value defined by {@link Settings#getSupportEmail()}
   */
  public String getSupportEmail() {
    return Settings.get().getSupportEmail();
  }

  /**
   * @return the value defined by {@link Settings#getAppName()}, or {@code "this application"} if it's {@code null}.
   */
  public String getAppName() {
    String appName = Settings.get().getAppName();
    if (appName == null)
      appName = "this application";
    return appName;
  }

  public String notifySupportMessage() {
    String supportEmail = getSupportEmail();
    if (supportEmail == null)
      return "please notify " + CUSTOMER_SUPPORT;
    return "please notify " + supportEmail;
  }

  public String contactSupportMessage() {
    String supportEmail = getSupportEmail();
    if (supportEmail == null)
      return "please contact " + CUSTOMER_SUPPORT;
    return "please contact " + supportEmail;
  }

  public String contactSupportForAssistanceMessage() {
    return contactSupportMessage() +  " for assistance.";
  }

  public String notifySupportIfProblemPersists() {
    return "If the problem continues, " + notifySupportMessage();
  }

  public String reloadAppMessage() {
    return "Sorry, this webpage needs to be " + UserAgent.getInstance().getReloadPageVerb() + "ed because a newer version of this app has been released.\n\n"
        + "(If you are still getting this message after that, please wait a few minutes and try to " + UserAgent.getInstance().getReloadPageVerb() + " again. " + notifySupportIfProblemPersists() + ")";
  }

  /** Formats an error message to be displayed to the user in an alert box */
  public String formatErrorMessage(String message, Throwable ex) {
    return formatErrorMessageHelper(message, "", StringUtils.capitalize(contactSupportForAssistanceMessage()), ex);
  }
  
  /** Formats an error message to be displayed to the user in an alert box */
  public String formatErrorMessageAndAskForRetry(String message, Throwable ex) {
    return formatErrorMessageHelper(message, "Please try again later, and if this happens again, \n", contactSupportForAssistanceMessage(), ex);
  }

  /** Formats an error message to be displayed to the user in an alert box */
  private static String formatErrorMessageHelper(String message, String message2, String supportMsg, Throwable ex) {
    String exMsg = ex.getMessage();
    if (ex instanceof StatusCodeException) {
      // this type of exception from the RPC system doesn't have a message, just a status code
      // that indicates the failure of an RPC call from an undeclared exception
      exMsg = "Status code " + ((StatusCodeException)ex).getStatusCode();
    }
    return message
        + "\n\nError message: " + StringUtils.abbreviate(exMsg, 100)  // some exceptions contain the HTML of the full page, so we must abbreviate in cases like that
        + "\n\nError type: " + exceptionTypeToString(ex)
        + "\n\n" + message2 + supportMsg;
  }

}
