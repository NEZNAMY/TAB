package me.neznamy.tab.platforms.bukkit.v1_8_R1;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.platform.TabListEntryTracker;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.server.v1_8_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R1.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R1.PlayerInfoData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Implementation of TabListEntryTracker.
 */
public class NMSTabListEntryTracker extends TabListEntryTracker {

    private static final Field ACTION = ReflectionUtils.getOnlyField(PacketPlayOutPlayerInfo.class, EnumPlayerInfoAction.class);
    private static final Field PLAYERS = ReflectionUtils.getOnlyField(PacketPlayOutPlayerInfo.class, List.class);

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void onPacketSend(@NotNull Object packet) {
        if (packet instanceof PacketPlayOutPlayerInfo) {
            PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo) packet;
            EnumPlayerInfoAction action = (EnumPlayerInfoAction) ACTION.get(info);
            for (PlayerInfoData nmsData : (List<PlayerInfoData>) PLAYERS.get(info)) {
                if (action == EnumPlayerInfoAction.ADD_PLAYER) {
                    tablistEntries.add(nmsData.a().getId());
                }
                if (action == EnumPlayerInfoAction.REMOVE_PLAYER) {
                    tablistEntries.remove(nmsData.a().getId());
                }
            }
        }
    }
}
