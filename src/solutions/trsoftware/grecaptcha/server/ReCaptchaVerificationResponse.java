package solutions.trsoftware.grecaptcha.server;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;

/**
 * Wrapper class for the JSON object returned from {@value #VERIFY_TOKEN_URL}.
 *
 * Can be parsed from the HTML response body using {@link Gson#fromJson(String, Class)}.
 *
 * @author Alex
 * @since 4/28/2020
 *
 * @see <a href="https://developers.google.com/recaptcha/docs/verify#api_response">reCAPTCHA docs</a>
 */
public class ReCaptchaVerificationResponse {

  /**
   * reCAPTCHA API endpoint for verifying a response token
   */
  public static final String VERIFY_TOKEN_URL = "https://www.google.com/recaptcha/api/siteverify";

  private boolean success;

  /**
   * Timestamp of the challenge load (ISO format yyyy-MM-dd'T'HH:mm:ssZZ)
   */
  private String challenge_ts;

  /**
   * The hostname of the site where the reCAPTCHA was solved
   */
  private String hostname;

  @SerializedName("error-codes")
  private List<String> errorCodes;


  /**
   * @return {@code true} iff the token was verified successfully
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * @return timestamp of the challenge load
   */
  @Nullable
  public Instant getChallengeTimestamp() {
    return challenge_ts != null ? Instant.parse(challenge_ts) : null;
  }

  @Nullable
  public String getHostname() {
    return hostname;
  }

  /**
   * @return any error codes included in the response, or {@code null} if the response did not contain this field
   *
   * @see <a href="https://developers.google.com/recaptcha/docs/verify#error_code_reference">Error code reference</a>
   */
  @Nullable
  public List<String> getErrorCodes() {
    return errorCodes;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("success", success)
        .add("challenge_ts", challenge_ts)
        .add("hostname", hostname)
        .add("errorCodes", errorCodes)
        .toString();
  }
}
