package me.neznamy.tab.shared.proxy;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.proxy.message.outgoing.ExpansionPlaceholder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Expansion handler for proxies via bridge.
 */
public class ProxyTabExpansion implements TabExpansion {

    /** Map holding all values for all players for resending on server switch */
    private final Map<TabPlayer, Map<String, String>> values = new WeakHashMap<>();

    @Override
    public void setValue(@NotNull TabPlayer player, @NotNull String key, @NotNull String value) {
        values.computeIfAbsent(player, p -> new HashMap<>()).put(key, value);
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
        for (Map.Entry<String, String> entry : values.computeIfAbsent(player, p -> new HashMap<>()).entrySet()) {
            player.sendPluginMessage(new ExpansionPlaceholder(entry.getKey(), entry.getValue()));
        }
    }
}
