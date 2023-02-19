package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * A class representing the n.m.s.DataWatcherObject class to make work with it much easier
 */
@Data @AllArgsConstructor
public class DataWatcherObject {

    /** NMS Fields */
    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR;
    public static Field SLOT;
    public static Field SERIALIZER;
    
    /** Instance fields */
    private final int position;
    private final Object serializer;

    /**
     * Loads all required Fields and throws Exception if something went wrong
     *
     * @param   nms
     *          NMS storage reference
     * @throws  NoSuchMethodException
     *          If something fails
     */
    public static void load(NMSStorage nms) throws NoSuchMethodException {
        if (nms.getMinorVersion() < 9) return;
        CONSTRUCTOR = CLASS.getConstructor(int.class, DataWatcherHelper.DataWatcherSerializer);
        SLOT = nms.getFields(CLASS, int.class).get(0);
        SERIALIZER = nms.getFields(CLASS, DataWatcherHelper.DataWatcherSerializer).get(0);
    }

    /**
     * Converts the object into NMS object
     *
     * @return  NMS object
     * @throws  ReflectiveOperationException
     *          If thrown by reflective operation
     */
    public Object build() throws ReflectiveOperationException {
        if (NMSStorage.getInstance().getMinorVersion() >= 9) {
            return DataWatcherObject.CONSTRUCTOR.newInstance(position, serializer);
        } else {
            return position;
        }
    }
}