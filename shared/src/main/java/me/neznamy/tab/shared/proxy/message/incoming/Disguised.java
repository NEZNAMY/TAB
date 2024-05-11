package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Plugin message notifying the proxy that player's disguise status changed.
 */
public class Disguised implements IncomingMessage {

    /** Player disguise status */
    private boolean disguised;

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        disguised = in.readBoolean();
    }

    @Override
    public void process(@NotNull ProxyTabPlayer player) {
        player.setDisguised(disguised);
    }
}
