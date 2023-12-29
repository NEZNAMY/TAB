package me.neznamy.tab.platforms.bungeecord.tablist;

import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TabList handler for 1.7 players, which has a completely different
 * system. Entries are identified by display name, not uuids. Display
 * names are changed by removing old entry and adding a new one, which
 * requires further tracking than just the UUID.<p>
 * Because BungeeCord does not have a TabList API, we need to use packets.
 * While BungeeCord itself does not support 1.7, some of its forks do.
 * This was tested on FlameCord fork.
 */
public class BungeeTabList17 extends BungeeTabList {

    /** Because entries are identified by names and not uuids on 1.7 */
    @NotNull
    private final Map<UUID, String> userNames = new HashMap<>();

    @NotNull
    private final Map<UUID, String> displayNames = new HashMap<>();

    /**
     * Constructs new instance with given parameters.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public BungeeTabList17(@NotNull BungeeTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NotNull UUID entry) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        removeUuid(entry);
        update(PlayerListItem.Action.REMOVE_PLAYER, createItem(null, displayNames.get(entry), 0));

        // Remove from map
        userNames.remove(entry);
        displayNames.remove(entry);
    }

    @Override
    public void updateDisplayName(@NotNull UUID entry, @Nullable IChatBaseComponent displayName) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        update(PlayerListItem.Action.REMOVE_PLAYER, createItem(null, displayNames.get(entry), 0));
        addEntry(new Entry(entry, userNames.get(entry), null, 0, 0, displayName));
    }

    @Override
    public void updateLatency(@NotNull UUID entry, int latency) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        update(PlayerListItem.Action.UPDATE_LATENCY, createItem(null, displayNames.get(entry), latency));
    }

    @Override
    public void updateGameMode(@NotNull UUID entry, int gameMode) {/*Added in 1.8*/}

    @Override
    public void addEntry(@NotNull Entry entry) {
        addUuid(entry.getUniqueId());
        String displayNameString = entry.getDisplayName() == null ? entry.getName() : entry.getDisplayName().toLegacyText();
        if (displayNameString.length() > Limitations.MAX_DISPLAY_NAME_LENGTH_1_7)
            displayNameString = displayNameString.substring(0, Limitations.MAX_DISPLAY_NAME_LENGTH_1_7);
        update(PlayerListItem.Action.ADD_PLAYER, createItem(entry.getName(), displayNameString, entry.getLatency()));

        // Add to map
        userNames.put(entry.getUniqueId(), entry.getName());
        displayNames.put(entry.getUniqueId(), displayNameString);
    }

    @Override
    public void setPlayerListHeaderFooter(@NotNull IChatBaseComponent header, @NotNull IChatBaseComponent footer) {
        // Not available on 1.7
    }

    private void update(@NotNull PlayerListItem.Action action, @NotNull PlayerListItem.Item item) {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(action);
        packet.setItems(new PlayerListItem.Item[]{item});
        player.sendPacket(packet);
    }

    private PlayerListItem.Item createItem(@Nullable String username, @NotNull String displayName, int latency) {
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(username);
        item.setPing(latency);
        item.setDisplayName(player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(displayName), player.getVersion()));
        return item;
    }
}
