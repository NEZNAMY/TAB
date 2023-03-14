package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import lombok.Data;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;

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
        VALUE = nms.getFields(CLASS, Object.class).get(0);
        if (nms.getMinorVersion() >= 9) {
            TYPE = nms.getFields(CLASS, DataWatcherObject.CLASS).get(0);
        } else {
            TYPE = nms.getFields(CLASS, int.class).get(1);
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
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 9) {
            Object nmsObject = TYPE.get(nmsItem);
            object = new DataWatcherObject(DataWatcherObject.SLOT.getInt(nmsObject), DataWatcherObject.SERIALIZER.get(nmsObject));
        } else {
            object = new DataWatcherObject(TYPE.getInt(nmsItem), null);
        }
        return new DataWatcherItem(object, value);
    }
}