package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import lombok.Data;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.platforms.bukkit.nms.storage.NMSStorage;

/**
 * Class representing NMS Data Watcher Item
 */
@Data
public class DataWatcherItem {
    
    /** Value type */
    private final DataWatcherObject type;
    
    /** Data value */
    private final Object value;

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
        NMSStorage nms = NMSStorage.getInstance();
        Object value = nms.DataWatcherItem_VALUE.get(nmsItem);
        DataWatcherObject object;
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 9) {
            Object nmsObject = nms.DataWatcherItem_TYPE.get(nmsItem);
            object = new DataWatcherObject(nms.DataWatcherObject_SLOT.getInt(nmsObject), nms.DataWatcherObject_SERIALIZER.get(nmsObject));
        } else {
            object = new DataWatcherObject(nms.DataWatcherItem_TYPE.getInt(nmsItem), null);
        }
        return new DataWatcherItem(object, value);
    }
}