package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;

/**
 * Class representing NMS Data Watcher Item
 */
public class DataWatcherItem {
    
    /** Value type */
    private final DataWatcherObject type;
    
    /** Data value */
    private final Object value;
    
    /**
     * Constructs new instance of the object with given parameters
     *
     * @param   type
     *          value type
     * @param   value
     *          value
     */
    public DataWatcherItem(DataWatcherObject type, Object value){
        Preconditions.checkNotNull(type, "type");
        Preconditions.checkNotNull(value, "value");
        this.type = type;
        this.value = value;
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
        NMSStorage nms = NMSStorage.getInstance();
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 9) {
            Object nmsObject = nms.DataWatcherItem_TYPE.get(nmsItem);
            return new DataWatcherItem(new DataWatcherObject(nms.DataWatcherObject_SLOT.getInt(nmsObject), nms.DataWatcherObject_SERIALIZER.get(nmsObject)), nms.DataWatcherItem_VALUE.get(nmsItem));
        } else {
            return new DataWatcherItem(new DataWatcherObject(nms.DataWatcherItem_TYPE.getInt(nmsItem), null), nms.DataWatcherItem_VALUE.get(nmsItem));
        }
    }

    /**
     * Returns {@link #type}
     * @return  {@link #type}
     */
    public DataWatcherObject getType() {
        return type;
    }

    /**
     * Returns {@link #value}
     * @return  {@link #value}
     */
    public Object getValue() {
        return value;
    }
}