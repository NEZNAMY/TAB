package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.NonNull;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.chat.component.TabTextComponent;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.Limitations;
import net.md_5.bungee.api.chat.BaseComponent;
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
    private final Map<UUID, TabComponent> displayNames = new HashMap<>();

    /**
     * Constructs new instance with given parameters.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public BungeeTabList17(@NonNull BungeeTabPlayer player) {
        super(player);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        removeUuid(entry);
        update(PlayerListItem.Action.REMOVE_PLAYER, createItem(null, displayNames.get(entry), 0));

        // Remove from map
        userNames.remove(entry);
        displayNames.remove(entry);
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        update(PlayerListItem.Action.REMOVE_PLAYER, createItem(null, displayNames.get(entry), 0));
        addEntry0(new Entry(entry, userNames.get(entry), null, false, 0, 0, displayName, 0, false));
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB
        update(PlayerListItem.Action.UPDATE_LATENCY, createItem(null, displayNames.get(entry), latency));
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        // Added in 1.8
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        // Added in 1.19.3
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        // Added in 1.21.2
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        // Added in 1.21.4
    }

    @Override
    public void addEntry0(@NonNull Entry entry) {
        addUuid(entry.getUniqueId());
        update(PlayerListItem.Action.ADD_PLAYER, createItem(entry.getName(),
                entry.getDisplayName() == null ? new TabTextComponent(entry.getName()) : entry.getDisplayName(), entry.getLatency()));

        // Add to map
        userNames.put(entry.getUniqueId(), entry.getName());
        displayNames.put(entry.getUniqueId(), entry.getDisplayName());
    }

    @Override
    public void setPlayerListHeaderFooter0(@NonNull TabComponent header, @NonNull TabComponent footer) {
        // Not available on 1.7
    }

    @Override
    @NotNull
    public BaseComponent toComponent(@NonNull TabComponent component) {
        String displayNameString = component.toLegacyText();
        if (displayNameString.length() > Limitations.MAX_DISPLAY_NAME_LENGTH_1_7)
            displayNameString = displayNameString.substring(0, Limitations.MAX_DISPLAY_NAME_LENGTH_1_7);
        return player.getPlatform().transformComponent(new TabTextComponent(displayNameString), player.getVersion());
    }

    private void update(@NonNull PlayerListItem.Action action, @NonNull PlayerListItem.Item item) {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(action);
        packet.setItems(new PlayerListItem.Item[]{item});
        player.sendPacket(packet);
    }

    @NotNull
    private PlayerListItem.Item createItem(@Nullable String username, @Nullable TabComponent displayName, int latency) {
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(username);
        item.setPing(latency);
        if (displayName != null) item.setDisplayName(toComponent(displayName));
        return item;
    }
}
