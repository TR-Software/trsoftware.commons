package solutions.trsoftware.commons.server.servlet.testutil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Provides a skeleton implementation of the {@link ServletInputStream} by wrapping an {@link InputStream},
 * delegating all standard {@link InputStream} methods to it, and throwing {@link UnsupportedOperationException}
 * for all the additional abstract methods declared by {@link ServletInputStream}.
 *
 * @author Alex
 * @since 7/31/2019
 */
public class ServletInputStreamAdapter extends ServletInputStream {

  private InputStream delegate;

  public ServletInputStreamAdapter(InputStream delegate) {
    this.delegate = Objects.requireNonNull(delegate);
  }

  @Override
  public boolean isFinished() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isReady() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setReadListener(ReadListener listener) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int read() throws IOException {
    return delegate.read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return delegate.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return delegate.read(b, off, len);
  }

  @Override
  public long skip(long n) throws IOException {
    return delegate.skip(n);
  }

  @Override
  public int available() throws IOException {
    return delegate.available();
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }

  @Override
  public void mark(int readlimit) {
    delegate.mark(readlimit);
  }

  @Override
  public void reset() throws IOException {
    delegate.reset();
  }

  @Override
  public boolean markSupported() {
    return delegate.markSupported();
  }
}
