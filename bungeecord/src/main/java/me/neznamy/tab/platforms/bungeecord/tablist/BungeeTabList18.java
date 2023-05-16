package me.neznamy.tab.platforms.bungeecord.tablist;

import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TabList handler for players between 1.8 and 1.19.2.<p>
 * Because BungeeCord does not have a TabList API, we need to use packets.
 * They are sent using an internal BungeeCord method that keeps track of them,
 * so they are removed on server switch to secure parity with Velocity.
 */
public class BungeeTabList18 extends BungeeTabList {

    public BungeeTabList18(BungeeTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NotNull UUID entry) {
        sendPacket(PlayerListItem.Action.REMOVE_PLAYER, item(entry));
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        PlayerListItem.Item item = item(entry);
        item.setDisplayName(displayName == null ? null : displayName.toString(player.getVersion()));
        sendPacket(PlayerListItem.Action.UPDATE_DISPLAY_NAME, item);
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        PlayerListItem.Item item = item(entry);
        item.setPing(latency);
        sendPacket(PlayerListItem.Action.UPDATE_LATENCY, item);
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        PlayerListItem.Item item = item(entry);
        item.setGamemode(gameMode);
        sendPacket(PlayerListItem.Action.UPDATE_GAMEMODE, item);
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        PlayerListItem.Item item = item(entry.getUniqueId());
        if (entry.getDisplayName() != null) item.setDisplayName(entry.getDisplayName().toString(player.getVersion()));
        item.setGamemode(entry.getGameMode());
        item.setPing(entry.getLatency());
        if (entry.getSkin() != null) {
            item.setProperties(new Property[]{new Property(TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature())});
        } else {
            item.setProperties(new Property[0]);
        }
        item.setUsername(entry.getName());
        sendPacket(PlayerListItem.Action.ADD_PLAYER, item);
    }

    private void sendPacket(@NotNull PlayerListItem.Action action, @NotNull PlayerListItem.Item item) {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(action);
        packet.setItems(new PlayerListItem.Item[]{item});
        ((UserConnection)player.getPlayer()).getTabListHandler().onUpdate(packet);
    }
}
