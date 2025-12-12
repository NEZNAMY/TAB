package me.neznamy.tab.platforms.bukkit.v1_21_R4;

import me.neznamy.tab.shared.platform.TabListEntryTracker;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.a;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Implementation of TabListEntryTracker.
 */
public class NMSTabListEntryTracker extends TabListEntryTracker {

    private static final a ADD_PLAYER = a.valueOf("ADD_PLAYER");

    @Override
    public void onPacketSend(@NotNull Object packet) {
        if (packet instanceof ClientboundPlayerInfoRemovePacket remove) {
            for (UUID id : remove.b()) {
                tablistEntries.remove(id);
            }
        }
        if (packet instanceof ClientboundPlayerInfoUpdatePacket update) {
            if (update.b().contains(ADD_PLAYER)) {
                for (ClientboundPlayerInfoUpdatePacket.b nmsData : update.e()) {
                    tablistEntries.add(nmsData.a());
                }
            }
        }
    }
}
