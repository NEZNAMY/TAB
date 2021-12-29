package me.neznamy.tab.api.event;

/**
 * The event bus. This is used to register event listeners/handlers for
 * handling events that may be fired by TAB.
 */
public interface EventBus {

    /**
     * Scans the given listener for any method that is annotated with
     * {@link Subscribe} and registers it as a listener to this event bus.
     *
     * All methods in this class that are annotated with {@link Subscribe}
     * must meet the following criteria:
     * <ul>
     *     <li>It must be <strong>public</strong>.</li>
     *     <li>It must <strong>not</strong> be abstract.</li>
     *     <li>It must have exactly <strong>one</strong> parameter.</li>
     *     <li>Its single parameter must be <strong>an event</strong>.</li>
     * </ul>
     * If a method does not meet the above criteria, an exception <strong>will</strong>
     * be thrown.
     *
     * @param listener the listener to register
     * @throws RuntimeException if a method does not meet the criteria
     */
    void register(Object listener);

    /**
     * Registers the given handler to this event bus.
     *
     * @param type the class type of the event
     * @param handler the handler to register
     * @param <E> the type of the event
     */
    <E extends TabEvent> void register(Class<E> type, EventHandler<E> handler);

    /**
     * Scans the given listener for any method that is annotated with
     * {@link Subscribe} and unregisters it from this event bus.
     *
     * @param listener the listener to unregister
     */
    void unregister(Object listener);

    /**
     * Unregisters the given handler from this event bus.
     *
     * @param handler the handler to unregister
     * @param <E> the type of the event
     */
    <E extends TabEvent> void unregister(EventHandler<E> handler);
}
