package me.neznamy.tab.api.util;

/**
 * An interface for BiFunction which can throw an exception.
 * Used in {@link me.neznamy.tab.api.protocol.PacketBuilder} to 
 * call build method for packet classes, which may throw an
 * exception due to reflection.
 * @param	<A>
 * 			First function argument
 * @param	<B>
 * 			Second function argument
 * @param	<C>
 * 			Function return type
 */
public interface BiFunctionWithException<A, B, C> {

	C apply(A a, B b) throws ReflectiveOperationException;
}
