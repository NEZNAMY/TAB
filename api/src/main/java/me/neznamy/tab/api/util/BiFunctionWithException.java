package me.neznamy.tab.api.util;

public interface BiFunctionWithException<A, B, C> {

	C apply(A a, B b) throws Exception;
}
