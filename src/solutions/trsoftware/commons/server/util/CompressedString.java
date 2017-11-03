package solutions.trsoftware.commons.server.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A container for storing a string as a gzipped byte array, and uncompressing
 * it on demand, in the toString method.
 * 
 * @author Alex Epshteyn
 */
public class CompressedString {
  private byte[] gzippedValue;
  private int originalValueByteLength;

  public CompressedString(String value) {
    byte[] valueBytes = value.getBytes();
    originalValueByteLength = valueBytes.length;
    ByteArrayOutputStream baOut = new ByteArrayOutputStream();
    try {
      GZIPOutputStream gzOut = new GZIPOutputStream(baOut);  // make the buffer big enough to fit the whole string
      gzOut.write(valueBytes);
      gzOut.close();
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    gzippedValue = baOut.toByteArray();
  }

  @Override
  public String toString() {
    try {
      GZIPInputStream gzIn = toStream();
      byte[] unzippedBytes = new byte[originalValueByteLength];
      gzIn.read(unzippedBytes);
      return new String(unzippedBytes);
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /** Opens an input stream for reading the contents */
  public GZIPInputStream toStream() {
    try {
      return new GZIPInputStream(new ByteArrayInputStream(gzippedValue));
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  /** The number of bytes in the original string */
  public int getOriginalSize() {
    return originalValueByteLength;
  }

  /** The number of bytes in the compressed string */
  public int getCompressedSize() {
    return gzippedValue.length;
  }


}
