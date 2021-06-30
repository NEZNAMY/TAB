package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

import java.util.Optional;

import me.neznamy.tab.platforms.bukkit.BukkitPacketBuilder;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.IChatBaseComponent;

/**
 * A class to help assigning DataWatcher items as positions often change per-version
 */
public class DataWatcherHelper {

	//position of armor stand flags
	private final int armorStandFlagsPosition = getArmorStandFlagsPosition();

	//original datawatcher to write to
	private DataWatcher data;
	
	//data watcher registry
	private DataWatcherRegistry registry;
	
	/**
	 * Constructs new instance of this class with given parent
	 * @param data - data to write to
	 */
	public DataWatcherHelper(DataWatcher data) {
		this.data = data;
		this.registry = NMSStorage.getInstance().getDataWatcherRegistry();
	}
	
	/**
	 * Returns armor stand flags position based on server version
	 * @return armor stand flags position based on server version
	 */
	private int getArmorStandFlagsPosition() {
		if (NMSStorage.getInstance().getMinorVersion() >= 17) {
			//1.17.x
			return 15;
		} else if (NMSStorage.getInstance().getMinorVersion() >= 15) {
			//1.15.x, 1.16.x
			return 14;
		} else if (NMSStorage.getInstance().getMinorVersion() >= 14) {
			//1.14.x
			return 13;
		} else if (NMSStorage.getInstance().getMinorVersion() >= 10) {
			//1.10.x - 1.13.x
			return 11;
		} else {
			//1.8.1 - 1.9.x
			return 10;
		}
	}
	
	/**
	 * Writes entity byte flags
	 * @param flags - flags to write
	 */
	public void setEntityFlags(byte flags) {
		data.setValue(new DataWatcherObject(0, registry.getByte()), flags);
	}
	
	/**
	 * Writes entity custom name with position based on server version and value depending on client version (RGB or not)
	 * @param customName - target custom name
	 * @param clientVersion - client version
	 */
	public void setCustomName(String customName, ProtocolVersion clientVersion) {
		if (NMSStorage.getInstance().getMinorVersion() >= 13) {
			try {
				data.setValue(new DataWatcherObject(2, registry.getOptionalComponent()), Optional.ofNullable(((BukkitPacketBuilder)TAB.getInstance().getPacketBuilder()).toNMSComponent(IChatBaseComponent.optimizedComponent(customName), clientVersion)));
			} catch (Exception e) {
				TAB.getInstance().getErrorManager().printError("Failed to create component", e);
			}
		} else if (NMSStorage.getInstance().getMinorVersion() >= 8){
			data.setValue(new DataWatcherObject(2, registry.getString()), customName);
		} else {
			//name length is limited to 64 characters on <1.8
			String cutName = (customName.length() > 64 ? customName.substring(0, 64) : customName);
			if (NMSStorage.getInstance().getMinorVersion() >= 6){
				data.setValue(new DataWatcherObject(10, registry.getString()), cutName);
			} else {
				data.setValue(new DataWatcherObject(5, registry.getString()), cutName);
			}
		}
			
	}
	
	/**
	 * Writes custom name visibility boolean
	 * @param visible - if visible or not
	 */
	public void setCustomNameVisible(boolean visible) {
		if (NMSStorage.getInstance().getMinorVersion() >= 9) {
			data.setValue(new DataWatcherObject(3, registry.getBoolean()), visible);
		} else {
			data.setValue(new DataWatcherObject(3, registry.getByte()), (byte)(visible?1:0));
		}
	}
	
	/**
	 * Writes entity health
	 * @param health - health of entity
	 */
	public void setHealth(float health) {
		if (NMSStorage.getInstance().getMinorVersion() >= 6) {
			data.setValue(new DataWatcherObject(6, registry.getFloat()), health);
		} else {
			data.setValue(new DataWatcherObject(16, registry.getInteger()), (int)health);
		}
	}
	
	/**
	 * Writes armor stand flags
	 * @param flags - flags to write
	 */
	public void setArmorStandFlags(byte flags) {
		data.setValue(new DataWatcherObject(armorStandFlagsPosition, registry.getByte()), flags);
	}
}
