package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.TabListEntry;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.tablist.BulkUpdateTabList;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.protocol.PlayerPublicKey;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.PlayerListItem;

import java.util.*;

/**
 * TabList handler for players between 1.8 and 1.19.2.<p>
 * Because BungeeCord does not have a TabList API, we need to use packets.
 * They are sent using an internal BungeeCord method that keeps track of them,
 * so they are removed on server switch to secure parity with Velocity.
 */
@RequiredArgsConstructor
public class BungeeTabList1_8 extends BulkUpdateTabList {

    /** Player this TabList belongs to */
    private final BungeeTabPlayer player;

    @Override
    public void removeEntries(@NonNull Collection<UUID> entries) {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        List<PlayerListItem.Item> items = new ArrayList<>();
        for (UUID uuid : entries) {
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(uuid);
            items.add(item);
        }
        packet.setItems(items.toArray(new PlayerListItem.Item[0]));
        ((UserConnection)player.getPlayer()).getTabListHandler().onUpdate(packet);
    }

    @Override
    public void updateDisplayNames(@NonNull Map<UUID, IChatBaseComponent> entries) {
        List<PlayerListItem.Item> items = new ArrayList<>();
        for (Map.Entry<UUID, IChatBaseComponent> entry : entries.entrySet()) {
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(entry.getKey());
            item.setDisplayName(entry.getValue() == null ? null : entry.getValue().toString(player.getVersion()));
            items.add(item);
        }
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
        packet.setItems(items.toArray(new PlayerListItem.Item[0]));
        ((UserConnection)player.getPlayer()).getTabListHandler().onUpdate(packet);
    }

    @Override
    public void updateLatencies(@NonNull Map<UUID, Integer> entries) {
        List<PlayerListItem.Item> items = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : entries.entrySet()) {
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(entry.getKey());
            item.setPing(entry.getValue());
            items.add(item);
        }
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.UPDATE_LATENCY);
        packet.setItems(items.toArray(new PlayerListItem.Item[0]));
        ((UserConnection)player.getPlayer()).getTabListHandler().onUpdate(packet);
    }

    @Override
    public void updateGameModes(@NonNull Map<UUID, Integer> entries) {
        List<PlayerListItem.Item> items = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : entries.entrySet()) {
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(entry.getKey());
            item.setGamemode(entry.getValue());
            items.add(item);
        }
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
        packet.setItems(items.toArray(new PlayerListItem.Item[0]));
        ((UserConnection)player.getPlayer()).getTabListHandler().onUpdate(packet);
    }

    @Override
    public void addEntries(@NonNull Collection<TabListEntry> entries) {
        List<PlayerListItem.Item> items = new ArrayList<>();
        for (TabListEntry data : entries) {
            PlayerListItem.Item item = new PlayerListItem.Item();
            if (data.getDisplayName() != null) item.setDisplayName(data.getDisplayName().toString(player.getVersion()));
            item.setGamemode(data.getGameMode());
            item.setPing(data.getLatency());
            if (data.getSkin() != null) {
                item.setProperties(new Property[]{new Property(TEXTURES_PROPERTY, data.getSkin().getValue(), data.getSkin().getSignature())});
            } else {
                item.setProperties(new Property[0]);
            }
            item.setUsername(data.getName());
            item.setUuid(data.getUniqueId());
            if (data.getChatSession() != null) {
                item.setChatSessionId((UUID) ((Object[])data.getChatSession())[0]);
                item.setPublicKey((PlayerPublicKey) ((Object[])data.getChatSession())[1]);
            }
            items.add(item);
        }
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        packet.setItems(items.toArray(new PlayerListItem.Item[0]));
        ((UserConnection)player.getPlayer()).getTabListHandler().onUpdate(packet);
    }
}
