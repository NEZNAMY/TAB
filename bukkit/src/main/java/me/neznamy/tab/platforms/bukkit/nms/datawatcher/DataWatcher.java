package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.platforms.bukkit.nms.storage.NMSStorage;

/**
 * A class representing the n.m.s.DataWatcher class to make work with it much easier
 */
public class DataWatcher {

    /** Watched data */
    private final Map<Integer, DataWatcherItem> dataValues = new HashMap<>();

    /** Helper for easier data write */
    private final DataWatcherHelper helper = new DataWatcherHelper(this);

    /**
     * Sets value into data values
     *
     * @param   type
     *          type of value
     * @param   value
     *          value
     */
    public void setValue(DataWatcherObject type, Object value){
        Preconditions.checkNotNull(type, "type");
        Preconditions.checkNotNull(value, "value");
        dataValues.put(type.getPosition(), new DataWatcherItem(type, value));
    }

    /**
     * Removes value by position
     *
     * @param   position
     *          position of value to remove
     */
    public void removeValue(int position) {
        dataValues.remove(position);
    }

    /**
     * Returns item with given position
     *
     * @param   position
     *          position of item
     * @return  item or null if not set
     */
    public DataWatcherItem getItem(int position) {
        return dataValues.get(position);
    }

    /**
     * Returns helper created by this instance
     *
     * @return  data write helper
     */
    public DataWatcherHelper helper() {
        return helper;
    }

    /**
     * Converts the class into an instance of NMS.DataWatcher
     *
     * @return  an instance of NMS.DataWatcher with same data
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    public Object toNMS() throws ReflectiveOperationException {
        NMSStorage nms = NMSStorage.getInstance();
        Object nmsWatcher;
        if (nms.newDataWatcher.getParameterCount() == 1) { //1.7+
            nmsWatcher = nms.newDataWatcher.newInstance(new Object[] {null});
        } else {
            nmsWatcher = nms.newDataWatcher.newInstance();
        }
        for (DataWatcherItem item : dataValues.values()) {
            Object position;
            if (nms.getMinorVersion() >= 9) {
                position = nms.newDataWatcherObject.newInstance(item.getType().getPosition(), item.getType().getSerializer());
            } else {
                position = item.getType().getPosition();
            }
            nms.DataWatcher_REGISTER.invoke(nmsWatcher, position, item.getValue());
            if (nms.is1_19_3Plus()) nms.DataWatcher_markDirty.invoke(nmsWatcher, position);
        }
        return nmsWatcher;
    }

    /**
     * Reads NMS data watcher and returns and instance of this class with same data
     *
     * @param   nmsWatcher
     *          NMS DataWatcher to read
     * @return  an instance of this class with same values
     * @throws  ReflectiveOperationException
     *          if thrown by reflective operation
     */
    @SuppressWarnings("unchecked")
    public static DataWatcher fromNMS(Object nmsWatcher) throws ReflectiveOperationException {
        Preconditions.checkNotNull(nmsWatcher, "nmsWatcher");
        DataWatcher watcher = new DataWatcher();
        List<Object> items = (List<Object>) nmsWatcher.getClass().getMethod("c").invoke(nmsWatcher);
        if (items != null) {
            for (Object watchableObject : items) {
                DataWatcherItem w = DataWatcherItem.fromNMS(watchableObject);
                watcher.setValue(w.getType(), w.getValue());
            }
        }
        return watcher;
    }

    @Override
    public String toString() {
        return String.format("DataWatcher{values=%s}", dataValues);
    }
}