package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.platforms.bukkit.BukkitPacketBuilder;
import me.neznamy.tab.platforms.bukkit.nms.storage.NMSStorage;
import me.neznamy.tab.shared.TAB;

import java.util.Optional;

/**
 * A class to help to assign DataWatcher items as positions often change per-version
 */
public class DataWatcherHelper {

    /** Position of armor stand flags */
    private final int armorStandFlagsPosition = getArmorStandFlagsPosition();

    /** DataWatcher to write to */
    private final DataWatcher data;

    /** Data Watcher registry reference */
    private final DataWatcherRegistry registry;

    /**
     * Constructs new instance of this class with given parent
     *
     * @param   data
     *          data to write to
     */
    public DataWatcherHelper(DataWatcher data) {
        this.data = data;
        this.registry = NMSStorage.getInstance().getDataWatcherRegistry();
    }

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
            //1.8.1 - 1.9.x
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
        data.setValue(new DataWatcherObject(0, registry.getTypeByte()), flags);
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
            data.setValue(new DataWatcherObject(2, registry.getTypeOptionalComponent()), Optional.ofNullable(((BukkitPacketBuilder)TAB.getInstance().getPlatform().getPacketBuilder()).toNMSComponent(IChatBaseComponent.optimizedComponent(customName), clientVersion)));
        } else if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 8){
            data.setValue(new DataWatcherObject(2, registry.getTypeString()), customName);
        } else {
            //name length is limited to 64 characters on <1.8
            String cutName = (customName.length() > 64 ? customName.substring(0, 64) : customName);
            if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 6){
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
            data.setValue(new DataWatcherObject(3, registry.getTypeBoolean()), visible);
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
            data.setValue(new DataWatcherObject(6, registry.getTypeFloat()), health);
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
        data.setValue(new DataWatcherObject(armorStandFlagsPosition, registry.getTypeByte()), flags);
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
