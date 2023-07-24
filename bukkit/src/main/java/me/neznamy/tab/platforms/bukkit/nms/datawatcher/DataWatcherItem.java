package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import lombok.Data;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

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
    @NotNull private final DataWatcherObject type;
    @NotNull private final Object value;

    /**
     * Loads all required Fields
     *
     * @param   nms
     *          NMS storage reference
     */
    public static void load(NMSStorage nms) {
        VALUE = ReflectionUtils.getFields(CLASS, Object.class).get(0);
        if (nms.getMinorVersion() >= 9) {
            TYPE = ReflectionUtils.getOnlyField(CLASS, DataWatcherObject.CLASS);
        } else {
            TYPE = ReflectionUtils.getFields(CLASS, int.class).get(1);
        }
    }
}