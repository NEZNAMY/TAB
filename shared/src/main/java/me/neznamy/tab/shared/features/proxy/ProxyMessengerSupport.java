package me.neznamy.tab.shared.features.proxy;

import com.saicone.delivery4j.AbstractMessenger;
import com.saicone.delivery4j.Broker;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class ProxyMessengerSupport extends ProxySupport {

    private final Supplier<Broker> brokerSupplier;
    private AbstractMessenger messenger;

    @Override
    public void sendMessage(@NotNull String message) {
        messenger.send(TabConstants.PROXY_CHANNEL_NAME, message);
    }

    @Override
    public void register() {
        messenger = new AbstractMessenger() {
            @Override
            protected @NotNull Broker loadBroker() {
                return brokerSupplier.get();
            }
        };
        messenger.subscribe(TabConstants.PROXY_CHANNEL_NAME).consume((channel, lines) -> {
            processMessage(lines[0]);
        }).cache(true);
        messenger.start();
    }

    @Override
    public void unregister() {
        messenger.close();
        messenger.clear();
    }
}
