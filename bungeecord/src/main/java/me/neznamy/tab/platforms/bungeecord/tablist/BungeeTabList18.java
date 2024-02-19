package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.NonNull;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.chat.TabComponent;
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
     * Constructs new instance with given parameters.
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
    public void updateDisplayName(@NonNull UUID entry, @Nullable TabComponent displayName) {
        Item item = item(entry);
        if (displayName != null) item.setDisplayName(player.getPlatform().toComponent(displayName, player.getVersion()));
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
    public void addEntry(@NonNull Entry entry) {
        addUuid(entry.getUniqueId());
        sendPacket(PlayerListItem.Action.ADD_PLAYER, entryToItem(entry));

        if (player.getVersion().getMinorVersion() == 8) {
            // Compensation for 1.8.0 client sided bug
            updateDisplayName(entry.getUniqueId(), entry.getDisplayName());
        }
    }

    private void sendPacket(@NonNull PlayerListItem.Action action, @NonNull Item item) {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(action);
        packet.setItems(new Item[]{item});
        player.sendPacket(packet);
    }
}
