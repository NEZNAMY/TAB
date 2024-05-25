package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.NonNull;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * TabList handler for 1.19.3+ players using the new tab list packets.
 * Because BungeeCord does not have a TabList API, we need to use packets.
 */
public class BungeeTabList1193 extends BungeeTabList {

    /** Map of actions to prevent creating new EnumSet on each packet send */
    private static final Map<Action, EnumSet<PlayerListItemUpdate.Action>> actions = new EnumMap<>(Action.class);

    static {
        actions.put(Action.ADD_PLAYER, EnumSet.allOf(PlayerListItemUpdate.Action.class));
        actions.put(Action.UPDATE_GAME_MODE, EnumSet.of(PlayerListItemUpdate.Action.UPDATE_GAMEMODE));
        actions.put(Action.UPDATE_DISPLAY_NAME, EnumSet.of(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME));
        actions.put(Action.UPDATE_LATENCY, EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LATENCY));
        actions.put(Action.UPDATE_LISTED, EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LISTED));
    }

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
    public void removeEntry0(@NonNull UUID entry) {
        removeUuid(entry);
        PlayerListItemRemove remove = new PlayerListItemRemove();
        remove.setUuids(new UUID[]{entry});
        player.sendPacket(remove);
    }

    @Override
    public void updateDisplayName(@NonNull UUID entry, @Nullable BaseComponent displayName) {
        Item item = item(entry);
        item.setDisplayName(displayName);
        sendPacket(Action.UPDATE_DISPLAY_NAME, item);
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        Item item = item(entry);
        item.setPing(latency);
        sendPacket(Action.UPDATE_LATENCY, item);
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        Item item = item(entry);
        item.setGamemode(gameMode);
        sendPacket(Action.UPDATE_GAME_MODE, item);
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        Item item = item(entry);
        item.setListed(listed);
        sendPacket(Action.UPDATE_LISTED, item);
    }

    @Override
    public void addEntry(@NonNull UUID id, @NonNull String name, @Nullable Skin skin, boolean listed, int latency, int gameMode, @Nullable BaseComponent displayName) {
        addUuid(id);
        sendPacket(Action.ADD_PLAYER, entryToItem(id, name, skin, listed, latency, gameMode, displayName));
    }

    private void sendPacket(@NonNull Action action, @NonNull Item item) {
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        packet.setActions(actions.get(action));
        packet.setItems(new Item[]{item});
        player.sendPacket(packet);
    }
}
