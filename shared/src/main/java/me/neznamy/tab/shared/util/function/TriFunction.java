package me.neznamy.tab.shared.util.function;

/**
 * An interface for a function with 3 input arguments.
 * @param   <A>
 *          First function argument
 * @param   <B>
 *          Second function argument
 * @param   <C>
 *          Third function argument
 * @param   <D>
 *          Function return type
 */
@FunctionalInterface
public interface TriFunction<A, B, C, D> {

    /**
     * Runs the function and returns the result
     *
     * @param   a
     *          First argument
     * @param   b
     *          Second argument
     * @param   c
     *          Third argument
     * @return  Output from function
     */
    D apply(A a, B b, C c);
}
