package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.Nullable;

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
    @Nullable private final Object serializer;

    /**
     * Loads all required Fields and throws Exception if something went wrong
     *
     * @throws  NoSuchMethodException
     *          If something fails
     */
    public static void load() throws NoSuchMethodException {
        CONSTRUCTOR = CLASS.getConstructor(int.class, DataWatcherHelper.DataWatcherSerializer);
        SLOT = ReflectionUtils.getOnlyField(CLASS, int.class);
        SERIALIZER = ReflectionUtils.getOnlyField(CLASS, DataWatcherHelper.DataWatcherSerializer);
    }

    /**
     * Converts the object into NMS object
     *
     * @return  NMS object
     */
    @SneakyThrows
    public Object build() {
        if (NMSStorage.getInstance().getMinorVersion() >= 9) {
            return DataWatcherObject.CONSTRUCTOR.newInstance(position, serializer);
        } else {
            return position;
        }
    }
}