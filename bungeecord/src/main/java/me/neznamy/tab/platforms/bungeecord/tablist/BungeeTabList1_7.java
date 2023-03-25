package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.TabListEntry;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.tablist.SingleUpdateTabList;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class BungeeTabList1_7 extends SingleUpdateTabList {

    /** Player this TabList belongs to */
    private final BungeeTabPlayer player;

    /** Because entries are identified by names and not uuids on 1.7 */
    private final Map<UUID, String> userNames = new HashMap<>();
    private final Map<UUID, String> displayNames = new HashMap<>();

    @Override
    public void removeEntry(@NonNull UUID entry) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setDisplayName(displayNames.get(entry));
        item.setPing(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        ((UserConnection)player.getPlayer()).getTabListHandler().onUpdate(packet);

        // Remove from map
        userNames.remove(entry);
        displayNames.remove(entry);
    }

    @Override
    public void updateDisplayName(@NonNull UUID entry, @Nullable IChatBaseComponent displayName) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB

        // Remove old entry
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setDisplayName(displayNames.get(entry));
        item.setPing(0); // Avoid NPE
        packet.setItems(new PlayerListItem.Item[]{item});
        ((UserConnection)player.getPlayer()).getTabListHandler().onUpdate(packet);

        // Add new entry
        packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        item = new PlayerListItem.Item();
        item.setDisplayName(displayName == null ? userNames.get(entry) : displayName.toLegacyText());
        if (item.getDisplayName().length() > 16) item.setDisplayName(item.getDisplayName().substring(0, 16)); // 16 character limit
        item.setPing(0); // Avoid NPE
        item.setUsername(userNames.get(entry));
        packet.setItems(new PlayerListItem.Item[]{item});
        ((UserConnection)player.getPlayer()).getTabListHandler().onUpdate(packet);

        // Update in map
        displayNames.put(entry, item.getDisplayName());
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        if (!displayNames.containsKey(entry)) return; // Entry not tracked by TAB

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.UPDATE_LATENCY);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setDisplayName(displayNames.get(entry));
        item.setPing(latency);
        packet.setItems(new PlayerListItem.Item[]{item});
        ((UserConnection)player.getPlayer()).getTabListHandler().onUpdate(packet);
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {} // Added in 1.8

    @Override
    public void addEntry(@NonNull TabListEntry entry) {
        PlayerListItem.Item item = new PlayerListItem.Item();
        if (entry.getDisplayName() != null) {
            item.setDisplayName(entry.getDisplayName().toLegacyText());
            if (item.getDisplayName().length() > 16) item.setDisplayName(item.getDisplayName().substring(0, 16)); // 16 character limit
        } else {
            item.setDisplayName(entry.getName());
        }
        item.setUsername(entry.getName());
        item.setPing(entry.getLatency());
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        packet.setItems(new PlayerListItem.Item[]{item});
        ((UserConnection)player.getPlayer()).getTabListHandler().onUpdate(packet);

        // Add to map
        userNames.put(entry.getUniqueId(), entry.getName());
        displayNames.put(entry.getUniqueId(), item.getDisplayName());
    }
}
