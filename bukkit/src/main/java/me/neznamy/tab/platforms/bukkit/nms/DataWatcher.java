package me.neznamy.tab.platforms.bukkit.nms;

import lombok.*;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A class representing the n.m.s.DataWatcher class to make work with it much easier
 */
@ToString
public class DataWatcher implements EntityData {

    /** NMS Fields */
    public static Class<?> DataWatcher;
    private static Constructor<?> newDataWatcher;
    private static Method DataWatcher_register;
    private static Method DataWatcher_markDirty;
    public static Method DataWatcher_packDirty;

    private static Class<?> DataWatcherObject;
    private static Constructor<?> newDataWatcherObject;

    private static Class<?> DataWatcherRegistry;
    private static Class<?> DataWatcherSerializer;
    private static Object DataWatcherSerializer_BYTE;
    private static Object DataWatcherSerializer_FLOAT;
    private static Object DataWatcherSerializer_STRING;
    private static Object DataWatcherSerializer_OPTIONAL_COMPONENT;
    private static Object DataWatcherSerializer_BOOLEAN;

    private static final int armorStandFlagsPosition = getArmorStandFlagsPosition();
    
    /** Watched data */
    private final Map<Integer, Item> dataValues = new HashMap<>();

    /**
     * Returns armor stand flags position based on server version
     *
     * @return  armor stand flags position based on server version
     */
    private static int getArmorStandFlagsPosition() {
        if (BukkitReflection.getMinorVersion() >= 17) {
            //1.17.x, 1.18.x, 1.19.x, 1.20.x
            return 15;
        } else if (BukkitReflection.getMinorVersion() >= 15) {
            //1.15.x, 1.16.x
            return 14;
        } else if (BukkitReflection.getMinorVersion() >= 14) {
            //1.14.x
            return 13;
        } else if (BukkitReflection.getMinorVersion() >= 10) {
            //1.10.x - 1.13.x
            return 11;
        } else {
            //1.8.x - 1.9.x
            return 10;
        }
    }
    
    /**
     * Loads all required Fields and throws Exception if something went wrong
     */
    public static void load() throws ReflectiveOperationException {
        int minorVersion = BukkitReflection.getMinorVersion();
        if (BukkitReflection.isMojangMapped()) {
            DataWatcher = Class.forName("net.minecraft.network.syncher.SynchedEntityData");
            DataWatcherObject = Class.forName("net.minecraft.network.syncher.EntityDataAccessor");
            DataWatcherRegistry = Class.forName("net.minecraft.network.syncher.EntityDataSerializers");
            DataWatcherSerializer = Class.forName("net.minecraft.network.syncher.EntityDataSerializer");
        } else if (minorVersion >= 17) {
            DataWatcher = Class.forName("net.minecraft.network.syncher.DataWatcher");
            DataWatcherObject = Class.forName("net.minecraft.network.syncher.DataWatcherObject");
            DataWatcherRegistry = Class.forName("net.minecraft.network.syncher.DataWatcherRegistry");
            DataWatcherSerializer = Class.forName("net.minecraft.network.syncher.DataWatcherSerializer");
        } else {
            DataWatcher = BukkitReflection.getLegacyClass("DataWatcher");
            if (minorVersion >= 9) {
                DataWatcherObject = BukkitReflection.getLegacyClass("DataWatcherObject");
                DataWatcherRegistry = BukkitReflection.getLegacyClass("DataWatcherRegistry");
                DataWatcherSerializer = BukkitReflection.getLegacyClass("DataWatcherSerializer");
            }
        }
        newDataWatcher = ReflectionUtils.getOnlyConstructor(DataWatcher);
        if (minorVersion >= 9) {
            DataWatcher_register = ReflectionUtils.getMethod(
                    DataWatcher,
                    new String[]{"define", "register", "a", "m_135372_"}, // {Bukkit 1.20.2+, Bukkit, Bukkit 1.18+, Mohist 1.18.2}
                    DataWatcherObject, Object.class
            );
        } else {
            DataWatcher_register = ReflectionUtils.getMethod(DataWatcher, new String[]{"func_75682_a", "a"}, int.class, Object.class); // {Thermos 1.7.10, Bukkit}
        }
        if (minorVersion >= 19) {
            DataWatcher_packDirty = ReflectionUtils.getMethod(DataWatcher, new String[] {"packDirty", "b"}); // {Mojang | 1.20.2+, 1.20.2-}
        }
        if (BukkitReflection.is1_19_3Plus()) {
            DataWatcher_markDirty = ReflectionUtils.getMethods(DataWatcher, void.class, DataWatcherObject).get(0);
        }
        if (minorVersion >= 9) {
            newDataWatcherObject = DataWatcherObject.getConstructor(int.class, DataWatcherSerializer);
            DataWatcherSerializer_BYTE = ReflectionUtils.getField(DataWatcherRegistry, "BYTE", "a", "f_135027_").get(null); // Mohist 1.18.2
            DataWatcherSerializer_FLOAT = ReflectionUtils.getField(DataWatcherRegistry, "FLOAT", "c", "f_135029_").get(null); // Mohist 1.18.2
            DataWatcherSerializer_STRING = ReflectionUtils.getField(DataWatcherRegistry, "STRING", "d", "f_135030_").get(null); // Mohist 1.18.2
            if (BukkitReflection.is1_19_3Plus()) {
                DataWatcherSerializer_OPTIONAL_COMPONENT = ReflectionUtils.getField(DataWatcherRegistry, "OPTIONAL_COMPONENT", "g").get(null);
                if (BukkitReflection.is1_19_4Plus()) {
                    DataWatcherSerializer_BOOLEAN = ReflectionUtils.getField(DataWatcherRegistry, "BOOLEAN", "k").get(null);
                } else {
                    DataWatcherSerializer_BOOLEAN = ReflectionUtils.getField(DataWatcherRegistry, "BOOLEAN", "j").get(null);
                }
            } else {
                if (minorVersion >= 13) {
                    DataWatcherSerializer_OPTIONAL_COMPONENT = ReflectionUtils.getField(DataWatcherRegistry,
                            "OPTIONAL_COMPONENT", "f", "f_135032_").get(null); // Mohist 1.18.2
                    DataWatcherSerializer_BOOLEAN = ReflectionUtils.getField(DataWatcherRegistry,
                            "BOOLEAN", "i", "f_135035_").get(null); // Mohist 1.18.2
                } else {
                    DataWatcherSerializer_BOOLEAN = DataWatcherRegistry.getDeclaredField("h").get(null);
                }
            }
        }
    }
    
