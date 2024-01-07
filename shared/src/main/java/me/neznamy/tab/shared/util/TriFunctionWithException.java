package me.neznamy.tab.shared.util;

/**
 * An interface for BiFunction which can throw an exception.
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
public interface TriFunctionWithException<A, B, C, D> {

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
     * @throws  ReflectiveOperationException
     *          If reflective operation fails
     */
    D apply(A a, B b, C c) throws ReflectiveOperationException;
}
