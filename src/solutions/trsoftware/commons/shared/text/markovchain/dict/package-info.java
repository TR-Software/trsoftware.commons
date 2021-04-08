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
 * This package contains various implementations of {@link solutions.trsoftware.commons.shared.text.markovchain.dict.CodingDictionary},
 * which is a way to canonicalize text tokens from their {@link java.lang.String} representation, to enable
 * various degrees of memory savings.
 * <p>
 * Here's a list of the implementations ranked from least memory-efficient to most memory efficient
 * (when used in a MarkovChain trained on a fairly large corpus):
 * <ol>
 *   <li>IdentityCodingDictionary: simply returns the original String instances.</li>
 *   <li>FlyweightCodingDictionary: similar to String.intern</li>
 *   <li>HashArrayCodingDictionary: turns a String into a canonical number representation</li>
 *   <li>ArrayCodingDictionary turns a String into a canonical number representation but doesn't use a hash map to speed up lookups</li>
 * </ol>
 * This experiment turned out to be a relative flop - the space savings don't really justify the complexity of using a CodingDictionary.
 * Storing the {@link java.lang.String} tokens directly in the MarkovChain is almost just as good.  Therefore, this package
 * may be deleted at a later time, unless you want to construct a huge MarkovChain and memory is a big consideration.
 * <p>
 * Here are some experimental results done on 10/26/2009:
 * <pre>
 * Training order 2 MarkovChains on aliceInWonderlandCorpus.txt with various CodingDictionary implementations:
 *   ShortHashArrayCodingDictionary used up 785.000 KB of memory and 20.00 ms avg. time
 *   ShortHashArrayCodingDictionaryUtf8 used up 811.695 KB of memory and 33.33 ms avg. time
 *   ShortArrayCodingDictionary used up 689.766 KB of memory and 269.50 ms avg. time
 *   ShortArrayCodingDictionaryUtf8 used up 719.984 KB of memory and 320.25 ms avg. time
 *   FlyweightCodingDictionary used up 819.102 KB of memory and 18.81 ms avg. time
 *   IdentityCodingDictionary used up 895.125 KB of memory and 15.38 ms avg. time
 * </pre>
 */
package solutions.trsoftware.commons.shared.text.markovchain.dict;

    