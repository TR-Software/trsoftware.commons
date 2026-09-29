package solutions.trsoftware.commons.shared.util.function;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

/**
 * Represents an operation that accepts two {@code int}-valued arguments and returns no
 * result.  This is the primitive type specialization of {@link BiConsumer} for {@code int}.
 *
 * @see BiConsumer
 * @see IntConsumer
 * @see IntBiFunction
 * @since 9/16/2026
 */
@FunctionalInterface
public interface IntBiConsumer {

    /**
     * Performs this operation on the given arguments.
     *
     * @param i the first input argument
     * @param j the second input argument
     */
    void accept(int i, int j);

    /**
     * Returns a composed {@link IntBiConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@link IntBiConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default IntBiConsumer andThen(IntBiConsumer after) {
        Objects.requireNonNull(after);
        return (l, r) -> {
            accept(l, r);
            after.accept(l, r);
        };
    }
}
