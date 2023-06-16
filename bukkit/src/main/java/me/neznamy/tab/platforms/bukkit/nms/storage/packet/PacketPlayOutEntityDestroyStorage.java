package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Custom class for holding data used in PacketPlayOutEntityDestroy minecraft packet.
 */
public class PacketPlayOutEntityDestroyStorage {

    /** NMS Fields */
    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR;
    public static Field ENTITIES;

    /**
     * Loads all required Fields and throws Exception if something went wrong
     *
     * @throws  NoSuchMethodException
     *          If something fails
     */
    public static void load() throws NoSuchMethodException {
        (ENTITIES = CLASS.getDeclaredFields()[0]).setAccessible(true);
        try {
            CONSTRUCTOR = CLASS.getConstructor(int[].class);
        } catch (NoSuchMethodException e) {
            //1.17.0
            CONSTRUCTOR = CLASS.getConstructor(int.class);
        }
    }
}