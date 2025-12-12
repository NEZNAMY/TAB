package me.neznamy.tab.platforms.bukkit.v1_17_R1;

import me.neznamy.tab.shared.platform.TabListEntryTracker;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of TabListEntryTracker.
 */
public class NMSTabListEntryTracker extends TabListEntryTracker {

    private static final EnumPlayerInfoAction ADD_PLAYER = EnumPlayerInfoAction.valueOf("ADD_PLAYER");
    private static final EnumPlayerInfoAction REMOVE_PLAYER = EnumPlayerInfoAction.valueOf("REMOVE_PLAYER");

    @Override
    public void onPacketSend(@NotNull Object packet) {
        if (packet instanceof PacketPlayOutPlayerInfo) {
            PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo) packet;
            for (PacketPlayOutPlayerInfo.PlayerInfoData nmsData : info.b()) {
                if (info.c() == ADD_PLAYER) {
                    tablistEntries.add(nmsData.a().getId());
                }
                if (info.c() == REMOVE_PLAYER) {
                    tablistEntries.remove(nmsData.a().getId());
                }
            }
        }
    }
}
