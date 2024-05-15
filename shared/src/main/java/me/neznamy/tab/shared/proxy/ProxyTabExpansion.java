package me.neznamy.tab.shared.proxy;

import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.message.outgoing.ExpansionPlaceholder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Expansion handler for proxies via bridge.
 */
public class ProxyTabExpansion implements TabExpansion {

    @Override
    public void setValue(@NotNull TabPlayer player, @NotNull String key, @NotNull String value) {
        player.expansionValues.put(key, value);
        ((ProxyTabPlayer)player).sendPluginMessage(new ExpansionPlaceholder(key, value));
    }

    @Override
    public void unregisterExpansion() {
        // Don't do anything on proxy side
    }

    /**
     * Resends all values to the player, typically on server switch.
     *
     * @param   player
     *          Player to resend all values to
     */
    public void resendAllValues(@NotNull ProxyTabPlayer player) {
        for (Map.Entry<String, String> entry : player.expansionValues.entrySet()) {
            player.sendPluginMessage(new ExpansionPlaceholder(entry.getKey(), entry.getValue()));
        }
    }
}
