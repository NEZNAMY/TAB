package me.neznamy.tab.api.event;

@FunctionalInterface
public interface EventHandler<E> {

    void handle(E event);
}
