package me.neznamy.tab.platforms.bungeecord;

import java.util.*;
import java.util.stream.Collectors;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.*;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import net.md_5.bungee.protocol.PlayerPublicKey;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.*;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

/**
 * Packet builder for BungeeCord platform
 */
public class BungeePacketBuilder extends PacketBuilder {

    @Override
    public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) {
        if (clientVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId() &&
                packet.getActions().contains(EnumPlayerInfoAction.REMOVE_PLAYER)) {
            PlayerListItemRemove remove = new PlayerListItemRemove();
            remove.setUuids(packet.getEntries().stream().map(PlayerInfoData::getUniqueId).toArray(UUID[]::new));
            return remove;
        }
        List<Item> items = new ArrayList<>();
        for (PlayerInfoData data : packet.getEntries()) {
            Item item = new Item();
            if (data.getDisplayName() != null) {
                if (clientVersion.getMinorVersion() >= 8) {
                    item.setDisplayName(data.getDisplayName().toString(clientVersion));
                } else {
                    item.setDisplayName(data.getDisplayName().toLegacyText());
                }
            } else if (clientVersion.getMinorVersion() < 8) {
                item.setDisplayName(String.valueOf(data.getName())); //avoiding NPE, 1.7 client requires this, 1.8 added a leading boolean
            }
            if (data.getGameMode() != null) item.setGamemode(data.getGameMode().ordinal() - 1);
            item.setListed(data.isListed());
            item.setPing(data.getLatency());
            if (data.getSkin() != null) {
                item.setProperties(new Property[]{new Property("textures", data.getSkin().getValue(), data.getSkin().getSignature())});
            } else {
                item.setProperties(new Property[0]);
            }
            item.setUsername(data.getName());
            item.setUuid(data.getUniqueId());
            item.setChatSessionId(data.getChatSessionId());
            item.setPublicKey((PlayerPublicKey) data.getProfilePublicKey());
            items.add(item);
        }
        if (clientVersion.getNetworkId() >= ProtocolVersion.V1_19_3.getNetworkId()) {
            PlayerListItemUpdate bungeePacket = new PlayerListItemUpdate();
            PlayerListItemUpdate.Action[] array = packet.getActions().stream().map(this::convertToUpdateAction).toArray(PlayerListItemUpdate.Action[]::new);
            bungeePacket.setActions(EnumSet.of(array[0], array));
            bungeePacket.setItems(items.toArray(new Item[0]));
            return bungeePacket;
        } else {
            PlayerListItem bungeePacket = new PlayerListItem();
            EnumPlayerInfoAction action = packet.getActions().contains(EnumPlayerInfoAction.ADD_PLAYER) ?
                    EnumPlayerInfoAction.ADD_PLAYER : packet.getActions().iterator().next();
            bungeePacket.setAction(convertToItemAction(action));
            bungeePacket.setItems(items.toArray(new Item[0]));
            return bungeePacket;
        }
    }

    private PlayerListItemUpdate.Action convertToUpdateAction(EnumPlayerInfoAction action) {
        if (action == EnumPlayerInfoAction.UPDATE_GAME_MODE) return PlayerListItemUpdate.Action.UPDATE_GAMEMODE;
        return PlayerListItemUpdate.Action.valueOf(action.name());
    }

    private PlayerListItem.Action convertToItemAction(EnumPlayerInfoAction action) {
        if (action == EnumPlayerInfoAction.UPDATE_GAME_MODE) return PlayerListItem.Action.UPDATE_GAMEMODE;
        return PlayerListItem.Action.valueOf(action.name());
    }

    @Override
    public PacketPlayOutPlayerInfo readPlayerInfo(Object bungeePacket, ProtocolVersion clientVersion) {
        if (bungeePacket instanceof PlayerListItemRemove) {
            return new PacketPlayOutPlayerInfo(
                    EnumPlayerInfoAction.REMOVE_PLAYER,
                    Arrays.stream(((PlayerListItemRemove) bungeePacket).getUuids()).map(PlayerInfoData::new).collect(Collectors.toList())
            );
        }
        Item[] items = bungeePacket instanceof PlayerListItemUpdate ? ((PlayerListItemUpdate) bungeePacket).getItems() : ((PlayerListItem) bungeePacket).getItems();
        List<PlayerInfoData> listData = new ArrayList<>();
        for (Item i : items) {
            Skin skin = i.getProperties() == null || i.getProperties().length == 0 ? null : new Skin(i.getProperties()[0].getValue(), i.getProperties()[0].getSignature());
            listData.add(new PlayerInfoData(
                    i.getUsername(),
                    i.getUuid(),
                    skin,
                    Boolean.TRUE.equals(i.getListed()),
                    i.getPing() == null ? 0 : i.getPing(),
                    i.getGamemode() == null ? null : EnumGamemode.VALUES[i.getGamemode()+1],
                    IChatBaseComponent.deserialize(i.getDisplayName()),
                    i.getChatSessionId(),
                    i.getPublicKey()));
        }
        if (bungeePacket instanceof PlayerListItemUpdate) {
            EnumPlayerInfoAction[] array = ((PlayerListItemUpdate) bungeePacket).getActions().stream().map(this::convertFromUpdateAction).toArray(EnumPlayerInfoAction[]::new);
            return new PacketPlayOutPlayerInfo(EnumSet.of(array[0], array), listData);
        } else {
            return new PacketPlayOutPlayerInfo(convertFromItemAction(((PlayerListItem) bungeePacket).getAction()), listData);
        }
    }

    private EnumPlayerInfoAction convertFromUpdateAction(PlayerListItemUpdate.Action action) {
        if (action == PlayerListItemUpdate.Action.UPDATE_GAMEMODE) return EnumPlayerInfoAction.UPDATE_GAME_MODE;
        return EnumPlayerInfoAction.valueOf(action.name());
    }

    private EnumPlayerInfoAction convertFromItemAction(PlayerListItem.Action action) {
        if (action == PlayerListItem.Action.UPDATE_GAMEMODE) return EnumPlayerInfoAction.UPDATE_GAME_MODE;
        return EnumPlayerInfoAction.valueOf(action.name());
    }
}