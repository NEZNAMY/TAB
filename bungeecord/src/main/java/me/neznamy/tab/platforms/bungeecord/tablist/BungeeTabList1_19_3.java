package me.neznamy.tab.platforms.bungeecord.tablist;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.TabListEntry;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.tablist.BulkUpdateTabList;
import net.md_5.bungee.protocol.PlayerPublicKey;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;

import java.util.*;

@RequiredArgsConstructor
public class BungeeTabList1_19_3 extends BulkUpdateTabList {

    /** Player this TabList belongs to */
    private final BungeeTabPlayer player;

    @Override
    public void removeEntries(@NonNull Collection<UUID> entries) {
        PlayerListItemRemove remove = new PlayerListItemRemove();
        remove.setUuids(entries.toArray(new UUID[0]));
        player.sendPacket(remove);
    }

    @Override
    public void updateDisplayNames(@NonNull Map<UUID, IChatBaseComponent> entries) {
        List<Item> items = new ArrayList<>();
        for (Map.Entry<UUID, IChatBaseComponent> entry : entries.entrySet()) {
            Item item = new Item();
            item.setUuid(entry.getKey());
            item.setDisplayName(entry.getValue() == null ? null : entry.getValue().toString(player.getVersion()));
            items.add(item);
        }
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME));
        packet.setItems(items.toArray(new Item[0]));
        player.sendPacket(packet);
    }

    @Override
    public void updateLatencies(@NonNull Map<UUID, Integer> entries) {
        List<Item> items = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : entries.entrySet()) {
            Item item = new Item();
            item.setUuid(entry.getKey());
            item.setPing(entry.getValue());
            items.add(item);
        }
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LATENCY));
        packet.setItems(items.toArray(new Item[0]));
        player.sendPacket(packet);
    }

    @Override
    public void updateGameModes(@NonNull Map<UUID, Integer> entries) {
        List<Item> items = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : entries.entrySet()) {
            Item item = new Item();
            item.setUuid(entry.getKey());
            item.setGamemode(entry.getValue());
            items.add(item);
        }
        PlayerListItemUpdate packet = new PlayerListItemUpdate();
        packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_GAMEMODE));
        packet.setItems(items.toArray(new Item[0]));
        player.sendPacket(packet);
    }

    @Override
    public void addEntries(@NonNull Collection<TabListEntry> entries) {
        List<Item> items = new ArrayList<>();
        for (TabListEntry data : entries) {
            Item item = new Item();
            if (data.getDisplayName() != null) item.setDisplayName(data.getDisplayName().toString(player.getVersion()));
            item.setGamemode(data.getGameMode());
            item.setListed(data.isListed());
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
        PlayerListItemUpdate bungeePacket = new PlayerListItemUpdate();
        bungeePacket.setActions(EnumSet.allOf(PlayerListItemUpdate.Action.class));
        bungeePacket.setItems(items.toArray(new Item[0]));
        player.sendPacket(bungeePacket);
    }
}
