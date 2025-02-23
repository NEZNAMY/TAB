package me.neznamy.tab.shared.util.function;

/**
 * An interface for Consumer which can throw an exception.
 * @param   <A>
 *          Consumer argument
 */
@FunctionalInterface
public interface ConsumerWithException<A> {

    /**
     * Runs the function.
     *
     * @param   a
     *          Consumer argument
     * @throws  Exception
     *          If thrown by function
     */
    void accept(A a) throws Exception;
}
