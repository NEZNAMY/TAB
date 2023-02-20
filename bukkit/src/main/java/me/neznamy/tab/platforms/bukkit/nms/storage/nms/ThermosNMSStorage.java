package me.neznamy.tab.platforms.bukkit.nms.storage.nms;

import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardScoreStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardTeamStorage;

/**
 * NMS loader for Thermos 1.7.10.
 */
public class ThermosNMSStorage extends BukkitLegacyNMSStorage {

    public ThermosNMSStorage() throws ReflectiveOperationException {}

    @Override
    public Class<?> getLegacyClass(String name) throws ClassNotFoundException {
        try {
            return getClass().getClassLoader().loadClass("net.minecraft.server." + serverPackage + "." + name);
        } catch (NullPointerException e) {
            // nested class not found
            throw new ClassNotFoundException(name);
        }
    }

    @Override
    public void loadNamedFieldsAndMethods() throws ReflectiveOperationException {
        (PING = EntityPlayer.getDeclaredField("field_71138_i")).setAccessible(true);
        PacketPlayOutScoreboardScoreStorage.ScoreboardScore_setScore = PacketPlayOutScoreboardScoreStorage.ScoreboardScore.getMethod("func_96647_c", int.class);
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setAllowFriendlyFire = PacketPlayOutScoreboardTeamStorage.ScoreboardTeam.getMethod("func_96660_a", boolean.class);
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setCanSeeFriendlyInvisibles = PacketPlayOutScoreboardTeamStorage.ScoreboardTeam.getMethod("func_98300_b", boolean.class);
        ChatSerializer_DESERIALIZE = ChatSerializer.getMethod("func_150699_a", String.class);
        DataWatcher.REGISTER = DataWatcher.CLASS.getMethod("func_75682_a", int.class, Object.class);
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setPrefix = PacketPlayOutScoreboardTeamStorage.ScoreboardTeam.getMethod("func_96666_b", String.class);
        PacketPlayOutScoreboardTeamStorage.ScoreboardTeam_setSuffix = PacketPlayOutScoreboardTeamStorage.ScoreboardTeam.getMethod("func_96662_c", String.class);
    }
}
