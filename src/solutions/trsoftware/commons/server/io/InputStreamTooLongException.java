/*
 * Copyright 2021 TR Software Inc.
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
 */

package solutions.trsoftware.commons.server.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Exception thrown by {@link ServerIOUtils#copyInputToOutput(InputStream, OutputStream, int, long)} to indicate
 * that it has read more bytes from the input stream than the limit specified by its {@code inputLengthLimit} argument.
 */
public class InputStreamTooLongException extends Exception {
  /**
   * The number of bytes have been copied from the input stream to the output stream
   * at the time when the {@link ServerIOUtils#copyInputToOutput(InputStream, OutputStream, int, long)} operation
   * was interrupted by throwing this exception.  
   */
  private final long numBytesCopied;
  /**
   * The value of the {@code inputLengthLimit} argument passed to 
   * {@link ServerIOUtils#copyInputToOutput(InputStream, OutputStream, int, long)} that caused it to be interrupted by 
   * throwing this exception. 
   */
  private final long inputLengthLimit;
  /**
   * Bytes that have already been read from the input stream but not copied to the output.
   * This is the input buffer at the time when the
   * {@link ServerIOUtils#copyInputToOutput(InputStream, OutputStream, int, long)} operation was interrupted
   * by throwing this exception.
   * The valid indices in this buffer are 0 through {@link #numExtraInputBytes} - 1.  
   * <p>
   * Having this information allows code that handles this exception to continue the copying operation from the point
   * where it was interrupted.
   *
   * @see #continueCopying(InputStream, OutputStream)
   */
  private final byte[] extraInputBytes;
  /**
   * The number of valid bytes in the {@link #extraInputBytes} buffer.
   * <p>
   * Having this information allows code that handles this exception to continue the copying operation from the point
   * where it was interrupted.
   *
   * @see #continueCopying(InputStream, OutputStream)
   */
  private final int numExtraInputBytes;

  /**
   * @param numBytesCopied see {@link #getNumBytesCopied()}
   * @param inputLengthLimit see {@link #getInputLengthLimit()}
   * @param extraInputBytes see {@link #getExtraInputBytes()}
   * @param numExtraInputBytes see {@link #getNumExtraInputBytes()}
   */
  public InputStreamTooLongException(long numBytesCopied, long inputLengthLimit, byte[] extraInputBytes, int numExtraInputBytes) {
    super("The number of bytes read from the input stream exceeded the limit of " + inputLengthLimit);
    this.numBytesCopied = numBytesCopied;
    this.inputLengthLimit = inputLengthLimit;
    this.extraInputBytes = extraInputBytes;
    this.numExtraInputBytes = numExtraInputBytes;
  }

  /**
   * The number of bytes have been copied from the input stream to the output stream
   * at the time when the {@link ServerIOUtils#copyInputToOutput(InputStream, OutputStream, int, long)} operation
   * was interrupted by throwing this exception.  
   */
  public long getNumBytesCopied() {
    return numBytesCopied;
  }

  /**
   * The value of the {@code inputLengthLimit} argument passed to 
   * {@link ServerIOUtils#copyInputToOutput(InputStream, OutputStream, int, long)} that caused it to be interrupted by 
   * throwing this exception.
   */
  public long getInputLengthLimit() {
    return inputLengthLimit;
  }

  /**
   * Bytes that have already been read from the input stream but not copied to the output.
   * This is the input buffer at the time when the
   * {@link ServerIOUtils#copyInputToOutput(InputStream, OutputStream, int, long)} operation was interrupted
   * by throwing this exception.
   * The valid indices in this buffer are 0 through {@link #numExtraInputBytes} - 1.  
   * <p>
   * Having this information allows code that handles this exception to continue the copying operation from the point
   * where it was interrupted.
   *
   * @see #continueCopying(InputStream, OutputStream)
   */
  public byte[] getExtraInputBytes() {
    return extraInputBytes;
  }

  /**
   * The number of valid bytes in the {@link #extraInputBytes} buffer.
   * <p>
   * Having this information allows code that handles this exception to continue the copying operation from the point
   * where it was interrupted.
   *
   * @see #continueCopying(InputStream, OutputStream)
   */
  public int getNumExtraInputBytes() {
    return numExtraInputBytes;
  }


  /**
   * Resumes the copying operation that resulted in {@link ServerIOUtils#copyInputToOutput(InputStream, OutputStream, int, long)}
   * throwing this exception from the point where it left off.
   * <p>
   * This method should produce the same outcome as if {@link ServerIOUtils#copyInputToOutput} had not been interrupted.
   * <p>
   * <strong>NOTE:</strong> calling this method more than once will produce inconsistent results.
   *
   * <p style="color: #0073BF; font-weight: bold;">
   *   TODO: unit test this
   * </p>
   *
   * @param from should be the same object that was passed to the original invocation of
   * {@link ServerIOUtils#copyInputToOutput} that triggered this exception
   * @param to should be the same object that was passed to the original invocation of
   * {@link ServerIOUtils#copyInputToOutput(InputStream, OutputStream, int, long)} that triggered this exception
   *
   * @return the total number of bytes that were written to the output stream (should be the same as the number
   * of bytes read from the input stream).
   */
  public long continueCopying(InputStream from, OutputStream to) throws IOException {
    long bytesCopied = numBytesCopied;
    // first write the bytes that have not been written yet
    to.write(extraInputBytes, 0, numExtraInputBytes);
    bytesCopied += numExtraInputBytes;
    // copy the remainder
    bytesCopied += ServerIOUtils.copyInputToOutput(from, to);
    return bytesCopied;
    /*
    NOTE: in the future, might consider providing an overload of this method that allows passing a new limit and
    uses ServerIOUtils#copyInputToOutput(InputStream, OutputStream, int, long), which might throw another
    InputStreamTooLongException
     */
  }

}
