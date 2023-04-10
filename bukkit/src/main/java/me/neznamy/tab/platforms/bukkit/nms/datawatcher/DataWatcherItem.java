package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import lombok.Data;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Class representing NMS Data Watcher Item
 */
@Data
public class DataWatcherItem {

    /** NMS Fields */
    public static Class<?> CLASS;
    public static Field TYPE;
    public static Field VALUE;

    /** Instance fields */
    private final DataWatcherObject type;
    private final Object value;

    /**
     * Loads all required Fields
     *
     * @param   nms
     *          NMS storage reference
     */
    public static void load(NMSStorage nms) {
        VALUE = ReflectionUtils.getFields(CLASS, Object.class).get(0);
        if (nms.getMinorVersion() >= 9) {
            TYPE = ReflectionUtils.getFields(CLASS, DataWatcherObject.CLASS).get(0);
        } else {
            TYPE = ReflectionUtils.getFields(CLASS, int.class).get(1);
        }
    }

    /**
     * Returns and instance of this class from given NMS item
     *
     * @param   nmsItem
     *          NMS item
     * @return  instance of this class with same data
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    public static DataWatcherItem fromNMS(Object nmsItem) throws ReflectiveOperationException {
        Object value = VALUE.get(nmsItem);
        DataWatcherObject object;
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            Object nmsObject = TYPE.get(nmsItem);
            object = new DataWatcherObject(DataWatcherObject.SLOT.getInt(nmsObject), DataWatcherObject.SERIALIZER.get(nmsObject));
        } else {
            object = new DataWatcherObject(TYPE.getInt(nmsItem), null);
        }
        return new DataWatcherItem(object, value);
    }
}