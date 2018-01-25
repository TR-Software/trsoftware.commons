/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.tools;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple framework for building a menu-driven interactive command line app.
 *
 * To use it, simply extend this class, and for each menu command, define a public inner class that implements
 * {@link CommandLineAction}.  All of these inner classes will be automatically glued together to produce the
 * interactive menu of the app.
 *
 * Feb 17, 2010
 *
 * @author Alex
 */
public abstract class CommandLineApp {

  public interface CommandLineAction {
    String getLabel();
    void execute(BufferedReader in, PrintStream out) throws Exception;
  }


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

  protected CommandLineApp() {
    // search the inner classes for implementors of CommandLineAction
    Class<? extends CommandLineApp> myClass = getClass();
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

  public void run() {
    //  open up stdin
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    PrintStream out = System.out;

    try {
      onBeforeRun(in, out);
      // keep rendering the main menu in a loop
      while (true) {
        out.println(getName() + " Main Menu:");
        for (CommandLineAction action : actions) {
          String label = action.getLabel().replaceFirst("(?i)("+shortcutChars.get(action)+")", "[$1]");
          out.printf("%d - %s%n", selectorInts.get(action), label);
        }
        String menuSelection = in.readLine().trim().toLowerCase();
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
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    finally {
      onAfterRun(in, out);
    }
  }

  static class ActionAborted extends RuntimeException {
    ActionAborted(String message) {
      super(message);
    }
  }
}
