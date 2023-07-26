package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import lombok.AllArgsConstructor;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A class to help to assign DataWatcher items as positions often change per-version
 */
@AllArgsConstructor
public class DataWatcherHelper {

    /** NMS Fields */
    public static Class<?> DataWatcherRegistry;
    public static Class<?> DataWatcherSerializer;
    public static Object DataWatcherSerializer_BYTE;
    public static Object DataWatcherSerializer_FLOAT;
    public static Object DataWatcherSerializer_STRING;
    public static Object DataWatcherSerializer_OPTIONAL_COMPONENT;
    public static Object DataWatcherSerializer_BOOLEAN;

    /** Instance fields */
    private final int armorStandFlagsPosition = getArmorStandFlagsPosition();
    @NotNull private final DataWatcher data;

    /**
     * Returns armor stand flags position based on server version
     *
     * @return  armor stand flags position based on server version
     */
    private int getArmorStandFlagsPosition() {
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 17) {
            //1.17.x, 1.18.x, 1.19.x
            return 15;
        } else if (TAB.getInstance().getServerVersion().getMinorVersion() >= 15) {
            //1.15.x, 1.16.x
            return 14;
        } else if (TAB.getInstance().getServerVersion().getMinorVersion() >= 14) {
            //1.14.x
            return 13;
        } else if (TAB.getInstance().getServerVersion().getMinorVersion() >= 10) {
            //1.10.x - 1.13.x
            return 11;
        } else {
            //1.8.x - 1.9.x
            return 10;
        }
    }

    public static void load(NMSStorage nms) throws NoSuchFieldException, IllegalAccessException {
        if (nms.getMinorVersion() < 9) return;
        if (nms.isMojangMapped()) {
            DataWatcherSerializer_BYTE = DataWatcherRegistry.getDeclaredField("BYTE").get(null);
            DataWatcherSerializer_FLOAT = DataWatcherRegistry.getDeclaredField("FLOAT").get(null);
            DataWatcherSerializer_STRING = DataWatcherRegistry.getDeclaredField("STRING").get(null);
            DataWatcherSerializer_OPTIONAL_COMPONENT = DataWatcherRegistry.getDeclaredField("OPTIONAL_COMPONENT").get(null);
            DataWatcherSerializer_BOOLEAN = DataWatcherRegistry.getDeclaredField("BOOLEAN").get(null);
        } else {
            DataWatcherSerializer_BYTE = DataWatcherRegistry.getDeclaredField("a").get(null);
            DataWatcherSerializer_FLOAT = DataWatcherRegistry.getDeclaredField("c").get(null);
            DataWatcherSerializer_STRING = DataWatcherRegistry.getDeclaredField("d").get(null);
            if (nms.is1_19_3Plus()) {
                DataWatcherSerializer_OPTIONAL_COMPONENT = DataWatcherRegistry.getDeclaredField("g").get(null);
                if (nms.is1_19_4Plus()) {
                    DataWatcherSerializer_BOOLEAN = DataWatcherRegistry.getDeclaredField("k").get(null);
                } else {
                    DataWatcherSerializer_BOOLEAN = DataWatcherRegistry.getDeclaredField("j").get(null);
                }
            } else {
                if (nms.getMinorVersion() >= 13) {
                    DataWatcherSerializer_OPTIONAL_COMPONENT = DataWatcherRegistry.getDeclaredField("f").get(null);
                    DataWatcherSerializer_BOOLEAN = DataWatcherRegistry.getDeclaredField("i").get(null);
                } else {
                    DataWatcherSerializer_BOOLEAN = DataWatcherRegistry.getDeclaredField("h").get(null);
                }
            }
        }
    }

    /**
     * Writes entity byte flags
     *
     * @param   flags
     *          flags to write
     */
    public void setEntityFlags(byte flags) {
        data.setValue(new DataWatcherObject(0, DataWatcherSerializer_BYTE), flags);
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
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 13) {
            data.setValue(new DataWatcherObject(2, DataWatcherSerializer_OPTIONAL_COMPONENT),
                    Optional.ofNullable(NMSStorage.getInstance().toNMSComponent(IChatBaseComponent.optimizedComponent(customName), clientVersion)));
        } else if (TAB.getInstance().getServerVersion().getMinorVersion() >= 8) {
            data.setValue(new DataWatcherObject(2, DataWatcherSerializer_STRING), customName);
        } else {
            //name length is limited to 64 characters on <1.8
            String cutName = (customName.length() > 64 ? customName.substring(0, 64) : customName);
            if (TAB.getInstance().getServerVersion().getMinorVersion() >= 6) {
                data.setValue(new DataWatcherObject(10, null), cutName);
            } else {
                data.setValue(new DataWatcherObject(5, null), cutName);
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
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            data.setValue(new DataWatcherObject(3, DataWatcherSerializer_BOOLEAN), visible);
        } else {
            data.setValue(new DataWatcherObject(3, null), (byte)(visible?1:0));
        }
    }

    /**
     * Writes entity health
     *
     * @param   health
     *          health of entity
     */
    public void setHealth(float health) {
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 6) {
            data.setValue(new DataWatcherObject(6, DataWatcherSerializer_FLOAT), health);
        } else {
            data.setValue(new DataWatcherObject(16, null), (int)health);
        }
    }

    /**
     * Writes armor stand flags
     *
     * @param   flags
     *          flags to write
     */
    public void setArmorStandFlags(byte flags) {
        data.setValue(new DataWatcherObject(armorStandFlagsPosition, DataWatcherSerializer_BYTE), flags);
    }

    /**
     * Writes wither invulnerable time
     * @param   time
     *          Time, apparently
     */
    public void setWitherInvulnerableTime(int time) {
        if (TAB.getInstance().getServerVersion().getMinorVersion() > 8)
            throw new UnsupportedOperationException("Not supported on 1.9+");
        data.setValue(new DataWatcherObject(20, null), time);
    }
}
