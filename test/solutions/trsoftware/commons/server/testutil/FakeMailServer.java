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

package solutions.trsoftware.commons.server.testutil;

import junit.framework.AssertionFailedError;
import org.simplejavamail.converter.internal.mimemessage.MimeMessageParser;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;
import solutions.trsoftware.commons.server.util.Duration;
import solutions.trsoftware.commons.server.util.ThreadUtils;
import solutions.trsoftware.commons.shared.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates a {@link Wiser} instance (SMTP server for testing).
 * Can be used in a <i>try-with-resources</i> block.
 *
 * @author Alex
 * @see <a href="https://github.com/voodoodyne/subethasmtp">SubEtha SMTP project on GitHub</a>
 */
public class FakeMailServer extends Assert implements AutoCloseable {
  public static final int DEFAULT_PORT = 25252;

  private Wiser wiser;

  /**
   * The number of emails received by the server after the last time assertReceivedCount
   * was called.
   */
  private int lastCount = 0;

  public FakeMailServer(int port) {
    String startMsg = String.format("Starting %s on port %d", Wiser.class.getName(), port);
    System.out.println(startMsg);
    Duration startupDuration = new Duration(startMsg);
    wiser = new Wiser(port);
    wiser.start();
    System.out.println(startupDuration);
  }

  /**
   * Asserts that the SMTP server stub received the given number of emails since it was last checked,
   * waiting up to the given number of milliseconds for this to happen.
   *
   * @param expectedNewCount the number of new email messages expected
   * @param timeoutMs will wait this number of millis for the assertion to be satisfied before throwing an
   *   {@link AssertionFailedError}
   * @return the {@code expectedNewCount} most recent email messages
   */
  public List<MimeMessageParser> assertNewMessageCount(int expectedNewCount, int timeoutMs) throws Exception {
    if (expectedNewCount < 1)
      throw new IllegalArgumentException("Use assertNoEmailReceived instead");
    // ensure that the new messages have been received, waiting up to timeout millis
    if (!ThreadUtils.waitFor(() -> getReceivedEmailCount() >= lastCount + expectedNewCount, timeoutMs, 100)) {
      fail(String.format("Timed out while waiting for email to be received.  (expected new count=%d, actual new count=%d, total count=%d)", expectedNewCount, getReceivedEmailCount() - lastCount, getReceivedEmailCount()));
    }
    int count = getReceivedEmailCount();
    assertEquals(expectedNewCount, count - lastCount);
    // return only the new messages
    List<WiserMessage> newMessages = wiser.getMessages().subList(lastCount, count);
    List<MimeMessageParser> parsedMessages = new ArrayList<>();
    for (WiserMessage wiserMessage : newMessages) {
      parsedMessages.add(new MimeMessageParser(wiserMessage.getMimeMessage()).parse());
    }
    lastCount = count;
    return parsedMessages;
  }

  /**
   * @return the total number of messages received by this SMTP server since it was started
   */
  public int getReceivedEmailCount() {
    return wiser.getMessages().size();
  }

  public void stop() {
    wiser.stop();
  }

  public boolean isRunning() {
    return wiser.getServer().isRunning();
  }

  @Override
  public void close() {
    wiser.stop();
  }

  /**
   * Runs a new instance of {@link FakeMailServer} on port {@value DEFAULT_PORT} (a different port can be specified with
   * a command line arg) and keeps printing out all the received email to the console, until the process is killed.
   */
  public static void main(String[] args) throws Exception {
    int port;
    if (args.length == 1) {
      port = Integer.parseInt(args[0]);
      System.out.printf("Using port %d (specified via command-line arg)%n", port);
    }
    else {
      port = DEFAULT_PORT;
      System.out.printf("Using port %d (can override via command-line arg)%n", port);
    }
    try (FakeMailServer mailServer = new FakeMailServer(port)) {
      int lastCount = mailServer.getReceivedEmailCount();
      while (true) {
        int newCount = mailServer.getReceivedEmailCount();
        if (newCount > lastCount) {
          List<WiserMessage> messages = mailServer.wiser.getMessages();
          for (WiserMessage message : messages.subList(lastCount, newCount)) {
            message.dumpMessage(System.out);
          }
        }
        lastCount = newCount;
        ThreadUtils.sleepUnchecked(1000);
      }
    }
  }
}
