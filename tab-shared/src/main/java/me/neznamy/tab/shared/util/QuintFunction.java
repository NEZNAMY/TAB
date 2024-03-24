package me.neznamy.tab.shared.util;

import java.lang.reflect.InvocationTargetException;

/**
 * Interface for 5-argument functions.
 *
 * @param   <T>
 *          First argument type
 * @param   <U>
 *          Second argument type
 * @param   <V>
 *          Third argument type
 * @param   <W>
 *          Fourth argument type
 * @param   <X>
 *          Fifth argument type
 * @param   <R>
 *          Return type

 */
@FunctionalInterface
public interface QuintFunction<T, U, V, W, X, R> {

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
     * @param   x
     *          Fifth argument
     * @return  Function result
     */
    R apply(T t, U u, V v, W w, X x) throws InvocationTargetException, InstantiationException, IllegalAccessException;
}