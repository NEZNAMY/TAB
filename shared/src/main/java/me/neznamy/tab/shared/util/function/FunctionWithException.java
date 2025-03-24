package me.neznamy.tab.shared.util.function;

import org.jetbrains.annotations.NotNull;

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
     * Empty immutable instance.
     */
    FunctionWithException<?, ?> EMPTY = a -> null;

    /**
     * Get an empty function instance that always return null.
     *
     * @return a function with null result.
     * @param <A> Function argument
     * @param <B> Function return type
     */
    @NotNull
    @SuppressWarnings("unchecked")
    static <A, B> FunctionWithException<A, B> empty() {
        return (FunctionWithException<A, B>) EMPTY;
    }

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

    /**
     * Provide a fallback function if current function result into null value.
     *
     * @param fallback the fallback function.
     * @return         a newly generated function with fallback provider.
     * @param <T>      function argument.
     * @param <R>      function return type.
     */
    @SuppressWarnings("unchecked")
    default <T extends A, R extends B> FunctionWithException<T, R> fallback(FunctionWithException<T, R> fallback) {
        if (this == EMPTY) {
            return fallback;
        }
        return a -> {
            final B b = apply(a);
            return b != null ? (R) b : fallback.apply(a);
        };
    }
}
