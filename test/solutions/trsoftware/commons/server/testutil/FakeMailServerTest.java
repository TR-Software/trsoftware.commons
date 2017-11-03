package solutions.trsoftware.commons.server.testutil;

import com.dumbster.smtp.SmtpMessage;
import junit.framework.*;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/** @author Alex, 10/8/13 */
public class FakeMailServerTest extends TestCase {

  private final int port = 25253;
  private FakeMailServer fakeMailServer;

  public void setUp() throws Exception {
    super.setUp();
    fakeMailServer = new FakeMailServer(port);
  }

  public void testReceivingMail() throws Exception {
    // configure the JavaMail API
    Properties props = new Properties();
    props.put("mail.debug", true);
    props.put("mail.smtp.host", "localhost");
    props.put("mail.smtp.port", Integer.toString(port));
    Session session = Session.getInstance(props);
    // send an email
    String msgBody = "Hello";
    Message msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress("admin@example.com", "Example.com Admin"));
    msg.addRecipient(Message.RecipientType.TO,
        new InternetAddress("user@example.com", "Mr. User"));
    String subject = "Your Example.com account has been activated";
    msg.setSubject(subject);
    msg.setText(msgBody);
    Transport.send(msg);
    // verify receipt
    SmtpMessage receivedMessage = fakeMailServer.assertNewMessageCount(1).get(0);
    assertEquals(msgBody, receivedMessage.getBody());
    assertEquals(subject, receivedMessage.getHeaderValue("Subject"));
  }
}
