package me.neznamy.tab.shared.util;

/**
 * An interface for BiFunction which can throw an exception.
 * @param   <A>
 *          First function argument
 * @param   <B>
 *          Second function argument
 * @param   <C>
 *          Function return type
 */
@FunctionalInterface
public interface BiFunctionWithException<A, B, C> {

    /**
     * Runs the function and returns the result
     *
     * @param   a
     *          First argument
     * @param   b
     *          Second argument
     * @return  Output from function
     * @throws  Exception
     *          If operation fails
     */
    C apply(A a, B b) throws Exception;
}
