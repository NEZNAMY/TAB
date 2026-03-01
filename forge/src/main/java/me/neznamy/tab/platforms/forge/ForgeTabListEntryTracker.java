package me.neznamy.tab.platforms.forge;

import me.neznamy.tab.shared.platform.TabListEntryTracker;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Forge implementation of TabListEntryTracker.
 */
public class ForgeTabListEntryTracker extends TabListEntryTracker {

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

    /**
     * Override to always return true, because join event is called too late, so TAB
     * is not able to catch all packets, thus it is not able to track tablist entries properly.
     * Return true to avoid not applying features. May result in client warnings if using a vanish
     * mod or similar, but that's a tradeoff.
     *
     * @param   uuid
     *          UUID of player to check
     * @return  true
     */
    @Override
    public boolean containsEntry(@NotNull UUID uuid) {
        return true;
    }
}
