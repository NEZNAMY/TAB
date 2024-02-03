package me.neznamy.tab.platforms.bungeecord.tablist;

import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.chat.TabComponent;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

/**
 * TabList handler for 1.19.3+ players using the new tab list packets.
 * Because BungeeCord does not have a TabList API, we need to use packets.
 */
public class BungeeTabList1193 extends BungeeTabList {

    /**
     * Constructs new instance with given parameters.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public BungeeTabList1193(@NotNull BungeeTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NotNull UUID entry) {
        removeUuid(entry);
        PlayerListItemRemove remove = new PlayerListItemRemove();
        remove.setUuids(new UUID[]{entry});
        player.sendPacket(remove);
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable TabComponent displayName) {
        Item item = item(entry);
        if (displayName != null) item.setDisplayName(player.getPlatform().toComponent(displayName, player.getVersion()));
        sendPacket(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME), item);
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        Item item = item(entry);
        item.setPing(latency);
        sendPacket(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LATENCY), item);
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {
        Item item = item(entry);
        item.setGamemode(gameMode);
        sendPacket(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_GAMEMODE), item);
    }

    @Override
    public void addEntry(@NotNull Entry entry) {
        addUuid(entry.getUniqueId());
        sendPacket(EnumSet.allOf(PlayerListItemUpdate.Action.class), entryToItem(entry));
    }

    private void sendPacket(@NotNull EnumSet<PlayerListItemUpdate.Action> actions, @NotNull Item item) {
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        packet.setActions(actions);
        packet.setItems(new Item[]{item});
        player.sendPacket(packet);
    }
}
