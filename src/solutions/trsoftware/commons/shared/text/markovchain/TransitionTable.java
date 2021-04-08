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

package solutions.trsoftware.commons.shared.text.markovchain;

import solutions.trsoftware.commons.shared.util.JsonBuilder;

import java.util.*;

/**
 * Transition table giving the possible transitions from a state of n=order words
 * to the next word.
 */
public abstract class TransitionTable<V> {
  /**
   * The possible values are either a String (if that's the only possible transition), or
   * a Map<String,Integer> giving the probabilities (as counts) of the possible transitions.
   * This is done to save memory, because most of the time there will only be one value,
   * in which case a map object is wasteful.
   */
  private Object value;

  /**
   * Subclasses should override this method depending on the desired representation of a word,
   * which could be a String, Short, byte[], or whatever.
   *
   * It's more space-efficient to represent this as an abstract method instead
   * of an instance field (assuming this class will have many instances). 
   *
   * @return
   */
  protected abstract Class<V> getValueClass();

  /** Initialize to having 100% transition probability to the given word */
  public TransitionTable(V initialWord) {
    value = initialWord;
  }

  /** Adds another transition to this table */
  public void add(V next) {
    // get or create the transition map
    Map<V, Number> map = asMap();
    value = map;

    // now add this new mapping
    if (map.containsKey(next))
      map.put(next, wrap(map.get(next).intValue() + 1));
    else
      map.put(next, 1);
  }

  /**
   * A factory method which creates a new instance of the smallest
   * wrapper class that can hold the given integer.
   * @return An instance of either Byte, Short, or Integer.
   */
  private Number wrap(int number) {
    // in all cases we call valueOf to return a flyweight instance instead of a new object
    if (Byte.MIN_VALUE <= number && number <= Byte.MAX_VALUE)
      return Byte.valueOf((byte)number);
    else if (Short.MIN_VALUE <= number && number <= Short.MAX_VALUE)
      return Short.valueOf((short)number);
    return Integer.valueOf(number);

  }

  public Map<V, Number> asMap() {
    Map<V, Number> map;
    if (isValueRaw()) {
      // it's time to upgrade to a map (initialized to the pair (oldValue, 1))
      map = new HashMap<V, Number>();
      map.put((V)value, wrap(1));
    }
    else {
      map = (Map<V, Number>)value;
    }
    return map;
  }

  /**
   * The transitions object is either a V, if that's the only possible transition
   * or a V->Integer map giving the weights for each transition.
   *
   * @param rnd A pseudorandom generator that will influence the pick if there's
   * more than one choice.  Either way, the random generator will be advanced by one
   * as a result of this method call.
   * @return The next word chosen at random based on the transition probabilities.
   */
  public V chooseTransition(Random rnd) {
    return chooseRandomToken(asMap().entrySet(), rnd);
  }

  /**
   * @return true if the value field contains the raw value and false if
   * it contains a transition map.
   */
  private boolean isValueRaw() {
    return value.getClass().equals(getValueClass());
  }

  /** Chooses a random value from the entry set with the entry values as weights */
  private V chooseRandomToken(Set<Map.Entry<V, Number>> weightedTokens, Random rnd) {
    ArrayList<V> options = new ArrayList<V>(weightedTokens.size() * 4);
      // insert each token into the options array the # of times proportional to its weight
    for (Map.Entry<V, Number> weightedToken : weightedTokens) {
      V token = weightedToken.getKey();
      int weight = weightedToken.getValue().intValue();
      for (int i = 0; i < weight; i++) {
        options.add(token);
      }
    }
    return options.get(rnd.nextInt(options.size()));
  }

  public int size() {
    if (isValueRaw())
      return 1;
    else
      return ((Map)value).size();
  }

  @Override
  public String toString() {
    if (isValueRaw())
      return (String)value;
    else
      return JsonBuilder.mapToJson((Map)value);
  }
}
