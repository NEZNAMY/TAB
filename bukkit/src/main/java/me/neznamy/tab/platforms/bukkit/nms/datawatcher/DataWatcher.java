package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A class representing the n.m.s.DataWatcher class to make work with it much easier
 */
@ToString
public class DataWatcher implements EntityData {

    /** NMS Fields */
    public static Class<?> CLASS;
    private static Constructor<?> CONSTRUCTOR;
    private static Method REGISTER;
    public static Method markDirty;
    public static Method packDirty;
    
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
    public static void load(NMSStorage nms) throws NoSuchMethodException {
        CONSTRUCTOR = ReflectionUtils.getOnlyConstructor(CLASS);
        if (nms.isMojangMapped()) {
            REGISTER = CLASS.getMethod("define", DataWatcherObject.CLASS, Object.class);
        } else if (nms.getMinorVersion() >= 9) {
            REGISTER = ReflectionUtils.getMethod(CLASS, new String[]{"register", "a"}, DataWatcherObject.CLASS, Object.class); // {Bukkit, Bukkit 1.18+}
        } else {
            REGISTER = ReflectionUtils.getMethod(CLASS, new String[]{"func_75682_a", "a"}, int.class, Object.class); // {Thermos 1.7.10, Bukkit}
        }
        if (nms.getMinorVersion() >= 19) {
            if (nms.isMojangMapped()) {
                packDirty = CLASS.getMethod("packDirty");
            } else {
                packDirty = CLASS.getMethod("b");
            }
        }
        if (nms.is1_19_3Plus()) {
            markDirty = ReflectionUtils.getOnlyMethod(CLASS, void.class, DataWatcherObject.CLASS);
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
    public void setValue(@NotNull DataWatcherObject type, @NotNull Object value) {
        dataValues.put(type.getPosition(), new DataWatcherItem(type, value));
    }

    /**
     * Converts the class into an instance of NMS.DataWatcher
     *
     * @return  an instance of NMS.DataWatcher with same data
     */
    @SneakyThrows
    public @NotNull Object build() {
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
    }
}