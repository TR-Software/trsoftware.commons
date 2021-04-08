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

/**
 * A state in a Markov chain is a set of tokens from the natural language being modeled
 * and a probabilistic transition table for the possible next tokens.
 * <p>
 * The various implementations of State support various ways of representing language tokens:
 * {@link solutions.trsoftware.commons.shared.text.markovchain.state.StringState} subclasses support representing
 * tokens as {@link java.lang.String}s and the {@link solutions.trsoftware.commons.shared.text.markovchain.state.ShortState}
 * subclasses support {@code short} integers as tokens.  The latter is optimized for minimal memory usage when
 * representing large {@link solutions.trsoftware.commons.shared.text.markovchain.MarkovChain}s.
 * <p>
 * Note: the benefits of supporting string representations didn't prove to be that
 * great (see doc for the {@link solutions.trsoftware.commons.shared.text.markovchain.dict} package)
 * Therefore, in the future, can delete everything except
 * {@link solutions.trsoftware.commons.shared.text.markovchain.state.NgramStringState}.
 * @see solutions.trsoftware.commons.shared.text.markovchain.MarkovChain
 */
package solutions.trsoftware.commons.shared.text.markovchain.state;



