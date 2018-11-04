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



