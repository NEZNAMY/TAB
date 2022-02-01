package me.neznamy.tab.shared.event;

import java.lang.reflect.Method;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.event.EventHandler;
import me.neznamy.tab.api.event.Subscribe;
import me.neznamy.tab.api.event.TabEvent;
import me.neznamy.tab.shared.TAB;
import net.kyori.event.EventSubscriber;
import net.kyori.event.PostOrders;
import net.kyori.event.PostResult;
import net.kyori.event.SimpleEventBus;
import net.kyori.event.method.MethodHandleEventExecutorFactory;
import net.kyori.event.method.MethodScanner;
import net.kyori.event.method.MethodSubscriptionAdapter;
import net.kyori.event.method.SimpleMethodSubscriptionAdapter;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class EventBusImpl implements EventBus {

    private final SimpleEventBus<TabEvent> bus;
    private final MethodSubscriptionAdapter<Object> methodAdapter;

    public EventBusImpl() {
        this.bus = new SimpleEventBus<TabEvent>(TabEvent.class) {

            @Override
            protected boolean shouldPost(@NonNull TabEvent event, @NonNull EventSubscriber subscriber) {
                return true;
            }
        };
        this.methodAdapter = new SimpleMethodSubscriptionAdapter<>(bus, new MethodHandleEventExecutorFactory<>(), new TabMethodScanner());
    }

    public <E extends TabEvent> void fire(final E event) {
        if (!bus.hasSubscribers(event.getClass())) return;
        final PostResult result = bus.post(event);
        if (result.exceptions().isEmpty()) return;

        TAB.getInstance().getErrorManager().printError("Some errors occurred whilst trying to fire event " + event);
        int i = 0;
        for (final Throwable exception : result.exceptions().values()) {
            TAB.getInstance().getErrorManager().printError("#" + i++ + ": \n", exception);
        }
    }

    @Override
    public void register(Object listener) {
        methodAdapter.register(listener);
    }

    @Override
    public <E extends TabEvent> void register(Class<E> type, EventHandler<E> handler) {
        bus.register(type, new HandlerWrapper<>(handler));
    }

    @Override
    public void unregister(Object listener) {
        methodAdapter.unregister(listener);
    }

    @Override
    public <E extends TabEvent> void unregister(EventHandler<E> handler) {
        bus.unregister(subscriber -> subscriber instanceof HandlerWrapper && ((HandlerWrapper<?>) subscriber).handler == handler);
    }

    private static final class TabMethodScanner implements MethodScanner<Object> {

        @Override
        public boolean shouldRegister(@NonNull Object listener, @NonNull Method method) {
            return method.isAnnotationPresent(Subscribe.class);
        }

        @Override
        public int postOrder(@NonNull Object listener, @NonNull Method method) {
            return PostOrders.NORMAL;
        }

        @Override
        public boolean consumeCancelledEvents(@NonNull Object listener, @NonNull Method method) {
            return true;
        }
    }

    private static final class HandlerWrapper<E> implements EventSubscriber<E> {

        private final EventHandler<E> handler;

        public HandlerWrapper(final EventHandler<E> handler) {
            this.handler = handler;
        }

        @Override
        public void invoke(@NonNull E event) {
            handler.handle(event);
        }
    }
}
