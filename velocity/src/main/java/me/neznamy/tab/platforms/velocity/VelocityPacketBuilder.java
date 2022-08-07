package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.api.util.GameProfile;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.*;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.platforms.velocity.storage.VelocityPacketStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Packet builder for Velocity platform
 */
public class VelocityPacketBuilder extends PacketBuilder {

    //packet storage
    private final VelocityPacketStorage vps = VelocityPacketStorage.getInstance();

    @Override
    public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        List<Object> items = new ArrayList<>();
        for (PlayerInfoData data : packet.getEntries()) {
            Object item = vps.newItem.newInstance(data.getUniqueId());
            if (data.getDisplayName() != null) {
                if (clientVersion.getMinorVersion() >= 8) {
                    vps.Item_setDisplayName.invoke(item, Main.getInstance().convertComponent(data.getDisplayName(), clientVersion));
                } else {
                    vps.Item_setDisplayName.invoke(item, LegacyComponentSerializer.legacySection().deserialize(data.getDisplayName().toLegacyText()));
                }
            } else if (clientVersion.getMinorVersion() < 8) {
                vps.Item_setDisplayName.invoke(item, LegacyComponentSerializer.legacySection().deserialize(data.getName()));
            }
            if (data.getGameMode() != null) vps.Item_setGameMode.invoke(item, data.getGameMode().ordinal()-1);
            vps.Item_setLatency.invoke(item, data.getLatency());
            if (data.getSkin() != null) {
                vps.Item_setProperties.invoke(item, Collections.singletonList(new GameProfile.Property("textures", data.getSkin().getValue(), data.getSkin().getSignature())));
            } else {
                vps.Item_setProperties.invoke(item, Collections.emptyList());
            }
            vps.Item_setName.invoke(item, data.getName());

            if(clientVersion.getMinorVersion() >= 19) {
                vps.Item_setPlayerKey.invoke(item, data.getProfilePublicKey());
            }

            items.add(item);
        }
        return vps.newPlayerListItem.newInstance(packet.getAction().ordinal(), items);
    }

    @Override
    public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        return vps.newScoreboardDisplay.newInstance((byte)packet.getSlot(), packet.getObjectiveName());
    }

    @Override
    public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        return vps.newScoreboardObjective.newInstance(packet.getObjectiveName(), jsonOrCut(packet.getDisplayName(), clientVersion, 32), packet.getRenderType() == null ? null : vps.HealthDisplay_valueOf.invoke(null, packet.getRenderType().toString()), (byte)packet.getAction());
    }

    @Override
    public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        return vps.newScoreboardSetScore.newInstance(packet.getPlayer(), (byte)packet.getAction().ordinal(), packet.getObjectiveName(), packet.getScore());
    }

    @Override
    public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        int color = 0;
        if (clientVersion.getMinorVersion() >= 13) {
            color = (packet.getColor() != null ? packet.getColor() : EnumChatFormat.lastColorsOf(packet.getPlayerPrefix())).ordinal();
        }
        return vps.newScoreboardTeam.newInstance(packet.getName(), (byte)packet.getAction(), jsonOrCut(packet.getName(), clientVersion, 16), jsonOrCut(packet.getPlayerPrefix(), clientVersion, 16), jsonOrCut(packet.getPlayerSuffix(), clientVersion, 16),
                packet.getNameTagVisibility(), packet.getCollisionRule(), color, (byte)packet.getOptions(), packet.getPlayers() instanceof List ? packet.getPlayers() : new ArrayList<>(packet.getPlayers()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketPlayOutPlayerInfo readPlayerInfo(Object packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
        List<PlayerInfoData> listData = new ArrayList<>();
        for (Object i : (List<Object>) vps.PlayerListItem_getItems.invoke(packet)) {
            Component displayNameComponent = (Component) vps.Item_getDisplayName.invoke(i);
            String displayName = displayNameComponent == null ? null : GsonComponentSerializer.gson().serialize(displayNameComponent);
            List<GameProfile.Property> properties = vps.Item_getProperties.invoke(i) == null ? null : (List<GameProfile.Property>) vps.Item_getProperties.invoke(i);
            Skin skin = properties == null || properties.size() == 0 ? null : new Skin(properties.get(0).getValue(), properties.get(0).getSignature());

            IdentifiedKey identifiedKey = (IdentifiedKey) vps.Item_getPlayerKey.invoke(i);

            listData.add(new PlayerInfoData((String) vps.Item_getName.invoke(i), (UUID) vps.Item_getUuid.invoke(i), skin, (int) vps.Item_getLatency.invoke(i),
                    EnumGamemode.VALUES[(int) vps.Item_getGameMode.invoke(i) + 1], displayName == null ? null : IChatBaseComponent.deserialize(displayName), identifiedKey));
        }
        return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.values()[(int) vps.PlayerListItem_getAction.invoke(packet)], listData);
    }

    @Override
    public PacketPlayOutScoreboardObjective readObjective(Object packet) throws ReflectiveOperationException {
        return new PacketPlayOutScoreboardObjective((byte)vps.ScoreboardObjective_getAction.invoke(packet), (String)vps.ScoreboardObjective_getName.invoke(packet),
                null, PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay.INTEGER);
    }

    @Override
    public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object packet) throws ReflectiveOperationException {
        return new PacketPlayOutScoreboardDisplayObjective((byte)vps.ScoreboardDisplay_getPosition.invoke(packet), (String)vps.ScoreboardDisplay_getName.invoke(packet));
    }
}