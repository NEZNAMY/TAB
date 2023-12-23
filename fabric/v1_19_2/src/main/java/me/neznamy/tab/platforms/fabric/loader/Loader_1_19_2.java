package me.neznamy.tab.platforms.fabric.loader;

import me.neznamy.tab.platforms.fabric.FabricMultiVersion;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Method loader compiled using Minecraft 1.19.2.
 */
@SuppressWarnings("unused") // Actually used, just via reflection
public class Loader_1_19_2 {

    /**
     * Constructs new instance and registers methods only available in this version.
     *
     * @param   serverVersion
     *          Exact server version
     */
    public Loader_1_19_2(@NotNull ProtocolVersion serverVersion) {
        if (serverVersion.getMinorVersion() >= 19) {
            FabricMultiVersion.buildTabListPacket = (action, entry) -> {
                ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.valueOf(action.name()), Collections.emptyList());
                ReflectionUtils.getFields(ClientboundPlayerInfoPacket.class, List.class).get(0).set(packet,
                        Collections.singletonList(new ClientboundPlayerInfoPacket.PlayerUpdate(
                                entry.createProfile(),
                                entry.getLatency(),
                                GameType.byId(entry.getGameMode()),
                                entry.getDisplayName(),
                                null
                        )));
                return packet;
            };
        }
    }
}
