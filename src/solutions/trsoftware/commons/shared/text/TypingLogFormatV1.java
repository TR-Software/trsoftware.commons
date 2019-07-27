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

package solutions.trsoftware.commons.shared.text;

import solutions.trsoftware.commons.shared.util.Levenshtein;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides a parser and a formatter for the TypingLog v1 string encoding.
 *
 * The string serialization format is described by the following grammar spec,
 * which aims to eliminate redundancy as much as possible.  For instance,
 * the time values are all offsets from the previous value to reduce the number
 * of characters needed to represent it.
 *
 * TypingLog:
 *   version,language,charTimings,editLog
 * version:
 *   "TLvX"  (where X is the version number, e.g. "TLv1")
 * language:
 *   ISO 639-1 language code (2 chars)
 * charTimings:
 *   N,T_1,...,T_N  (where N = charTimings.length and T_i is the time elapsed since T_i-1)
 * editLog:
 *   wordCursor,N,TypingEdit_1,...,TypingEdit_N, (where N = number of TypingEdits at this same wordCursor)
 * TypingEdit:
 *   time,EditOperation_1...EditOperation_k
 * EditOperation:
 *   p$c  (character c substituted at position p)
 *   p-c  (character c deleted at position p)
 *   p+c  (character c inserted at position p)
 *
 * 
 * Nov 23, 2012
 * @author Alex
 */
public class TypingLogFormatV1 {
  /** The TypingLog format version that this class is designed to parse */
  public static final int VERSION = 1;
  /** The string representation of a {@link TypingLog} formatted using this class will start with this prefix */
  public static final String PREFIX = "TLv";

  private TypingLogFormatV1() {
    // this class cannot be instantiated
  }

  public static TypingLog parseTypingLog(String str) {
    return new Parser(str).parse();
  }

  public static boolean isTypingLog(String str) {
    return str.startsWith(PREFIX);
  }

  public static String formatTypingLog(TypingLog typingLog) {
    return new Formatter(typingLog).format();
  }



  public static class Parser {
    /** The string being parsed */
    private String str;
    /** The current position of the parser within {@link #str} */
    private int cursor;
    /** The parsed value for the language of the text */
    private Language textLanguage;
    /** The underlying text that the user is supposed to be typing */
    private StringBuilder text = new StringBuilder();
    /** The parsed timing of each char in {@link #text} */
    private int[] charTimings;

    /** Initializes a parser for a TypingLog from the given string representation */
    public Parser(String str) {
      this.str = str;
    }

    public TypingLog parse() {
      // 1) start by reading the version number
      if (!str.startsWith(PREFIX))
        throw parsingException();
      cursor = "TLv".length();
      int version = readInt();
      if (VERSION != version) {
        throw new IllegalArgumentException("Wrong TypingLog format version (given " + version + " but expected " + VERSION + ")");
      }
      // 2) parse the value of language
      textLanguage = Language.fromIsoCode(readStr());
      // 2) parse charTimings
      readCharTimings();
      // 3) finally, parse the edit list
      assert str.charAt(cursor) == '|'; // the char timings should be separated from the edit log with a pipe char
      // consume the pipe char
      cursor++;
      List<TypingEdit> typingEditList = readTypingEdits();
      return new TypingLog(text.toString(), textLanguage, charTimings, typingEditList);
    }

    private IllegalArgumentException parsingException() {
      // TODO: create a new exception class with cursor as a field
      return new IllegalArgumentException("Error parsing TypingLog string at position " + cursor + " (" + str + ")");
    }

    /** Reads an integer delimited by a comma and advances the cursor to the next position after the comma */
    private int readInt() {
      int nextCommaIndex = str.indexOf(',', cursor);
      int result = Integer.parseInt(str.substring(cursor, nextCommaIndex));
      // consume the comma
      cursor = nextCommaIndex + 1;
      return result;
    }

    /** Reads a string delimited by a comma and advances the cursor to the next position after the comma */
    private String readStr() {
      int nextCommaIndex = str.indexOf(',', cursor);
      String result = str.substring(cursor, nextCommaIndex);
      // consume the comma
      cursor = nextCommaIndex + 1;
      return result;
    }

    private void readCharTimings() {
      // first read the length of the text (which should be the same as the char timings array)
      int len = readInt();
      charTimings = new int[len];
      int time = 0; // the time since the start of the race
      // now read the chars of the underlying text and their timing values
      // TODO: cont here: rewrite this to read the actual text chars along with the timing values
      // TODO: cont here: decide whether to include commas as a delimiter (probably necessary, otherwise can't distinguish an actual text char from a time value if the char is a digit)
      for (int i = 0; i < charTimings.length; i++) {
        char c = str.charAt(cursor++);
        // we use '\b' (backspace) as the separator when the text char is a digit (to distinguish it from the time int that follows it)
        if (c == '\b')
          c = str.charAt(cursor++); // skip the separator char
        text.append(c);  // this is a character from the text
        // now read the time value that follows
        int timeStartPos = cursor;
        do {
          // we use '\b' (backspace) as the separator when the text char is a digit or '-' (to distinguish it from the time int that follows it)
          c = str.charAt(cursor++);
        } while (Character.isDigit(c) || c == '-');
        cursor--; // at this point, c is the next char in the log, so un-consume it
        int timeDiff = Integer.parseInt(str.substring(timeStartPos, cursor));
        time += timeDiff;
        charTimings[i] = time;
      }
    }

