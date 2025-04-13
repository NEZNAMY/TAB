package me.neznamy.tab.shared.util.function;

/**
 * An interface for Consumer with 3 arguments.
 * @param   <A>
 *          First function argument
 * @param   <B>
 *          Second function argument
 * @param   <C>
 *          Third function argument
 */
@FunctionalInterface
public interface TriConsumer<A, B, C> {

    /**
     * Runs the function.
     *
     * @param   a
     *          First argument
     * @param   b
     *          Second argument
     * @param   c
     *          Third argument
     */
    void accept(A a, B b, C c);
}
