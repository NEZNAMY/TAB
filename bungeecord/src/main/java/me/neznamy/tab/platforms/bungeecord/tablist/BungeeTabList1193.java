package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.NonNull;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

/**
 * TabList handler for 1.19.3+ players using the new tab list packets.
 * Because BungeeCord does not have a TabList API, we need to use packets.
 */
public class BungeeTabList1193 extends BungeeTabList {

    /**
     * Constructs new instance with given parameter.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public BungeeTabList1193(@NonNull BungeeTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        removeUuid(entry);
        PlayerListItemRemove remove = new PlayerListItemRemove();
        remove.setUuids(new UUID[]{entry});
        player.sendPacket(remove);
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable BaseComponent displayName) {
        Item item = item(entry);
        item.setDisplayName(displayName);
        sendPacket(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME), item);
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        Item item = item(entry);
        item.setPing(latency);
        sendPacket(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LATENCY), item);
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        Item item = item(entry);
        item.setGamemode(gameMode);
        sendPacket(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_GAMEMODE), item);
    }

    @Override
    public void addEntry0(@NonNull UUID id, @NonNull String name, @Nullable Skin skin, int latency, int gameMode, @Nullable BaseComponent displayName) {
        addUuid(id);
        sendPacket(EnumSet.allOf(PlayerListItemUpdate.Action.class), entryToItem(id, name, skin, latency, gameMode, displayName));
    }

    private void sendPacket(@NonNull EnumSet<PlayerListItemUpdate.Action> actions, @NonNull Item item) {
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        packet.setActions(actions);
        packet.setItems(new Item[]{item});
        player.sendPacket(packet);
    }
}
