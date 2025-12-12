package me.neznamy.tab.platforms.neoforge;

import me.neznamy.tab.shared.platform.TabListEntryTracker;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * NeoForge implementation of TabListEntryTracker.
 */
public class NeoForgeTabListEntryTracker extends TabListEntryTracker {

    @Override
    public void onPacketSend(@NotNull Object packet) {
        if (packet instanceof ClientboundPlayerInfoRemovePacket remove) {
            for (UUID id : remove.profileIds()) {
                tablistEntries.remove(id);
            }
        }
        if (packet instanceof ClientboundPlayerInfoUpdatePacket update) {
            if (update.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
                for (ClientboundPlayerInfoUpdatePacket.Entry nmsData : update.entries()) {
                    tablistEntries.add(nmsData.profileId());
                }
            }
        }
    }
}
