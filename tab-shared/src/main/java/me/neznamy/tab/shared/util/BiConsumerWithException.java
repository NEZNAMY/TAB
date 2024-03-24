package me.neznamy.tab.shared.util;

/**
 * An interface for BiConsumer which can throw an exception.
 * @param   <A>
 *          First function argument
 * @param   <B>
 *          Second function argument
 */
@FunctionalInterface
public interface BiConsumerWithException<A, B> {

    /**
     * Runs the function.
     *
     * @param   a
     *          First argument
     * @param   b
     *          Second argument
     * @throws  Exception
     *          If thrown by function
     */
    void accept(A a, B b) throws Exception;
}
