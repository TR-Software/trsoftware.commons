package solutions.trsoftware.grecaptcha.server;

import com.google.gson.Gson;
import junit.framework.TestCase;

import java.time.Instant;
import java.util.Collections;

/**
 * @author Alex
 * @since 4/28/2020
 */
public class ReCaptchaVerificationResponseTest extends TestCase {

  public void testParsing() throws Exception {
    Gson gson = new Gson();
    {
      // check a response with an error code
      String json = "{\"success\": false, \"error-codes\": [\"invalid-input-secret\"]}";
      ReCaptchaVerificationResponse response = gson.fromJson(json, ReCaptchaVerificationResponse.class);
      assertEquals(false, response.isSuccess());
      assertNull(response.getChallengeTimestamp());
      assertNull(response.getHostname());
      assertEquals(Collections.singletonList("invalid-input-secret"), response.getErrorCodes());
    }
    {
      // check a successful response
      String json = "{\"success\": true, \"challenge_ts\": \"2020-04-28T19:14:08.640Z\", \"hostname\": \"example.com\"}";
      ReCaptchaVerificationResponse response = gson.fromJson(json, ReCaptchaVerificationResponse.class);
      assertEquals(true, response.isSuccess());
      assertEquals(Instant.parse("2020-04-28T19:14:08.640Z"), response.getChallengeTimestamp());
      assertEquals("example.com", response.getHostname());
      assertNull(response.getErrorCodes());
    }
  }
}