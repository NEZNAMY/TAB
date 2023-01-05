package me.neznamy.tab.platforms.bukkit.nms.storage;

import me.neznamy.tab.platforms.bukkit.Main;

/**
 * NMS loader for Thermos 1.7.10.
 */
public class ThermosNMSStorage extends BukkitLegacyNMSStorage {

    /**
     * Creates new instance, initializes required NMS classes and fields
     *
     * @throws  ReflectiveOperationException
     *          If any class, field or method fails to load
     */
    public ThermosNMSStorage() throws ReflectiveOperationException {
    }

    /**
     * Returns class from given name
     *
     * @param   name
     *          class name
     * @return  class from given name
     * @throws  ClassNotFoundException
     *          if class was not found
     */
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
        DataWatcher_REGISTER = DataWatcher.getMethod("func_75682_a", int.class, Object.class);
        ScoreboardTeam_setPrefix = ScoreboardTeam.getMethod("func_96666_b", String.class);
        ScoreboardTeam_setSuffix = ScoreboardTeam.getMethod("func_96662_c", String.class);
    }
}
