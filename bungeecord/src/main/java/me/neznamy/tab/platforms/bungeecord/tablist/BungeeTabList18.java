package me.neznamy.tab.platforms.bungeecord.tablist;

import me.neznamy.tab.platforms.bungeecord.BungeeMultiVersion;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TabList handler for players between 1.8 and 1.19.2.<p>
 * Because BungeeCord does not have a TabList API, we need to use packets.
 */
public class BungeeTabList18 extends BungeeTabList {

    public BungeeTabList18(@NotNull BungeeTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NotNull UUID entry) {
        removeUuid(entry);
        sendPacket(PlayerListItem.Action.REMOVE_PLAYER, item(entry));
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        Item item = item(entry);
        if (displayName != null) BungeeMultiVersion.setDisplayName(item, displayName, player.getVersion());
        sendPacket(PlayerListItem.Action.UPDATE_DISPLAY_NAME, item);
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        Item item = item(entry);
        BungeeMultiVersion.setPing(item, latency);
        sendPacket(PlayerListItem.Action.UPDATE_LATENCY, item);
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        Item item = item(entry);
        BungeeMultiVersion.setGamemode(item, gameMode);
        sendPacket(PlayerListItem.Action.UPDATE_GAMEMODE, item);
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        addUuid(entry.getUniqueId());
        sendPacket(PlayerListItem.Action.ADD_PLAYER, entryToItem(entry));
    }

    private void sendPacket(@NotNull PlayerListItem.Action action, @NotNull Item item) {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(action);
        packet.setItems(new Item[]{item});
        player.sendPacket(packet);
    }
}
