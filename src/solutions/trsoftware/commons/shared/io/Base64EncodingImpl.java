package solutions.trsoftware.commons.shared.io;

import com.google.gwt.core.shared.GwtIncompatible;

import java.util.Base64;

/**
 * GWT-incompatible implementation of {@link Base64Encoding} using {@link Base64 java.util.Base64},
 * which is ~1.7x faster than Guava's {@link com.google.common.io.BaseEncoding}.
 * <p>
 * <b>Note:</b> this class is emulated for GWT using {@link com.google.common.io.BaseEncoding}
 * in the {@code super-source} version.
 *
 * @author Alex
 * @since 12/20/2025
 * @see <a href="test/solutions/trsoftware/commons/shared/io/Base64EncodingBenchmark.ods">Benchmark results spreadsheet</a>
 */
// super-source version: src/solutions/trsoftware/commons/translatable/solutions/trsoftware/commons/shared/io/Base64EncodingImpl.java
@GwtIncompatible("java.util.Base64")
@SuppressWarnings("NonJREEmulationClassesInClientCode")
class Base64EncodingImpl extends Base64Encoding {
  // Benchmark results: test/solutions/trsoftware/commons/shared/io/Base64EncodingBenchmark.ods

  private final Base64.Encoder encoder;
  private final Base64.Decoder decoder;

  Base64EncodingImpl(boolean url, boolean withoutPadding) {
    super(url, withoutPadding);
    Base64.Encoder encoder;
    if (url) {
      encoder = Base64.getUrlEncoder();
      this.decoder = Base64.getUrlDecoder();
    }
    else {
      encoder = Base64.getEncoder();
      this.decoder = Base64.getDecoder();
    }
    this.encoder = withoutPadding ? encoder.withoutPadding() : encoder;
  }

  @Override
  public String encode(byte[] bytes) {
    return encoder.encodeToString(bytes);
  }

  @Override
  public byte[] decode(String chars) {
    return decoder.decode(chars);
  }

}
