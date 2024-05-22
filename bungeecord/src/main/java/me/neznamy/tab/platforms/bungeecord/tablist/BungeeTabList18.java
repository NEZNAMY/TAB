package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.NonNull;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TabList handler for players between 1.8 and 1.19.2.<p>
 * Because BungeeCord does not have a TabList API, we need to use packets.
 */
public class BungeeTabList18 extends BungeeTabList {

    /**
     * Constructs new instance with given parameter.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public BungeeTabList18(@NonNull BungeeTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        removeUuid(entry);
        sendPacket(PlayerListItem.Action.REMOVE_PLAYER, item(entry));
    }

    @Override
    public void updateDisplayName(@NonNull UUID entry, @Nullable BaseComponent displayName) {
        Item item = item(entry);
        item.setDisplayName(displayName);
        sendPacket(PlayerListItem.Action.UPDATE_DISPLAY_NAME, item);
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        Item item = item(entry);
        item.setPing(latency);
        sendPacket(PlayerListItem.Action.UPDATE_LATENCY, item);
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        Item item = item(entry);
        item.setGamemode(gameMode);
        sendPacket(PlayerListItem.Action.UPDATE_GAMEMODE, item);
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        // Added in 1.19.3
    }

    @Override
    public void addEntry(@NonNull UUID id, @NonNull String name, @Nullable Skin skin, boolean listed, int latency, int gameMode, @Nullable BaseComponent displayName) {
        addUuid(id);
        sendPacket(PlayerListItem.Action.ADD_PLAYER, entryToItem(id, name, skin, listed, latency, gameMode, displayName));
    }

    private void sendPacket(@NonNull PlayerListItem.Action action, @NonNull Item item) {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(action);
        packet.setItems(new Item[]{item});
        player.sendPacket(packet);
    }
}
