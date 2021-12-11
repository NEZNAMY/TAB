package me.neznamy.tab.api.event;

/**
 * A handler for an event. This allows listening for events in a more
 * functional style.
 *
 * @param <E> the type of event this handler handles
 */
@FunctionalInterface
public interface EventHandler<E> {

    /**
     * Called when the given event is fired from an event bus.
     *
     * @param event the event to handle
     */
    void handle(E event);
}
