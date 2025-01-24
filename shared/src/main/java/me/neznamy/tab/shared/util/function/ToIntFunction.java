package me.neznamy.tab.shared.util.function;

/**
 * Represents a function that accepts an int-valued argument and produces a
 * result.
 * @param   <T>
 *          the type of the argument of the function
 */
@FunctionalInterface
public interface ToIntFunction<T> {

    /**
     * Applies this function to the given argument.
     *
     * @param   value
     *          the function argument
     * @return  the function result
     * @throws  Exception
     *          If thrown by operation
     */
    int apply(T value) throws Exception;
}
