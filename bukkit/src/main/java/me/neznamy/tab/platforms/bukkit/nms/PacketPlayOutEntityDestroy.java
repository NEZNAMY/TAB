package me.neznamy.tab.platforms.bukkit.nms;

import lombok.Data;
import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Custom class for holding data used in PacketPlayOutEntityDestroy minecraft packet.
 */
@Data
public class PacketPlayOutEntityDestroy implements TabPacket {

    /** NMS Fields */
    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR;
    public static Field ENTITIES;

    /** Packet's instance fields */
    private final int[] entities;

    /**
     * Loads all required Fields and throws Exception if something went wrong
     *
     * @param   nms
     *          NMS storage reference
     * @throws  NoSuchMethodException
     *          If something fails
     */
    public static void load(NMSStorage nms) throws NoSuchMethodException {
        (ENTITIES = CLASS.getDeclaredFields()[0]).setAccessible(true);
        try {
            CONSTRUCTOR = CLASS.getConstructor(int[].class);
        } catch (NoSuchMethodException e) {
            //1.17.0
            CONSTRUCTOR = CLASS.getConstructor(int.class);
        }
    }

    /**
     * Constructs new instance with given parameter
     *
     * @param   entities
     *          Destroyed entities
     */
    public PacketPlayOutEntityDestroy(int... entities) {
        this.entities = entities;
    }

    /**
     * Converts this class into NMS packet
     *
     * @return  NMS packet
     * @throws  ReflectiveOperationException
     *          If something went wrong
     */
    public Object build() throws ReflectiveOperationException {
        if (CONSTRUCTOR.getParameterTypes()[0] != int.class) {
            return CONSTRUCTOR.newInstance(new Object[]{entities});
        } else {
            //1.17.0
            return CONSTRUCTOR.newInstance(entities[0]);
        }
    }
}