package solutions.trsoftware.commons.server.testutil;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import solutions.trsoftware.commons.client.util.Assert;
import solutions.trsoftware.commons.client.util.CollectionUtils;
import org.apache.commons.codec.binary.Base64;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates a dummy SMTP server for testing.
 *
 * @author Alex
 */
public class FakeMailServer extends Assert {
  private SimpleSmtpServer mailServer;

  /**
   * The number of emails received by the server after the last time assertReceivedCount
   * was called.
   */
  private int lastCount = 0;

  public FakeMailServer(int port) {
    mailServer = SimpleSmtpServer.start(port);
  }

  /**
   * Asserts that the fake mail server received count emails since it was last
   * checked, (presumably at the beginning of the unit test) waiting up to 10
   * seconds for this to happen.
   *
   * @return the last n=count email messages
   */
  public LinkedList<SmtpMessage> assertNewMessageCount(int expectedNewCount) throws Exception {
    if (expectedNewCount < 1)
      throw new IllegalArgumentException("Use assertNoEmailReceived instead");
    // ensure that the new messages have been received, waiting up to timeout millis
    int timeout = 5000;
    long startTime = System.currentTimeMillis();
    while (mailServer.getReceivedEmailSize() < lastCount + expectedNewCount) {
      if (System.currentTimeMillis() > (startTime + timeout)) {
        fail(String.format("Timed out while waiting for email to be received.  (expected new count=%d, actual new count=%d, total count=%d)", expectedNewCount, mailServer.getReceivedEmailSize()- lastCount, mailServer.getReceivedEmailSize()));
      }
      Thread.sleep(100);
    }
    int count = mailServer.getReceivedEmailSize();
    assertEquals(expectedNewCount, count - lastCount);
    // return only the new messages
    List<SmtpMessage> newMessages = CollectionUtils.<SmtpMessage>asList(mailServer.getReceivedEmail()).subList(lastCount, count);
    lastCount = count;
    return new LinkedList<SmtpMessage>(newMessages);
  }

  private static final Pattern BODY_HEADER_PATTERN = Pattern.compile(".*Content-Type: text/plain; charset=\"(.+?)\"MIME-Version: 1.0Content-Transfer-Encoding: (\\w+)\n(.*)", Pattern.DOTALL);

  /**
   * The result of {@link SmtpMessage#getBody } might be something like this:
   * <pre>
   *--===============1686450895==Content-Type: text/plain; charset="us-ascii"MIME-Version: 1.0Content-Transfer-Encoding: 7bit
   *Hello
   *--===============1686450895==--
   * </pre>
   * Or base64-encoded:
   * <pre>
   *--===============0105064596==Content-Type: text/plain; charset="utf-8"MIME-Version: 1.0Content-Transfer-Encoding: base64
   *SGVsbG8=
   *--===============0105064596==--
   * </pre>
   * Either way, this method should return "Hello" for these two examples.
   *
   * NOTE: this implementation has not been thoroughly tested - it's only been deemed good-enough for messages
   * sent by the Google App Engine SDK's local dev_appserver.
   */
  public static String extractMessageBodyText(SmtpMessage message) {
    String emailBody = message.getBody();
    Matcher matcher = BODY_HEADER_PATTERN.matcher(emailBody);
    if (matcher.matches()) {
      String charset = matcher.group(1);
      String encoding = matcher.group(2);
      String bodyText = matcher.group(3);
      if ("base64".equalsIgnoreCase(encoding)) {
        return new String(Base64.decodeBase64(bodyText.getBytes()));
      }
      return bodyText;
    }
    throw new IllegalArgumentException("Body message text doesn't match expected pattern");
  }

  /**
   * Asserts that the fake mail server received no new emails since it was last
   * checked, waiting up to 10 seconds to be sure.
   *
   * @return the email messages
   */
  public void assertNoEmailReceived() throws Exception {
    int oldCount = lastCount;
    // ensure that newCount email has been received, waiting up to 10 seconds
    int timeout = 10000;
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() < (startTime + timeout)) {
      assertEquals(oldCount, mailServer.getReceivedEmailSize());
      Thread.sleep(1000);
    }
    assertEquals(oldCount, mailServer.getReceivedEmailSize());
  }

  public void stop() {
    mailServer.stop();
  }

  public boolean isStopped() {
    return mailServer.isStopped();
  }

  /**
   * Runs dumbster and prints out all the received email to the command line, until
   * process is killed.
   */
  public static void main(String[] args) throws InterruptedException {
    int port = 25252;
    SimpleSmtpServer mailServer = SimpleSmtpServer.start(port);
    System.out.println("Dumbster SMTP Server started on port " + port);
    System.out.println("NOTE: Dumbster doesn't properly preserve line breaks in message bodies");
    try {
      int lastCount = mailServer.getReceivedEmailSize();
      while (true) {
        int newCount = mailServer.getReceivedEmailSize();
        if (newCount > lastCount) {
          LinkedList<SmtpMessage> messages = new LinkedList<SmtpMessage>();
          Iterator emailIter = mailServer.getReceivedEmail();
          while (emailIter.hasNext()) {
            messages.add((SmtpMessage)emailIter.next());
          }
          for (SmtpMessage message : messages.subList(lastCount, newCount)) {
            System.out.printf("New Email Message Received at %s:%n", new Date().toString());
            System.out.println("------------------------ Begin Message:");
            Iterator headerIter = message.getHeaderNames();
            while (headerIter.hasNext()) {
              String headerName = (String)headerIter.next();
              System.out.printf("%s: %s%n", headerName, message.getHeaderValue(headerName));
            }
            System.out.println(message.getBody());
            System.out.println("------------------------ End Message");
          }
        }
        lastCount = newCount;
        Thread.sleep(1000);
      }
    }
    finally {
      mailServer.stop();
    }
  }
}
