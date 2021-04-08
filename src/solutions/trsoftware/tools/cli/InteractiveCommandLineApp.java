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

package solutions.trsoftware.tools.cli;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import solutions.trsoftware.commons.shared.util.NumberRange;
import solutions.trsoftware.tools.util.TablePrinter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * A simple framework for building a menu-driven interactive command line app with a text-based UI.
 *
 * <h3>Usage:</h3>
 * Simply extend this class, and for each menu command, define a {@code public static} inner class that implements
 * {@link CommandLineAction}.  All of these inner classes will be automatically glued together to produce the
 * interactive menu of the app.
 *
 * @since Feb 17, 2010
 * @author Alex
 */
public abstract class InteractiveCommandLineApp implements Runnable {

  private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PATTERN);

  public interface CommandLineAction {
    String getLabel();
    void execute(BufferedReader in, PrintStream out) throws Exception;
  }

  /**
   * Abstract base class providing a skeleton implementation of {@link CommandLineAction}
   */
  protected static abstract class BaseAction implements CommandLineAction {
    @Override
    public String getLabel() {
      return getClass().getSimpleName();
    }
  }

  protected static class QuitAction implements CommandLineAction {
    public String getLabel() {
      return "Quit";
    }

    public void execute(BufferedReader in, PrintStream out) throws Exception {
      out.println("Exiting.");
      System.exit(0);
    }
  }

  private List<CommandLineAction> actions = new ArrayList<CommandLineAction>();

  /** Lowercase letter shortcuts for each command */
  private BiMap<CommandLineAction, Character> shortcutChars = HashBiMap.create();

  /** Integer selectors for each command */
  private BiMap<CommandLineAction, Integer> selectorInts = HashBiMap.create();

  private char getShortcutForAction(CommandLineAction action) {
    if (shortcutChars.containsKey(action))
      return shortcutChars.get(action);
    // get the first available letter to use for the shortcut
    String label = action.getLabel().toLowerCase();
    for (int i = 0; i < label.length(); i++) {
      Character shortcut = label.charAt(i);
      if (!shortcutChars.containsValue(shortcut)) {
        // this is it
        return shortcut;
      }
    }
    throw new IllegalStateException("Unable to create a keyboard shortcut for " + action.getClass().getSimpleName() + ": ran out of characters");
  }

  protected InteractiveCommandLineApp() {
    // search the inner classes for implementors of CommandLineAction
    Class<? extends InteractiveCommandLineApp> myClass = getClass();
    List<Class<?>> innerClasses = new ArrayList<Class<?>>(Arrays.asList(myClass.getClasses()));
    innerClasses.add(QuitAction.class);  // the quit action must be added manually
    int i = 1;
    for (Class<?> c : innerClasses) {
      if (!c.equals(CommandLineAction.class) && CommandLineAction.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())) {
        // this is an action class
        CommandLineAction action = null;
        try {
          action = (CommandLineAction)c.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
        actions.add(action);
        selectorInts.put(action, i++);
        shortcutChars.put(action, getShortcutForAction(action));
      }
    }
  }

  protected void onBeforeRun(BufferedReader in, PrintStream out) throws Exception {
    // subclasses may override
  }

  protected void onAfterRun(BufferedReader input, PrintStream output) {
    // subclasses may override
  }

  protected String getName() {
    // subclasses may override
    return getClass().getSimpleName();
  }

  @Override
  public void run() {
    //  open up stdin
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    PrintStream out = System.out;

    try {
      onBeforeRun(in, out);
      // keep rendering the main menu in a loop
      while (true) {
        printMainMenu(out);
        String menuSelection = promptForInput(in, "Command: ").trim().toLowerCase();
        CommandLineAction selectedAction;
        if (menuSelection.matches("\\d+")) // a number was input
          selectedAction = selectorInts.inverse().get(new Integer(menuSelection));
        else if (menuSelection.matches("[a-z]")) // a char was input
          selectedAction = shortcutChars.inverse().get(menuSelection.charAt(0));
        else {
          out.println("Unrecognized selection, please try again.");
          continue;
        }
        try {
          selectedAction.execute(in, out);
        } catch (ActionAborted e) {
          System.out.printf("Aborted '%s'%n", e.getMessage());
        }
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
      }
    finally {
      onAfterRun(in, out);
    }
  }

  /**
   *
   *<pre>
   * ╔════
   * ║
   *</pre>
   * @param out
   */
  protected void printMainMenu(PrintStream out) {
    ArrayList<String> lines = new ArrayList<>();
    for (CommandLineAction action : actions) {
      String label = action.getLabel().replaceFirst("(?i)("+shortcutChars.get(action)+")", "[$1]");
      if (action.getClass().getAnnotation(Deprecated.class) != null)
        label += " **DEPRECATED**";
      lines.add(String.format("%d - %s", selectorInts.get(action), label));
    }
    TablePrinter.printMenu(out, getName() + " Main Menu:", lines);

  }

  /**
   * Prints the given message and reads the next line of user input from the given reader.
   * @param br the input reader
   * @param prompt the prompt message to print
   * @return input entered by the user in response to this prompt
   */
  public static String promptForInput(BufferedReader br, String prompt) throws IOException {
    System.out.print(prompt);
    return br.readLine().trim();
  }

  /**
   * Prints the given message and reads the next line of user input from the given reader.
   * @param br the input reader
   * @param prompt the prompt message to print
   * @param parser will be used to parse the input to the desired type
   * @return input entered by the user in response to this prompt, parsed using the given parser function
   */
  public static <T> T promptForInput(BufferedReader br, String prompt, Function<String, T> parser) throws IOException {
    return parser.apply(promptForInput(br, prompt));
  }

  /**
   * Prints the given message and reads the next line of user input from the given reader, interpreting it as an integer.
   *
   * @param br the input reader
   * @param prompt the prompt message to print
   * @return input entered by the user in response to this prompt, parsed as an integer
   */
  public static int promptForInteger(BufferedReader br, String prompt) throws IOException {
    try {
      return Integer.parseInt(promptForInput(br, prompt));
    }
    catch (NumberFormatException e) {
      return promptForInteger(br, "ERROR: Unable to parse input as integer; please try again: ");
    }
  }

  /**
   * Prints the given message and reads the next line of user input from the given reader, interpreting it as an integer
   * within the given bounds.  If the input is out of range, the user will be prompted to redo the entry.
   *
   * @param br the input reader
   * @param prompt the prompt message to print
   * @param min the lowest acceptable input value (inclusive)
   * @param max the highest acceptable input value (inclusive)
   * @return input entered by the user in response to this prompt, parsed as an integer
   */
  public static int promptForInteger(BufferedReader br, String prompt, int min, int max) throws IOException {
    do {
      int val = promptForInteger(br, prompt);
      if (!NumberRange.inRange(min, max, val)) {
        String errMsg = String.format("ERROR: Please enter an integer within the range %d..%d: ", min, max);
        return promptForInteger(br, errMsg, min, max);
      } else {
        return val;
      }
    } while (true);
  }

  /**
   * Prompts for a string formatted as {@value #DATE_FORMAT_PATTERN} and returns the result parsed as a {@link Date}.
   *
   * @param in the input reader
   * @param label short description of the requested value, to be included in the prompt message
   * @return the entered date
   */
  public static Date promptForDate(BufferedReader in, String label) throws ParseException, IOException {
    return DATE_FORMAT.parse(promptForInput(in, String.format("Enter %s (%s): ", label, DATE_FORMAT_PATTERN)));
  }

  /**
   * Prints the given message followed by a prompt for a yes/no response.
   * @param br the input reader
   * @param message will be printed above the prompt
   * @throws ActionAborted if the user enters anything except {@code "yes"} in response to this prompt
   */
  protected static void confirm(BufferedReader br, String message) throws IOException, ActionAborted {
    System.out.println(message);
    System.out.print("Are you sure you want to continue? (yes/no): ");

    if (!"yes".equals(br.readLine())) {
      throw new ActionAborted(message);
    }
  }

  protected static class ActionAborted extends RuntimeException {
    public ActionAborted(String message) {
      super(message);
    }
  }
}
