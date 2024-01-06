package me.neznamy.tab.platforms.bukkit.entity;

import lombok.*;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.Limitations;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.util.ComponentCache;
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

    private static Object DataWatcherSerializer_BYTE;
    private static Object DataWatcherSerializer_FLOAT;
    private static Object DataWatcherSerializer_STRING;
    private static Object DataWatcherSerializer_OPTIONAL_COMPONENT;
    private static Object DataWatcherSerializer_BOOLEAN;

    private static final int armorStandFlagsPosition = EntityData.getArmorStandFlagsPosition(BukkitReflection.getMinorVersion());

    private static Method ChatSerializer_DESERIALIZE;
    private static final ComponentCache<IChatBaseComponent, Object> componentCache = new ComponentCache<>(1000,
            (component, clientVersion) -> ChatSerializer_DESERIALIZE.invoke(null, component.toString(clientVersion)));

    /** Watched data */
    private final Map<Integer, Item> dataValues = new HashMap<>();

    /**
     * Loads all required Fields and throws Exception if something went wrong.
     *
     * @throws  ReflectiveOperationException
     *          If something goes wrong
     */
    public static void load() throws ReflectiveOperationException {
        int minorVersion = BukkitReflection.getMinorVersion();
        DataWatcher = BukkitReflection.getClass("network.syncher.SynchedEntityData", "network.syncher.DataWatcher", "DataWatcher");
        if (minorVersion >= 7) {
            Class<?> ChatSerializer = BukkitReflection.getClass("network.chat.Component$Serializer",
                    "network.chat.IChatBaseComponent$ChatSerializer", "IChatBaseComponent$ChatSerializer", "ChatSerializer");
            ChatSerializer_DESERIALIZE = ReflectionUtils.getMethods(ChatSerializer, Object.class, String.class).get(0);
            newDataWatcher = DataWatcher.getConstructor(BukkitReflection.getClass("world.entity.Entity", "Entity"));
        } else {
            newDataWatcher = DataWatcher.getConstructor();
        }
        if (minorVersion >= 9) {
            DataWatcherObject = BukkitReflection.getClass("network.syncher.EntityDataAccessor",
                    "network.syncher.DataWatcherObject", "DataWatcherObject");
            Class<?> dataWatcherRegistry = BukkitReflection.getClass("network.syncher.EntityDataSerializers",
                    "network.syncher.DataWatcherRegistry", "DataWatcherRegistry");
            Class<?> dataWatcherSerializer = BukkitReflection.getClass("network.syncher.EntityDataSerializer",
                    "network.syncher.DataWatcherSerializer", "DataWatcherSerializer");
            DataWatcher_register = ReflectionUtils.getMethod(
                    DataWatcher,
                    new String[]{"define", "register", "a", "m_135372_"}, // {Mojang, Bukkit, Bukkit 1.18+, Mohist 1.18.2}
                    DataWatcherObject, Object.class
            );
            newDataWatcherObject = DataWatcherObject.getConstructor(int.class, dataWatcherSerializer);
            DataWatcherSerializer_BYTE = ReflectionUtils.getField(dataWatcherRegistry, "BYTE", "a", "f_135027_").get(null); // Mohist 1.18.2
            DataWatcherSerializer_FLOAT = ReflectionUtils.getField(dataWatcherRegistry, "FLOAT", "c", "f_135029_").get(null); // Mohist 1.18.2
            DataWatcherSerializer_STRING = ReflectionUtils.getField(dataWatcherRegistry, "STRING", "d", "f_135030_").get(null); // Mohist 1.18.2
            if (BukkitReflection.is1_19_3Plus()) {
                DataWatcherSerializer_OPTIONAL_COMPONENT = ReflectionUtils.getField(dataWatcherRegistry, "OPTIONAL_COMPONENT", "g").get(null);
                if (BukkitReflection.is1_19_4Plus()) {
                    DataWatcherSerializer_BOOLEAN = ReflectionUtils.getField(dataWatcherRegistry, "BOOLEAN", "k").get(null);
                } else {
                    DataWatcherSerializer_BOOLEAN = ReflectionUtils.getField(dataWatcherRegistry, "BOOLEAN", "j").get(null);
                }
            } else {
                if (minorVersion >= 13) {
                    DataWatcherSerializer_OPTIONAL_COMPONENT = ReflectionUtils.getField(dataWatcherRegistry,
                            "OPTIONAL_COMPONENT", "f", "f_135032_").get(null); // Mohist 1.18.2
                    DataWatcherSerializer_BOOLEAN = ReflectionUtils.getField(dataWatcherRegistry,
                            "BOOLEAN", "i", "f_135035_").get(null); // Mohist 1.18.2
                } else {
                    DataWatcherSerializer_BOOLEAN = dataWatcherRegistry.getDeclaredField("h").get(null);
                }
            }
        } else {
            DataWatcher_register = ReflectionUtils.getMethod(
                    DataWatcher,
                    new String[]{"func_75682_a", "a"}, int.class, // {Thermos 1.7.10, Bukkit}
                    Object.class
            );
        }
        if (minorVersion >= 19) {
            DataWatcher_packDirty = ReflectionUtils.getMethod(DataWatcher, new String[] {"packDirty", "b"}); // {Mojang, Bukkit}
        }
        if (BukkitReflection.is1_19_3Plus()) {
            DataWatcher_markDirty = ReflectionUtils.getMethods(DataWatcher, void.class, DataWatcherObject).get(0);
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
                    Optional.of(componentCache.get(IChatBaseComponent.optimizedComponent(customName), clientVersion)));
        } else if (BukkitReflection.getMinorVersion() >= 8) {
            setValue(2, DataWatcherSerializer_STRING, customName);
        } else {
            //name length is limited to 64 characters on <1.8
            String cutName = (customName.length() > Limitations.BOSSBAR_NAME_LENGTH_1_7 ?
                    customName.substring(0, Limitations.BOSSBAR_NAME_LENGTH_1_7) : customName);
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

    /**
     * Class for specific data watcher item.
     */
    @RequiredArgsConstructor
    @Getter
    private static class Item {

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
