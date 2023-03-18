package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.shared.backend.EntityData;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class representing the n.m.s.DataWatcher class to make work with it much easier
 */
@ToString
public class DataWatcher implements EntityData {

    /** NMS Fields */
    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR;
    public static Method REGISTER;
    public static Method markDirty;
    public static Method packDirty;

    public static Class<?> DataValue;
    public static Field DataValue_POSITION;
    public static Field DataValue_VALUE;
    
    /** Watched data */
    private final Map<Integer, DataWatcherItem> dataValues = new HashMap<>();

    /** Helper for easier data write */
    @Getter private final DataWatcherHelper helper = new DataWatcherHelper(this);

    /**
     * Loads all required Fields and throws Exception if something went wrong
     *
     * @param   nms
     *          NMS storage reference
     */
    public static void load(NMSStorage nms) {
        CONSTRUCTOR = CLASS.getConstructors()[0];
        if (nms.is1_19_3Plus()) {
            markDirty = nms.getMethods(CLASS, void.class, DataWatcherObject.CLASS).get(0);
            DataValue_POSITION = nms.getFields(DataValue, int.class).get(0);
            DataValue_VALUE = nms.getFields(DataValue, Object.class).get(0);
        }
    }
    
    /**
     * Sets value into data values
     *
     * @param   type
     *          type of value
     * @param   value
     *          value
     */
    public void setValue(@NonNull DataWatcherObject type, @NonNull Object value) {
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
     * Converts the class into an instance of NMS.DataWatcher
     *
     * @return  an instance of NMS.DataWatcher with same data
     */
    public Object build() {
        try {
            NMSStorage nms = NMSStorage.getInstance();
            Object nmsWatcher;
            if (CONSTRUCTOR.getParameterCount() == 1) { //1.7+
                nmsWatcher = CONSTRUCTOR.newInstance(new Object[] {null});
            } else {
                nmsWatcher = CONSTRUCTOR.newInstance();
            }
            for (DataWatcherItem item : dataValues.values()) {
                Object nmsObject = item.getType().build();
                REGISTER.invoke(nmsWatcher, nmsObject, item.getValue());
                if (nms.is1_19_3Plus()) markDirty.invoke(nmsWatcher, nmsObject);
            }
            return nmsWatcher;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
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
    public static DataWatcher fromNMS(@NonNull Object nmsWatcher) throws ReflectiveOperationException {
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
}