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

package solutions.trsoftware.commons.shared.text.markovchain.state;

import solutions.trsoftware.commons.shared.text.markovchain.ShortTransitionTable;
import solutions.trsoftware.commons.shared.text.markovchain.TransitionTable;

/**
 * A state in a Markov chain that uses Shorts to represent words.
 *
 * Oct 20, 2009
 *
 * @author Alex
 */
abstract class ShortState extends State<Short> {

  protected TransitionTable<Short> createTransitionTable(Short initalValue) {
    return new ShortTransitionTable(initalValue);
  }

}
