package me.neznamy.tab.shared.proxy.message.incoming;

import com.google.common.io.ByteArrayDataInput;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PlayerJoinResponse implements IncomingMessage {

    private String world;
    private String group;
    private Map<String, Object> placeholders;
    private int gameMode;

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        world = in.readUTF();
        if (TAB.getInstance().getGroupManager().getPermissionPlugin().contains("Vault") &&
                !TAB.getInstance().getGroupManager().isGroupsByPermissions()) group = in.readUTF();
        placeholders = new HashMap<>();
        int placeholderCount = in.readInt();
        for (int i=0; i<placeholderCount; i++) {
            String identifier = in.readUTF();
            if (identifier.startsWith("%rel_")) {
                Map<String, String> map = new HashMap<>();
                int playerCount = in.readInt();
                for (int j=0; j<playerCount; j++) {
                    String otherPlayer = in.readUTF();
                    String value = in.readUTF();
                    map.put(otherPlayer, value);
                }
                placeholders.put(identifier, map);
            } else {
                placeholders.put(identifier, in.readUTF());
            }
        }
        gameMode = in.readInt();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(@NotNull ProxyTabPlayer player) {
        TAB.getInstance().debug("Bridge took " + (System.currentTimeMillis()-player.getBridgeRequestTime()) + "ms to respond to join message of " + player.getName());
        TAB.getInstance().getFeatureManager().onWorldChange(player.getUniqueId(), world);
        if (group != null) player.setGroup(group);
        // reset attributes from previous server to default false values, new server will send separate update packets if needed
        if (player.vanished) { // Only trigger if bridge says player is vanished, do not trigger on proxy vanish
            player.vanished = false;
            TAB.getInstance().getFeatureManager().onVanishStatusChange(player);
        }
        player.setDisguised(false);
        player.setInvisibilityPotion(false);
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            String identifier = entry.getKey();
            if (!TAB.getInstance().getPlaceholderManager().isPlaceholderRegistered(identifier)) continue;
            if (identifier.startsWith("%rel_")) {
                Map<String, String> map = (Map<String, String>) entry.getValue();
                for (Map.Entry<String, String> entry2 : map.entrySet()) {
                    TabPlayer other = TAB.getInstance().getPlayer(entry2.getKey());
                    if (other != null) { // Backend player did not connect via this proxy if null
                        ((RelationalPlaceholder)TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier))
                                .updateValue(player, other, entry2.getValue());
                    }
                }
            } else {
                Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(identifier);
                if (pl instanceof PlayerPlaceholder) {
                    ((PlayerPlaceholder) pl).updateValue(player, entry.getValue());
                } else {
                    ((ServerPlaceholder) pl).updateValue(entry.getValue());
                }
            }
        }
        player.setGamemode(gameMode);
        player.setBridgeConnected(true);
    }
}
