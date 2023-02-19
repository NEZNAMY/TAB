package me.neznamy.tab.platforms.bukkit.nms.storage;

import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;

/**
 * NMS loader for Thermos 1.7.10.
 */
public class ThermosNMSStorage extends BukkitLegacyNMSStorage {

    public ThermosNMSStorage() throws ReflectiveOperationException {}

    @Override
    public Class<?> getLegacyClass(String name) throws ClassNotFoundException {
        try {
            return Main.class.getClassLoader().loadClass("net.minecraft.server." + serverPackage + "." + name);
        } catch (NullPointerException e) {
            // nested class not found
            throw new ClassNotFoundException(name);
        }
    }

    @Override
    public void loadNamedFieldsAndMethods() throws ReflectiveOperationException {
        PING = getField(EntityPlayer, "field_71138_i");
        ScoreboardScore_setScore = ScoreboardScore.getMethod("func_96647_c", int.class);
        ScoreboardTeam_setAllowFriendlyFire = ScoreboardTeam.getMethod("func_96660_a", boolean.class);
        ScoreboardTeam_setCanSeeFriendlyInvisibles = ScoreboardTeam.getMethod("func_98300_b", boolean.class);
        ChatSerializer_DESERIALIZE = ChatSerializer.getMethod("func_150699_a", String.class);
        DataWatcher.REGISTER = DataWatcher.CLASS.getMethod("func_75682_a", int.class, Object.class);
        ScoreboardTeam_setPrefix = ScoreboardTeam.getMethod("func_96666_b", String.class);
        ScoreboardTeam_setSuffix = ScoreboardTeam.getMethod("func_96662_c", String.class);
    }
}
