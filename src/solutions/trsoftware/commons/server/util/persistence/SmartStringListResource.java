/*
 * Copyright 2018 TR Software Inc.
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
 *
 */

package solutions.trsoftware.commons.server.util.persistence;

import solutions.trsoftware.commons.shared.util.callables.Function1;

import java.io.*;
import java.util.*;

import static solutions.trsoftware.commons.server.io.ServerIOUtils.*;

/**
 * Represents a writeable resource of delimited strings.
 *
 * This class is a more powerful version of StringSetResourceFile, because
 * it supports add and remove operations which allow modifying the list
 * piecemeal, instead of the bulk getContent and setContent operations
 * allowed by StringSetResourceFile.
 * 
 * Nov 4, 2009
 *
 * @author Alex
 * @deprecated Replace with {@link ObjectToFileMapping}
 */
public class SmartStringListResource<V extends Collection<String>> {

  private final File resourceFile;
  private final Function1<List<String>, V> parser;
  private final String separators;

  public static final Function1<List<String>, Set<String>> SET_PARSER =
      new Function1<List<String>, Set<String>>() {
        public Set<String> call(List<String> arg) {
          return new HashSet<String>(arg);
        }
      };

  private volatile V cachedValue;

  public SmartStringListResource(File resourceFile) {
    this(resourceFile, null);
  }

  /**
   * Loads a comma-separated list of strings from the given text file resource
   * @param parser An optional transformer for the list of strings into some
   * other desired data structure (e.g. Set<String>)
   */
  public SmartStringListResource(File resourceFile, Function1<List<String>, V> parser) {
    this(resourceFile, parser, ", ");
    reload();
  }

  /**
   * Loads a comma-separated list of strings from the given text file resource
   * @param parser An optional transformer for the list of strings into some
   * other desired data structure (e.g. Set<String>)
   */
  public SmartStringListResource(File resourceFile, Function1<List<String>, V> parser, String separators) {
    this.resourceFile = resourceFile;
    this.parser = parser;
    this.separators = separators;
    reload();
  }

  /**
   * Called at instance creation time and can also be called at a later time
   * to refresh changes from disk.
   */
  public synchronized void reload() {
    ArrayList<String> lines = readLinesFromFile();
    ArrayList<String> strings = new ArrayList<String>();
    for (String line : lines) {
      StringTokenizer tokenizer = new StringTokenizer(line, separators);
      while (tokenizer.hasMoreTokens())
        strings.add(tokenizer.nextToken());
    }
    if (parser != null)
      cachedValue = parser.call(strings);
    else
      cachedValue = (V)strings;
  }

  private ArrayList<String> readLinesFromFile() {
    ArrayList<String> lines = null;
    try {
      lines = readLines(readFileUTF8(resourceFile), true);
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return lines;
  }

  /** Adds the given string to the last line of the resource file, and updates the values in memory */
  public synchronized void add(String item) {
    Writer writer = null;
    try {
      writer = writeFileUTF8(resourceFile, true);
      writer.append(separators.charAt(0));
      writer.append(item);
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    finally {
      try {
        if (writer != null)
          writer.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    reload();
  }


  /** Removes the first occurrence of the given string from the resource file, and updates the values in memory */
  public synchronized void remove(String item) {
    if (!cachedValue.contains(item))
      return;
    PrintWriter writer = null;
    try {
      ArrayList<String> lines = readLinesFromFile();
      writer = new PrintWriter(writeFileUTF8(resourceFile));
      boolean stopReplacing = false;
      Iterator<String> lineIter = lines.iterator();
      while (lineIter.hasNext()) {
        String line = lineIter.next();
        // replace all occurences of the token on each line
        if (!stopReplacing) {
          // example regex: (^|[, ]+)item($|[, ]+)
          String replacedLine =  line.replaceFirst(String.format("(^|[%s]+)%s($|[%s]+)", separators, item, separators), "$1");
          if (!replacedLine.equals(line)) {
            // a replacement was made
            stopReplacing = true;
          }
          line = replacedLine;
        }
        if (lineIter.hasNext())
          writer.println(line);
        else
          writer.print(line);  // don't print a line break after the last line (o.w. you get an extra line in the file)
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    finally {
      if (writer != null)
        writer.close();
    }
    reload();
  }

  public V getStrings() {
    return cachedValue;
  }
}
