package me.neznamy.tab.platforms.sponge;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.List;
import java.util.UUID;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.world.level.GameType;

public final class SpongePacketBuilder extends PacketBuilder {

    private static final UUID SYSTEM_ID = new UUID(0, 0);

    @Override
    public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) {
        return packet;
    }

    @Override
    public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) {
        return new ClientboundChatPacket(ComponentUtils.fromComponent(packet.getMessage(), clientVersion), ChatType.valueOf(packet.getType().name()), SYSTEM_ID);
    }

    @Override
    public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) {
        final EnumPlayerInfoAction action = packet.getActions().contains(EnumPlayerInfoAction.ADD_PLAYER) ? EnumPlayerInfoAction.ADD_PLAYER : packet.getActions().iterator().next();
        final List<ClientboundPlayerInfoPacket.PlayerUpdate> entries = packet.getEntries().stream().map(entry -> {
            final GameProfile profile = new GameProfile(entry.getUniqueId(), entry.getName());
            if (entry.getSkin() != null) {
                profile.getProperties().put("textures", new Property("textures", entry.getSkin().getValue(), entry.getSkin().getSignature()));
            }
            final GameType type = GameType.valueOf(entry.getGameMode().name());
            return new ClientboundPlayerInfoPacket.PlayerUpdate(profile, entry.getLatency())
        })
        final ClientboundPlayerInfoPacket infoPacket = new ClientboundPlayerInfoPacket()
    }
}
