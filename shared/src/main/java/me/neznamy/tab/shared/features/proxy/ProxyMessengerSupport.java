package me.neznamy.tab.shared.features.proxy;

import com.saicone.delivery4j.AbstractMessenger;
import com.saicone.delivery4j.Broker;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.TabTextColor;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class ProxyMessengerSupport extends ProxySupport {

    @NotNull
    private final String messengerName;

    @NotNull
    private final Supplier<Broker> brokerSupplier;

    @Nullable
    private AbstractMessenger messenger;

    @Override
    public void sendMessage(@NotNull String message) {
        if (messenger == null || !messenger.isEnabled()) return;
        messenger.send(TabConstants.PROXY_CHANNEL_NAME, message);
    }

    @Override
    public void register() {
        try {
            Broker broker = brokerSupplier.get();
            messenger = new AbstractMessenger() {

                @Override
                @NotNull
                protected Broker loadBroker() {
                    return broker;
                }
            };
            messenger.subscribe(TabConstants.PROXY_CHANNEL_NAME).consume((channel, lines) -> processMessage(lines[0])).cache(true);
            messenger.start();
            TAB.getInstance().getPlatform().logInfo(new TabTextComponent("Successfully connected to " + messengerName, TabTextColor.GREEN));
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().criticalError("Failed to connect to " + messengerName + ": " + e.getClass().getName() + ": " + e.getMessage(), null);
        }
    }

    @Override
    public void unregister() {
        if (messenger == null) return;
        messenger.close();
        messenger.clear();
    }
}
