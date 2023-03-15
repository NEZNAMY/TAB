package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityMetadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Custom class for holding data used in PacketPlayOutEntityMetadata minecraft packet.
 */
public class PacketPlayOutEntityMetadataStorage implements TabPacket {

    /** NMS Fields */
    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR;
    public static Field LIST;

    /**
     * Loads all required Fields and throws Exception if something went wrong
     *
     * @param   nms
     *          NMS storage reference
     * @throws  NoSuchMethodException
     *          If something fails
     */
    public static void load(NMSStorage nms) throws NoSuchMethodException {
        if (nms.is1_19_3Plus()) {
            CONSTRUCTOR = CLASS.getConstructor(int.class, List.class);
        } else {
            CONSTRUCTOR = CLASS.getConstructor(int.class, DataWatcher.CLASS, boolean.class);
        }
        LIST = nms.getFields(CLASS, List.class).get(0);
    }

    /**
     * Converts this class into NMS packet
     *
     * @return  NMS packet
     * @throws  ReflectiveOperationException
     *          If something went wrong
     */
    public static Object build(PacketPlayOutEntityMetadata packet) throws ReflectiveOperationException {
        if (CONSTRUCTOR.getParameterCount() == 2) {
            //1.19.3+
            return CONSTRUCTOR.newInstance(packet.getEntityId(), DataWatcher.packDirty.invoke(((DataWatcher)packet.getDataWatcher()).build()));
        } else {
            return CONSTRUCTOR.newInstance(packet.getEntityId(), ((DataWatcher)packet.getDataWatcher()).build(), true);
        }
    }

    public static Object buildSilent(PacketPlayOutEntityMetadata packet) {
        try {
            return build(packet);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}