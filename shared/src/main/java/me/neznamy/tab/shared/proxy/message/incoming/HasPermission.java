package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Incoming plugin message notifying proxy about permission status of a player.
 */
public class HasPermission implements IncomingMessage {

    /** Permission node */
    private String permission;

    /** Whether player has the permission or not */
    private boolean value;

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        permission = in.readUTF();
        value = in.readBoolean();
    }

    @Override
    public void process(@NotNull ProxyTabPlayer player) {
        player.setHasPermission(permission, value);
    }
}
