package me.neznamy.tab.shared.util;

/**
 * An interface for Function which can throw an exception.
 * @param   <A>
 *          Function argument
 * @param   <B>
 *          Function return type
 */
@FunctionalInterface
public interface FunctionWithException<A, B> {

    /**
     * Runs the function and returns the result
     *
     * @param   a
     *          Argument
     * @return  Output from function
     * @throws  Exception
     *          If thrown by function
     */
    B apply(A a) throws Exception;
}
