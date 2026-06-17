package me.neznamy.tab.platforms.bukkit.v26_2;

import io.netty.channel.Channel;
import me.neznamy.tab.shared.platform.NettyTabListEntryTracker;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Implementation of TabListEntryTracker.
 */
public class NMSTabListEntryTracker extends NettyTabListEntryTracker {

    public NMSTabListEntryTracker(@NotNull Channel channel) {
        super(channel);
    }

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