    private List<TypingEdit> readTypingEdits() {
      List<TypingEdit> ret = new ArrayList<TypingEdit>();
      int time = 0;
      while (cursor < str.length()) {
        // repeat until the end of the string:
        // read the next wordCursor and number of entries for that word cursor
        int textOffset = readInt();
        int n = readInt();
        for (int i = 0; i < n; i++) {
          // read the next TypingEdit
          time += readInt();
          List<Levenshtein.EditOperation> edits = new ArrayList<Levenshtein.EditOperation>();
          while (str.charAt(cursor) != ',') {
            edits.add(readEditOperation());
          }
          cursor++;  // consume the comma
          ret.add(new TypingEdit(textOffset, edits, time));
        }
      }
      return ret;
    }

    private Levenshtein.EditOperation readEditOperation() {
      // 1) read the position int
      int posStart = cursor;
      int posEnd = cursor;
      while (Character.isDigit(str.charAt(cursor))) {
        posEnd++;
        cursor++;
      }
      int pos = Integer.parseInt(str.substring(posStart, posEnd));
      // 2) read the type and the actual character
      char type = str.charAt(cursor++);
      char c = str.charAt(cursor++);
      switch (type) {
        case '+':
          return new Levenshtein.Insertion(pos, c);
        case '-':
          return new Levenshtein.Deletion(pos, c);
        case '$':
          return new Levenshtein.Substitution(pos, c);
        default:
          throw parsingException();
      }
    }

  }

  public static class Formatter {
    private final String text;
    private final Language textLanguage;
    private StringBuilder str = new StringBuilder();
    private int[] charTimings;
    private List<TypingEdit> editLog;

    /** Creates a serializer for the given TypingLog object. */
    public Formatter(TypingLog typingLog) {
      text = typingLog.getText();
      textLanguage = typingLog.getTextLanguage();
      charTimings = typingLog.getCharTimings();
      editLog = typingLog.getEditLog();
    }

    public String format() {
      str.append("TLv").append(VERSION).append(',').append(textLanguage.getIsoCode()).append(',');
      writeCharTimings();
      str.append('|'); // separate the char timings from the edit log with a pipe char
      writeEditLog();
      return str.toString();
    }

    private void writeCharTimings() {
      int timeOffset = 0;
      str.append(charTimings.length).append(',');
      for (int i = 0; i < charTimings.length; i++) {
        // the output for each char will be formatted as "[char][time]", where time is the time since last entry,
        // not the absolute time since the start of the race (this is an optimization to reduce log size
        // to save space, we write the time since last entry
        // NOTE: if the time is 0, i.e. we have a partial log and the user never typed this character,
        // the written value will be negative, (but that's okay, because such a log will never be saved in production).
        char c = text.charAt(i);
        int time = charTimings[i];
        if (Character.isDigit(c) || c == '-') {
          // have to prepend a separator char, otherwise this char will be indistinguishable from the time int that follows it
          str.append('\b'); // this is the non-printable char "backspace"
        }
        str.append(c).append(time - timeOffset);
        timeOffset = time;
      }
    }

    private void writeEditLog() {
      if (editLog.isEmpty())
        return;
      int textOffset = -1;
      int timeOffset = 0;
      List<TypingEdit> nextGroup;
      Iterator<TypingEdit> iter = editLog.iterator();
      TypingEdit nextEdit = iter.next();  // we know that the log has at least one entry
      do {
        // group all the edits for each textOffset: each iteration of the outer loop is for a single word group
        textOffset = nextEdit.getOffset();
        // 1) build a list of all the entries in this group
        nextGroup = new ArrayList<TypingEdit>();
        while (nextEdit != null && nextEdit.getOffset() == textOffset) {
          nextGroup.add(nextEdit);
          if (iter.hasNext())
            nextEdit = iter.next();
          else
            nextEdit = null;
        }
        // 2) now that we know the size, write the group
        str.append(textOffset).append(',').append(nextGroup.size()).append(',');
        for (TypingEdit typingEdit : nextGroup) {
          writeTypingEdit(typingEdit, timeOffset);
          str.append(',');
          timeOffset = typingEdit.getTime();
        }
      } while (nextEdit != null);
    }

    private void writeTypingEdit(TypingEdit typingEdit, int timeOffset) {
      str.append(typingEdit.getTime() - timeOffset).append(',');
      for (Levenshtein.EditOperation op : typingEdit.getEdits()) {
        str.append(op.getPosition());
        if (op instanceof Levenshtein.Insertion)
          str.append('+');
        else if (op instanceof Levenshtein.Deletion)
          str.append('-');
        else if (op instanceof Levenshtein.Substitution)
          str.append('$');
        else
          throw new IllegalStateException("Unrecognized EditOperation");
        str.append(op.getChar());
      }
    }
  }


}
