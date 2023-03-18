package me.neznamy.tab.platforms.bungeecord;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.tablist.TabListEntry;
import me.neznamy.tab.shared.tablist.BulkUpdateTabList;
import net.md_5.bungee.protocol.PlayerPublicKey;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.protocol.packet.PlayerListItemRemove;
import net.md_5.bungee.protocol.packet.PlayerListItemUpdate;

import java.util.*;

@RequiredArgsConstructor
public class BungeeTabList extends BulkUpdateTabList {

    /** Player this TabList belongs to */
    private final BungeeTabPlayer player;

    @Override
    public void removeEntries(@NonNull Collection<UUID> entries) {
        if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) {
            PlayerListItemRemove remove = new PlayerListItemRemove();
            remove.setUuids(entries.toArray(new UUID[0]));
            player.sendPacket(remove);
        } else if (player.getVersion().getMinorVersion() >= 8) {
            PlayerListItem packet = new PlayerListItem();
            packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
            List<Item> items = new ArrayList<>();
            for (UUID uuid : entries) {
                Item item = new Item();
                item.setUuid(uuid);
                items.add(item);
            }
            packet.setItems(items.toArray(new Item[0]));
            player.sendPacket(packet);
        } else {
            for (UUID uuid : entries) {
                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
                Item item = new Item();
                item.setUuid(uuid);
                packet.setItems(new Item[]{item});
                player.sendPacket(packet);
            }
        }
    }

    @Override
    public void updateDisplayNames(@NonNull Map<UUID, IChatBaseComponent> entries) {
        if (player.getVersion().getMinorVersion() >= 8) {
            List<Item> items = new ArrayList<>();
            for (Map.Entry<UUID, IChatBaseComponent> entry : entries.entrySet()) {
                Item item = new Item();
                item.setUuid(entry.getKey());
                item.setDisplayName(entry.getValue() == null ? null : entry.getValue().toString(player.getVersion()));
                items.add(item);
            }
            if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) {
                PlayerListItemUpdate packet = new PlayerListItemUpdate();
                packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME));
                packet.setItems(items.toArray(new Item[0]));
                player.sendPacket(packet);
            } else {
                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
                packet.setItems(items.toArray(new Item[0]));
                player.sendPacket(packet);
            }
        } else {
            for (Map.Entry<UUID, IChatBaseComponent> entry : entries.entrySet()) {
                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
                Item item = new Item();
                item.setUuid(entry.getKey());
                if (entry.getValue() != null) {
                    item.setDisplayName(entry.getValue().toLegacyText());
                } else {
                    item.setDisplayName("NULL"); // Should be unused by TAB
                }
                packet.setItems(new Item[]{item});
                player.sendPacket(packet);
            }
        }
    }

    @Override
    public void updateLatencies(@NonNull Map<UUID, Integer> entries) {
        if (player.getVersion().getMinorVersion() >= 8) {
            List<Item> items = new ArrayList<>();
            for (Map.Entry<UUID, Integer> entry : entries.entrySet()) {
                Item item = new Item();
                item.setUuid(entry.getKey());
                item.setPing(entry.getValue());
                items.add(item);
            }
            if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) {
                PlayerListItemUpdate packet = new PlayerListItemUpdate();
                packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_LATENCY));
                packet.setItems(items.toArray(new Item[0]));
                player.sendPacket(packet);
            } else {
                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.UPDATE_LATENCY);
                packet.setItems(items.toArray(new Item[0]));
                player.sendPacket(packet);
            }
        } else {
            for (Map.Entry<UUID, Integer> entry : entries.entrySet()) {
                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.UPDATE_LATENCY);
                Item item = new Item();
                item.setUuid(entry.getKey());
                item.setPing(entry.getValue());
                packet.setItems(new Item[]{item});
                player.sendPacket(packet);
            }
        }
    }

    @Override
    public void updateGameModes(@NonNull Map<UUID, Integer> entries) {
        if (player.getVersion().getMinorVersion() >= 8) {
            List<Item> items = new ArrayList<>();
            for (Map.Entry<UUID, Integer> entry : entries.entrySet()) {
                Item item = new Item();
                item.setUuid(entry.getKey());
                item.setGamemode(entry.getValue());
                items.add(item);
            }
            if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) {
                PlayerListItemUpdate packet = new PlayerListItemUpdate();
                packet.setActions(EnumSet.of(PlayerListItemUpdate.Action.UPDATE_GAMEMODE));
                packet.setItems(items.toArray(new Item[0]));
                player.sendPacket(packet);
            } else {
                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
                packet.setItems(items.toArray(new Item[0]));
                player.sendPacket(packet);
            }
        } else {
            for (Map.Entry<UUID, Integer> entry : entries.entrySet()) {
                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
                Item item = new Item();
                item.setUuid(entry.getKey());
                item.setGamemode(entry.getValue());
                packet.setItems(new Item[]{item});
                player.sendPacket(packet);
            }
        }
    }

    @Override
    public void addEntries(@NonNull Collection<TabListEntry> entries) {
        if (player.getVersion().getMinorVersion() >= 8) {
            List<Item> items = new ArrayList<>();
            for (TabListEntry data : entries) {
                Item item = new Item();
                if (data.getDisplayName() != null) item.setDisplayName(data.getDisplayName().toString(player.getVersion()));
                item.setGamemode(data.getGameMode());
                item.setListed(data.isListed());
                item.setPing(data.getLatency());
                if (data.getSkin() != null) {
                    item.setProperties(new Property[]{new Property("textures", data.getSkin().getValue(), data.getSkin().getSignature())});
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
            if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) {
                PlayerListItemUpdate bungeePacket = new PlayerListItemUpdate();
                bungeePacket.setActions(EnumSet.allOf(PlayerListItemUpdate.Action.class));
                bungeePacket.setItems(items.toArray(new Item[0]));
                player.sendPacket(bungeePacket);
            } else {
                PlayerListItem bungeePacket = new PlayerListItem();
                bungeePacket.setAction(PlayerListItem.Action.ADD_PLAYER);
                bungeePacket.setItems(items.toArray(new Item[0]));
                player.sendPacket(bungeePacket);
            }
        } else {
            for (TabListEntry data : entries) {
                Item item = new Item();
                if (data.getDisplayName() != null) {
                    item.setDisplayName(data.getDisplayName().toLegacyText());
                } else {
                    item.setDisplayName(data.getName()); //avoiding NPE, 1.7 client requires this, 1.8 added a leading boolean
                }
                item.setGamemode(data.getGameMode());
                item.setListed(data.isListed());
                item.setPing(data.getLatency());
                if (data.getSkin() != null) {
                    item.setProperties(new Property[]{new Property("textures", data.getSkin().getValue(), data.getSkin().getSignature())});
                } else {
                    item.setProperties(new Property[0]);
                }
                item.setUsername(data.getName());
                item.setUuid(data.getUniqueId());
                PlayerListItem bungeePacket = new PlayerListItem();
                bungeePacket.setAction(PlayerListItem.Action.ADD_PLAYER);
                bungeePacket.setItems(new Item[]{item});
                player.sendPacket(bungeePacket);
            }
        }
    }
}
