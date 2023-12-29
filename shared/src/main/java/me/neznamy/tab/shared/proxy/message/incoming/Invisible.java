package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

public class Invisible implements IncomingMessage {

    private boolean invisible;

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        invisible = in.readBoolean();
    }

    @Override
    public void process(@NotNull ProxyTabPlayer player) {
        player.setInvisibilityPotion(invisible);
    }
}
