package solutions.trsoftware.commons.server.testutil;

import com.google.common.base.MoreObjects;
import solutions.trsoftware.commons.server.util.RuntimeUtils;

import java.io.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Allows running a {@link Callable} in a sub-process, to test logic that depends on different JVM parameters
 * (e.g. whether assertions are enabled with an {@code -ea} flag).
 * <p>
 * The {@link #main} method of this class reads a serialized {@link Command} object from stdin and writes the serialized
 * result (either a {@link NormalResult} or {@link ExceptionalResult}) to stdout.
 * <p>
 * The static {@link #invokeInSubProcess(List, Command)} method facilitates forking the current JVM and
 * communicating with the sub-process to send the command and return the response.
 *
 * @author Alex
 * @since 3/3/2026
 */
public class SubProcessTester {

  // TODO: might make more sense to use RMI for this (see https://docs.oracle.com/javase/8/docs/technotes/guides/rmi/)

  /**
   * Invokes the given command in a new java process with the given JVM args (e.g. {@code "-ea"}).
   * The {@code "-classpath"} arg will be set automatically to match the current java process.
   * The new java process will execute {@link SubProcessTester#main(String[])}, which reads the serialized
   * {@code command} object from stdin and writes a serialized {@link Result} object to stdout.
   * This method handles all the forking and serialization logic needed to make that happen.
   *
   * @param jvmArgs any additional VM options (e.g. {@code "-ea"});
   *   {@code "-classpath"} will be set automatically based on the current java process
   * @param command the code to execute: ideally this should be a standalone static class
   *   to ensure that it doesn't capture any non-serializable references (e.g. the parent unit test instance)
   * @param <T> the serializable return type of the command
   * @return the response from the sub-process containing the result of the command invocation
   * @see RuntimeUtils#buildNewJavaProcess()
   */
  public static <T extends Serializable> Result invokeInSubProcess(List<String> jvmArgs, Command<T> command) throws Exception {
    ProcessBuilder processBuilder = RuntimeUtils.buildNewJavaProcess();
    processBuilder.command().addAll(jvmArgs);
    processBuilder.command().add(SubProcessTester.class.getName());
    System.out.println("Starting " + String.join(" ", processBuilder.command()));
    Process subprocess = processBuilder.start();
    BufferedReader stderrReader = new BufferedReader(new InputStreamReader(subprocess.getErrorStream()));
    OutputStream stdin = subprocess.getOutputStream();
    InputStream stdout = subprocess.getInputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(stdin);
    objOut.flush();
    // send the serialized object to be executed in subprocess
    objOut.writeObject(command);
    objOut.flush();
    boolean finished = subprocess.waitFor(10, TimeUnit.SECONDS);
    if (!finished) {
      subprocess.destroyForcibly().waitFor();
      throw new RuntimeException("Subprocess timed out");  // TODO: handle this
    }
    int exitStatus = subprocess.exitValue();
    if (exitStatus != 0)
      throw new RuntimeException(String.format("%s subprocess failed to execute command %s",
          SubProcessTester.class.getSimpleName(), command));

    // read the response object
    ObjectInputStream objIn = new ObjectInputStream(stdout);
    Object response = objIn.readObject();
    return (Result)response;
  }

  /**
   * Reads a serialized {@link Command} object from stdin, executes it, and writes a serialized {@link Result}
   * object to stdout.
   *
   * @param args ignored
   */
  public static void main(String[] args) throws Exception {
    ObjectInputStream objInputStream = new ObjectInputStream(System.in);
    Object arg = objInputStream.readObject();
    Result response = null;
    if (arg instanceof Command) {
      Callable<?> callable = (Callable<?>)arg;
      try {
        Object result = callable.call();
        response = new NormalResult((Serializable)result);
      }
      catch (Throwable ex) {
        ex.printStackTrace();
        response = new ExceptionalResult(ex);
      }
    }
    ObjectOutputStream objOutputStream = new ObjectOutputStream(new BufferedOutputStream(System.out));
    objOutputStream.writeObject(response);
    objOutputStream.flush();
  }

  /**
   * A callable to be executed in a different java process, returning a {@link Serializable} result.
   * This object is read from stdin by {@link #main(String[])}.
   *
   * @see #invokeInSubProcess(List, Command)
   * @see #main(String[])
   * @see Result
   */
  public interface Command<T extends Serializable> extends Callable<T>, Serializable {

  }

  /**
   * Wrapper for the outcome of an executed {@link Command}.
   * This object is written to stdout by {@link #main(String[])}.
   * @see NormalResult
   * @see ExceptionalResult
   */
  public static abstract class Result implements Serializable {
    protected Serializable result;

    protected Result(Serializable result) {
      this.result = result;
    }

    private Result() {  // default constructor for serialization
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .addValue(result)
          .toString();
    }
  }

  /**
   * Wrapper for the object returned {@link Command#call()}, if it didn't throw an exception.
   * @see ExceptionalResult
   */
  public static class NormalResult extends Result {
    public NormalResult(Serializable result) {
      super(result);
    }

    private NormalResult() {  // default constructor for serialization
    }

    /**
     * @return the value returned by {@link Command#call()}
     */
    public Object getResult() {
      return result;
    }
  }

  /**
   * Wrapper for the exception thrown by {@link Command#call()}, if it didn't return normally.
   * @see NormalResult
   */
  public static class ExceptionalResult extends Result {
    public ExceptionalResult(Throwable result) {
      super(result);
    }

    private ExceptionalResult() {  // default constructor for serialization
    }

    public Throwable getException() {
      return (Throwable)result;
    }
  }
}
