package me.neznamy.tab.shared.event;

import java.lang.reflect.Method;

import lombok.AllArgsConstructor;
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
import org.jetbrains.annotations.NotNull;

public class EventBusImpl implements EventBus {

    private final SimpleEventBus<TabEvent> bus;
    private final MethodSubscriptionAdapter<Object> methodAdapter;

    public EventBusImpl() {
        bus = new SimpleEventBus<TabEvent>(TabEvent.class) {

            @Override
            protected boolean shouldPost(@NotNull TabEvent event, @NotNull EventSubscriber<?> subscriber) {
                return true;
            }
        };
        methodAdapter = new SimpleMethodSubscriptionAdapter<>(bus, new MethodHandleEventExecutorFactory<>(), new TabMethodScanner());
    }

    public <E extends TabEvent> void fire(E event) {
        if (!bus.hasSubscribers(event.getClass())) return;
        PostResult result = bus.post(event);
        if (result.exceptions().isEmpty()) return;

        TAB.getInstance().getErrorManager().printError("Some errors occurred whilst trying to fire event " + event);
        int i = 0;
        for (Throwable exception : result.exceptions().values()) {
            TAB.getInstance().getErrorManager().printError("#" + i++ + ": \n", exception);
        }
    }

    @Override
    public void register(@lombok.NonNull Object listener) {
        methodAdapter.register(listener);
    }

    @Override
    public <E extends TabEvent> void register(@lombok.NonNull Class<E> type, @lombok.NonNull EventHandler<E> handler) {
        bus.register(type, new HandlerWrapper<>(handler));
    }

    @Override
    public void unregister(@lombok.NonNull Object listener) {
        methodAdapter.unregister(listener);
    }

    @Override
    public <E extends TabEvent> void unregister(@lombok.NonNull EventHandler<E> handler) {
        bus.unregister(subscriber -> subscriber instanceof HandlerWrapper && ((HandlerWrapper<?>) subscriber).handler == handler);
    }

    private static class TabMethodScanner implements MethodScanner<Object> {

        @Override
        public boolean shouldRegister(@NotNull Object listener, @NotNull Method method) {
            return method.isAnnotationPresent(Subscribe.class);
        }

        @Override
        public int postOrder(@NotNull Object listener, @NotNull Method method) {
            return PostOrders.NORMAL;
        }

        @Override
        public boolean consumeCancelledEvents(@NotNull Object listener, @NotNull Method method) {
            return true;
        }
    }

    @AllArgsConstructor
    private static class HandlerWrapper<E> implements EventSubscriber<E> {

        private final EventHandler<E> handler;

        @Override
        public void invoke(@NotNull E event) {
            handler.handle(event);
        }
    }
}
