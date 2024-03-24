package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

public interface IncomingMessage {

    void read(@NotNull ByteArrayDataInput in);

    void process(@NotNull ProxyTabPlayer player);
}
