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

package solutions.trsoftware.commons.server.util.text;

import solutions.trsoftware.commons.client.util.stats.HashCounter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a Huffman-like compression algorithm with a 256 degree tree
 * (byte encoding instead of binary).  The leftmost half of the nodes at
 * each level all contain subrees while the rightmost half are all leaf nodes.
 * So we have the following levels:
 *
 * Level 0: implicit root
 * Level 1: 128 words
 * Level 2: 128^2 words = 16,384
 * Level 3: 128^3 words = 2,097,152
 *
 * The algorithm works as follows: when first invoked on the initial set of words,
 * it places the words that occur once on level 3 (filling it left to right),
 * those that occur two or more times on level 2.
 *
 * In the Brown Corpus, the 128th most frequent word has probability of 0.0701%,
 * (see http://www.edict.com.hk/lexiconindex/frequencylists/words2000.htm)
 * so ideally we'd like to put on level 0 words that have this probability or higher,
 * but first we need enough data.
 *
 * After we've read more than 100 words, we can be fairly certain that words
 * which have occurred at least 5 times (probability 5%) can be promoted to level 1.
 * When we promote a word, we leave its old location as well, to allow backwards
 * compatibility with previously-encoded strings.  By the time we reach 1000
 * words in the tree, a frequency of 5 means only 0.5% (which is rank 22 on the brown corpus).
 * The number 5 will remain the threshold for promotion to level 1 until we reach
 * a total of 7132 words in the tree, which gives us the 0.0701% of the 128th
 * most-frequent word in the Brown corpus.  After that, we use just use
 * this percentage as the threshold until level 1 fills up.
 *
 * When a level is full, words are put on the next available level regardless
 * of their frequency.  Levels are filled from left to right.
 *
 * Compression overview:
 *
 * Unless we have a corpus with more than 2 million unique words (which is impossible
 * for a natural language - the Brown Corpus only has 1 million words total),
 * then each word will be coded using at most 3 bytes.  Any word that occurs
 * more than once will be coded using 2 bytes, and the most frequent 128 words
 * (which hopefully account for 30% of all text, juding by the Brown Corpus),
 * will be coded using 1 byte.
 *
 * The compression will be effective if the quanity of text to compress is large
 * enough to account for all this coding tree metadata storage.
 *
 *
 * Oct 22, 2009
 *
 * @deprecated Almost works, but decoding gets a few words wrong here and there
 * (there's some kind of bug).  The worst problem though, is that it doesn't
 * offer good compression - the size of the coder itself is huge (the HashMap
 * takes up about 80% of the size).  But even if that were eliminated, we wouldn't
 * get much compression (almost 1:1 ratio still) because having lots of short
 * Strings (for the words) uses much more memory than a long String with the
 * full text.  The only advantage of this class is that it provides faster
 * tokenization (twice as fast) of a text than tokenizing the text string with
 * less overhead than storing the token arrays.  Conclusion (on 10/22/2009):
 * I don't think the number of texts in the database will ever get high
 * enough to justify revisiting this class. 
 *
 *
 * @author Alex
 */
public class AdaptiveHuffmanCoder implements Serializable {

  private Object[] level1 = new Object[ORDER];

  /** The total number of words (not unique) that have been processed so far */
  private int counter = 0;

  /** The number of children of a tree node */
  private static final int ORDER = 256;
  /** The first index in a level's node array than can contain leaf nodes */
  private static final int FIRST_LEAF_SLOT = 128;
  /** The last index in a level's node array than can contain leaf nodes */
  private static final int LAST_LEAF_SLOT = 255;
  /** Our tree has at most 3 levels */
  private static final int MAX_LEVEL = 3;


  private static class WordData implements Serializable {
    /** The number of times this word occurs in the text we've processed so far */
    private int count;
    /**
     * The shortest code to encode this word so far
     * (the length of this array gives the smallest level number on which the
     * word can be found).  
     */
    private int code;

    private WordData(int count, int code) {
      // we don't to remember more than 32767 occurrences of a word - if it has that many, it's probably been promoted to level 1 already
      this.count = count;
      this.code = code;
    }

  }

  /** Maps the word strings to their metadata objects, for fast encoding */
  Map<String, WordData> words = new HashMap<String, WordData>();

  public byte[] encode(String[] input) {
    updateCodingTree(input);
    byte[] buffer = new byte[input.length * 3];  // each word coded with at most 3 bytes
    int pos = 0;
    for (String word : input) {
      byte[] code = pathToByteArray(words.get(word).code);
      int len = code.length;
      System.arraycopy(code, 0, buffer, pos, len);
      pos += len;
    }
    // truncate the buffer
    byte[] result = new byte[pos];
    System.arraycopy(buffer, 0, result, 0, pos);
    return result;
  }

  public String[] decode(byte[] input) {
    ArrayList<String> result = new ArrayList<String>(input.length);
    Object[] nodes = level1;
    for (byte b : input) {
      int index = byteValueToIndex(b);
      if (index < FIRST_LEAF_SLOT)
        nodes = (Object[])nodes[index];  // descend into subtree
      else {
        // at a leaf node
        result.add((String)nodes[index]);
        nodes = level1;  // go back to level 1
      }
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * Inserts the missing words from the input into the tree and updates
   * the frequencies of existing words (possibly promoting their position).
   */
  private void updateCodingTree(String[] input) {
    // count the word frequencies in the input
    counter += input.length;
    HashCounter<String> wordCounts = new HashCounter<String>(input.length);
    for (String word : input) {
      wordCounts.increment(word);
    }
    // update our internal data based on these frequencies
    for (Map.Entry<String, Integer> count : wordCounts.entriesSortedByValueDescending()) {
      // iterate from most to least frequent (to ensure that more frequent words get better placement in the tree)
      String word = count.getKey();
      int n = count.getValue();  // # of occurrences
      WordData wordData = words.get(word);
      if (wordData == null) {
        // this is the first time we've seen this word; insert it into the tree
        int code = addToTree(determineLevelOfWord(n), word);
        words.put(word, new WordData(n, code));
      }
      else {
        // the word existed previously
        wordData.count += n;
        // see if the word can be promoted
        byte[] codeArr = pathToByteArray(wordData.code);
        int currentLevel = codeArr.length;
        int targetLevel = determineLevelOfWord(wordData.count);
        if (targetLevel < currentLevel) {
          // yes, can be promoted!
          int newCode = addToTree(targetLevel, word, currentLevel);
          if (newCode != 0)
            wordData.code = newCode;  // was promoted
        }
      }
    }
  }

  /** @return the level where a word with this many n should be located */
  private int determineLevelOfWord(int n) {
    if (n <= 1) {
      // this word should go into the lowest level - it occurs only once
      return 3;
    }
    else if (n < 5) {
      // occurs less than 5 times - should go into level 2
      return 2;
    }
    else if (counter <= 7132) {
      // occurs at least 5/7132 times (meaning frequency >= the level 1 threshold of 0.0701%)
      return 1;
    }
    else if (((double)n/counter) >= 0.000701) {
      return 1;  // words with frequency of at least 0.0701% can be promoted to level 1
    }
    else {
      return 2; // occurs at least 5 times but not enough frequency to be in level 1 
    }
  }

  /**
   * Adds the given word to the next available leaf node slot in the given level,
   * returning the path to that node as a byte array.
   *
   * If the level is full, tries the next lower level until the entire tree is full.
   *
   * @param level The level of the tree to use, between 1 and 3
   * @param word
   * @return the path to this leaf node as an int which can be converted to a byte array
   * using the pathToByteArray method; returns 0 to indicate failure (because the subtree was full)
   */
  int addToTree(int level, String word) {
    return addToTree(level, word, MAX_LEVEL+1);
  }
  /**
   * Adds the given word to the next available leaf node slot in the given level,
   * returning the path to that node as a byte array.
   *
   * @param level The level of the tree to use, between 1 and 3
   * @param word
   * @param maxLevel - don't try to insert deeper than this level - return null instead.
   * @return the path to this leaf node as an int which can be converted to a byte array
   * using the pathToByteArray method; returns 0 to indicate failure (because the subtree was full)
   */
  int addToTree(int level, String word, int maxLevel) {
    // traverse the tree to the next available slot the given level
    // this int will keep track of where we are in the tree using bitwise operations
    int path = addToSubtree(level1, 1, level, word, 0x100);
    while (path == 0) {
      // failure - this subtree's leaf nodes are full; keep trying deeper levels
      if (++level < maxLevel)
        path = addToSubtree(level1, 1, level, word, 0x100);
      else
        return 0;  // give up
    }
    return path;
  }

  /**
   *
   * @return the path to this leaf node as an int which can be converted to a byte array
   * using the pathToByteArray method; returns 0 to indicate failure (because the subtree was full)
   */
  private int addToSubtree(Object[] nodes, int level, int targetLevel, String word, int pathSoFar) {
    if (level == targetLevel) {
      // we're on our target level - find the next available leaf node
      for (int i = FIRST_LEAF_SLOT; i <= LAST_LEAF_SLOT; i++) {
        if (nodes[i] == null) {
          // we found our empty slot - insert the leaf here!
          nodes[i] = word;
          return pathSoFar | i;
        }
      }
      // if we get to this point, the above loop didn't return, meaning this level is full
      // so we return failure so that the caller will try to place the leaf on the next available level
      return 0;
    }
    // at this point we're either not on the desired level
    // we need to descend a level down this tree
    // pick a subtree to descend into
    for (int i = 0; i < FIRST_LEAF_SLOT; i++) {
      if (nodes[i] == null) {
        // this subtree doesn't exist yet - it needs to be created
        nodes[i] = new Object[ORDER];
      }
      // descend into this subtree
      int path = addToSubtree((Object[])nodes[i], level+1, targetLevel, word, (pathSoFar << 8) | i);
      if (path == 0)
        continue;  // failure - this subtree's leaf nodes are full, try the next subtree at this level
      else
        return path;  // success! the leaf was added to this subtree!
    }
    // we have failed to add the leaf - all subtrees at this level are full
    // return failure for the caller to try to add the leaf to a deeper level
    return 0;
  }


  /**
   * Converts an int like 0x01aabbcc to a byte array like byte[]{0xaa, 0xbb, 0xcc}
   * The most significant byte of the path we care about has to be 1 (e.g. 0x0100 or 0x010000)  
   */
  static byte[] pathToByteArray(int i) {
    // try all possible path lengths starting with 3 until a match is found
    for (int len = 3; len > 0; len--) {
      if (((i >> (8*len)) & 0xff) == 1) {
        // we've confirmed that this is our path length (the len'th byte is nonzero)
        // this happens when the byte just to the left of it is 1
        // e.g. for len == 3, the above operation is (i >> 24) & 0xff, which gets the value of byte 4
        byte[] ret = new byte[len];
        for (int b = len-1; b >= 0; b--) {
          ret[b] = (byte)(i & 0xff);  // get the least significant byte
          i >>= 8;  // shift left by 1 byte
        }
        return ret;
      }
    }
    // all of the 3 leftmost bytes were empty
    throw new IllegalArgumentException("The path " + Integer.toHexString(i) + " is not valid");
  }

  /**
   * Convert a signed byte (-128..127) to an "unsigned byte" (0..256) int
   * value which can be used to index into an array holding the children
   * of a tree node. 
   */
  static int byteValueToIndex(byte b) {
    // the bytes 0..127 correspond to their respective integers
    if (b >= 0)
      return (int)b;
    // however, bytes -128..0 correspond to integers 128..255
    return 256 + b;
  }

  /** The inverse of byteValueToIndex */
  static byte indexToByteValue(int i) {
    return (byte)i;
  }



  // tree printing methods (for debugging)

  String dumpTree() {
    return dumpSubtree(level1, 1, new StringBuffer()).toString();
  }

  private StringBuffer dumpSubtree(Object[] nodes, int level, StringBuffer buffer) {
    // descend into the subrees on left half of this level
    for (int i = 0; i < FIRST_LEAF_SLOT; i++) {
      if (nodes[i] == null)
        break; // no more non-null subtree nodes at this level
      else {
        dumpSubtree((Object[])nodes[i], level+1, dumpIndex(buffer.append("\n"), level, i).append("("));  // move the caret down a line and indent new level with " ("
        buffer.append(")");
      }
    }
    // print the leaf nodes in this level
    for (int i = FIRST_LEAF_SLOT; i <= LAST_LEAF_SLOT; i++) {
      if (nodes[i] == null)
        break; // no more non-null leaf nodes at this level
      else
        dumpIndex(buffer.append("\n"), level, i).append(nodes[i]);
    }
    return buffer;
  }

  private StringBuffer dumpIndex(StringBuffer buffer, int level, int i) {
    return indent(buffer, level).append("[").append(i).append("] ");
  }

  private StringBuffer indent(StringBuffer buffer, int level) {
    char[] indentation = new char[level - 1];
    Arrays.fill(indentation, ' ');
    return buffer.append(indentation);
  }

}
