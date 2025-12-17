package me.neznamy.tab.shared.util.function;

/**
 * An interface for a function with 4 input arguments.
 * @param   <A>
 *          First function argument
 * @param   <B>
 *          Second function argument
 * @param   <C>
 *          Third function argument
 * @param   <D>
 *          Fourth function argument
 * @param   <E>
 *          Function return type
 */
@FunctionalInterface
public interface QuadFunction<A, B, C, D, E> {

    /**
     * Runs the function and returns the result
     *
     * @param   a
     *          First argument
     * @param   b
     *          Second argument
     * @param   c
     *          Third argument
     * @param   d
     *          Fourth argument
     * @return  Output from function
     */
    E apply(A a, B b, C c, D d);
}
