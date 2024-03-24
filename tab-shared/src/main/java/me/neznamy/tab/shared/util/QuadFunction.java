package me.neznamy.tab.shared.util;

/**
 * Interface for 4-argument functions.
 *
 * @param   <T>
 *          First argument type
 * @param   <U>
 *          Second argument type
 * @param   <V>
 *          Third argument type
 * @param   <W>
 *          Fourth argument type
 * @param   <R>
 *          Return type
 */
@FunctionalInterface
public interface QuadFunction<T, U, V, W, R> {

    /**
     * Runs the function.
     *
     * @param   t
     *          First argument
     * @param   u
     *          Second argument
     * @param   v
     *          Third argument
     * @param   w
     *          Fourth argument
     * @return  Function result
     * @throws  Exception
     *          If thrown by function
     */
    R apply(T t, U u, V v, W w) throws Exception;
}