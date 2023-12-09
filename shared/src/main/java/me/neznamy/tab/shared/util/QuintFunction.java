package me.neznamy.tab.shared.util;

@FunctionalInterface
public interface QuintFunction<T, U, V, W, X, R> {
    R apply(T t, U u, V v, W w, X x);
}