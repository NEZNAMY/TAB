package me.neznamy.tab.platforms.fabric.loader;

import com.mojang.authlib.GameProfile;
import me.neznamy.tab.platforms.fabric.FabricMultiVersion;
import me.neznamy.tab.platforms.fabric.FabricTabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Method loader compiled using Minecraft 1.18.2.
 */
@SuppressWarnings("unused") // Actually used, just via reflection
public class Loader_1_18_2 {

    /**
     * Constructs new instance and registers methods only available in this version.
     *
     * @param   serverVersion
     *          Exact server version
     */
    public Loader_1_18_2(@NotNull ProtocolVersion serverVersion) {
        if (serverVersion.getMinorVersion() >= 16) {
            FabricMultiVersion.sendMessage = (player, message) -> player.sendMessage(message, new UUID(0, 0));
        }
        if (serverVersion.getMinorVersion() >= 17) {
            FabricMultiVersion.buildTabListPacket = (action, entry) -> {
                ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()), Collections.emptyList());
                ReflectionUtils.getFields(ClientboundPlayerInfoPacket.class, List.class).get(0).set(packet,
                        Collections.singletonList(new ClientboundPlayerInfoPacket.PlayerUpdate(
                                entry.createProfile(),
                                entry.getLatency(),
                                GameType.byId(entry.getGameMode()),
                                entry.getDisplayName()
                        )));
                return packet;
            };
            FabricMultiVersion.onPlayerInfo = (receiver, packet0) -> {
                ClientboundPlayerInfoPacket packet = (ClientboundPlayerInfoPacket) packet0;
                for (ClientboundPlayerInfoPacket.PlayerUpdate nmsData : packet.getEntries()) {
                    GameProfile profile = nmsData.getProfile();
                    Field displayNameField = ReflectionUtils.getFields(ClientboundPlayerInfoPacket.PlayerUpdate.class, Component.class).get(0);
                    Field latencyField = ReflectionUtils.getFields(ClientboundPlayerInfoPacket.PlayerUpdate.class, int.class).get(0);
                    if (packet.getAction() == ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME) {
                        IChatBaseComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, profile.getId());
                        if (newDisplayName != null) displayNameField.set(nmsData, ((FabricTabPlayer)receiver).getPlatform().toComponent(newDisplayName, receiver.getVersion()));
                    }
                    if (packet.getAction() == ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY) {
                        latencyField.set(nmsData, TAB.getInstance().getFeatureManager().onLatencyChange(receiver, profile.getId(), latencyField.getInt(nmsData)));
                    }
                    if (packet.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
                        TAB.getInstance().getFeatureManager().onEntryAdd(receiver, profile.getId(), profile.getName());
                    }
                }
            };
        }
    }
}
