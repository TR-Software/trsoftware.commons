package solutions.trsoftware.commons.shared.io;

import com.google.common.io.BaseEncoding;


/**
 * GWT-compatible implementation of {@link Base64Encoding} using Guava's {@link BaseEncoding}.
 *
 * @author Alex
 * @since 12/20/2025
 */
class Base64EncodingImpl extends Base64Encoding {
  // GWT-compatible emulated version of solutions.trsoftware.commons.shared.io.Base64EncodingImpl

  private final BaseEncoding encoding;

  Base64EncodingImpl(boolean url, boolean withoutPadding) {
    super(url, withoutPadding);
    BaseEncoding encoding = url ? BaseEncoding.base64Url() : BaseEncoding.base64();
    this.encoding = withoutPadding ? encoding.omitPadding() : encoding;

  }

  @Override
  public String encode(byte[] bytes) {
    return encoding.encode(bytes);
  }

  @Override
  public byte[] decode(String chars) {
    return encoding.decode(chars);
  }
}