    /**
     * Sets value into data values
     *
     * @param   position
     *          data position
     * @param   serializer
     *          Object serializer (1.9+)
     * @param   value
     *          value
     */
    public void setValue(int position, @Nullable Object serializer, @NotNull Object value) {
        dataValues.put(position, new Item(position, serializer, value));
    }

    /**
     * Writes entity byte flags
     *
     * @param   flags
     *          flags to write
     */
    public void setEntityFlags(byte flags) {
        setValue(0, DataWatcherSerializer_BYTE, flags);
    }

    /**
     * Writes entity custom name with position based on server version and value depending on client version (RGB or not)
     *
     * @param   customName
     *          target custom name
     * @param   clientVersion
     *          client version
     */
    public void setCustomName(@NotNull String customName, @NotNull ProtocolVersion clientVersion) {
        if (BukkitReflection.getMinorVersion() >= 13) {
            setValue(2, DataWatcherSerializer_OPTIONAL_COMPONENT,
                    Optional.of(TAB.getInstance().getPlatform().toComponent(IChatBaseComponent.optimizedComponent(customName), clientVersion)));
        } else if (BukkitReflection.getMinorVersion() >= 8) {
            setValue(2, DataWatcherSerializer_STRING, customName);
        } else {
            //name length is limited to 64 characters on <1.8
            String cutName = (customName.length() > 64 ? customName.substring(0, 64) : customName);
            if (BukkitReflection.getMinorVersion() >= 6) {
                setValue(10, null, cutName);
            } else {
                setValue(5, null, cutName);
            }
        }
    }

    /**
     * Writes custom name visibility boolean
     *
     * @param   visible
     *          if visible or not
     */
    public void setCustomNameVisible(boolean visible) {
        if (BukkitReflection.getMinorVersion() >= 9) {
            setValue(3, DataWatcherSerializer_BOOLEAN, visible);
        } else {
            setValue(3, null, (byte)(visible?1:0));
        }
    }

    /**
     * Writes entity health
     *
     * @param   health
     *          health of entity
     */
    public void setHealth(float health) {
        if (BukkitReflection.getMinorVersion() >= 6) {
            setValue(6, DataWatcherSerializer_FLOAT, health);
        } else {
            setValue(16, null, (int)health);
        }
    }

    /**
     * Writes armor stand flags
     *
     * @param   flags
     *          flags to write
     */
    public void setArmorStandFlags(byte flags) {
        setValue(armorStandFlagsPosition, DataWatcherSerializer_BYTE, flags);
    }

    /**
     * Writes wither invulnerable time
     * @param   time
     *          Time, apparently
     */
    public void setWitherInvulnerableTime(int time) {
        if (BukkitReflection.getMinorVersion() > 8)
            throw new UnsupportedOperationException("Not supported on 1.9+");
        setValue(20, null, time);
    }

    /**
     * Converts the class into an instance of NMS.DataWatcher
     *
     * @return  an instance of NMS.DataWatcher with same data
     */
    @SneakyThrows
    public @NotNull Object build() {
        Object nmsWatcher;
        if (newDataWatcher.getParameterCount() == 1) { //1.7+
            nmsWatcher = newDataWatcher.newInstance(new Object[] {null});
        } else {
            nmsWatcher = newDataWatcher.newInstance();
        }
        for (Item item : dataValues.values()) {
            Object nmsObject = item.createObject();
            DataWatcher_register.invoke(nmsWatcher, nmsObject, item.getValue());
            if (BukkitReflection.is1_19_3Plus()) DataWatcher_markDirty.invoke(nmsWatcher, nmsObject);
        }
        return nmsWatcher;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Item {

        /** Item position */
        private final int position;

        /** Item serializer (1.9+) */
        @Nullable
        private final Object serializer;

        /** Item value */
        @NotNull
        private final Object value;

        /**
         * Creates data watcher object with position and serializer
         *
         * @return  NMS data watcher object
         */
        @SneakyThrows
        public Object createObject() {
            if (BukkitReflection.getMinorVersion() >= 9) {
                return newDataWatcherObject.newInstance(position, serializer);
            } else {
                return position;
            }
        }
    }
}
