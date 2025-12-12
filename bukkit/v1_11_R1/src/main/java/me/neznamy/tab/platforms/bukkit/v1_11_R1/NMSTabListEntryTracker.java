package me.neznamy.tab.platforms.bukkit.v1_11_R1;

import com.mojang.authlib.GameProfile;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.platform.TabListEntryTracker;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.minecraft.server.v1_11_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_11_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Implementation of TabListEntryTracker.
 */
public class NMSTabListEntryTracker extends TabListEntryTracker {

    private static final Field ACTION = ReflectionUtils.getOnlyField(PacketPlayOutPlayerInfo.class, EnumPlayerInfoAction.class);
    private static final Field PLAYERS = ReflectionUtils.getOnlyField(PacketPlayOutPlayerInfo.class, List.class);
    private static final Class<?> PlayerInfoData = PacketPlayOutPlayerInfo.class.getDeclaredClasses()[0];
    private static final Field PlayerInfoData_Profile = ReflectionUtils.getOnlyField(PlayerInfoData, GameProfile.class);

    @Override
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void onPacketSend(@NotNull Object packet) {
        if (packet instanceof PacketPlayOutPlayerInfo) {
            PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo) packet;
            EnumPlayerInfoAction action = (EnumPlayerInfoAction) ACTION.get(info);
            for (Object nmsData : (List<Object>) PLAYERS.get(info)) {
                GameProfile profile = (GameProfile) PlayerInfoData_Profile.get(nmsData);
                if (action == EnumPlayerInfoAction.ADD_PLAYER) {
                    tablistEntries.add(profile.getId());
                }
                if (action == EnumPlayerInfoAction.REMOVE_PLAYER) {
                    tablistEntries.remove(profile.getId());
                }
            }
        }
    }
}
