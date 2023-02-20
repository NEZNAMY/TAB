package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import lombok.AllArgsConstructor;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;

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
    private final DataWatcher data;

    /**
     * Returns armor stand flags position based on server version
     *
     * @return  armor stand flags position based on server version
     */
    private int getArmorStandFlagsPosition() {
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 17) {
            //1.17.x, 1.18.x, 1.19.x
            return 15;
        } else if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 15) {
            //1.15.x, 1.16.x
            return 14;
        } else if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 14) {
            //1.14.x
            return 13;
        } else if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 10) {
            //1.10.x - 1.13.x
            return 11;
        } else {
            //1.8.x - 1.9.x
            return 10;
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
    public void setCustomName(String customName, ProtocolVersion clientVersion) {
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 13) {
            data.setValue(new DataWatcherObject(2, DataWatcherSerializer_OPTIONAL_COMPONENT),
                    Optional.ofNullable(NMSStorage.getInstance().toNMSComponent(IChatBaseComponent.optimizedComponent(customName), clientVersion)));
        } else if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 8) {
            data.setValue(new DataWatcherObject(2, DataWatcherSerializer_STRING), customName);
        } else {
            //name length is limited to 64 characters on <1.8
            String cutName = (customName.length() > 64 ? customName.substring(0, 64) : customName);
            if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 6) {
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
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 9) {
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
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 6) {
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
