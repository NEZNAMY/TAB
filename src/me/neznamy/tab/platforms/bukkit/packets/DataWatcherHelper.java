package me.neznamy.tab.platforms.bukkit.packets;

import java.util.Optional;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.packets.IChatBaseComponent;

public class DataWatcherHelper {

	private static final int ARMOR_STAND_BYTEFLAGS_POSITION = getArmorStandFlagsPosition();
	
	private DataWatcher data;
	
	public DataWatcherHelper(DataWatcher data) {
		this.data = data;
	}
	
	private static int getArmorStandFlagsPosition() {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
			//1.15.x, hopefully 1.16.x too
			return 14;
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 14) {
			//1.14.x
			return 13;
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 10) {
			//1.10.x - 1.13.x
			return 11;
		} else {
			//1.8.1 - 1.9.x
			return 10;
		}
	}
	
	public void setEntityFlags(byte flags) {
		data.setValue(new DataWatcherObject(0, DataWatcherSerializer.Byte), flags);
	}
	
	public void setCustomName(String customName, ProtocolVersion clientVersion) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			data.setValue(new DataWatcherObject(2, DataWatcherSerializer.Optional_IChatBaseComponent), Optional.ofNullable(NMSHook.stringToComponent(IChatBaseComponent.optimizedComponent(customName).toString(clientVersion))));
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8){
			data.setValue(new DataWatcherObject(2, DataWatcherSerializer.String), customName);
		} else {
			if (customName.length() > 64) customName = customName.substring(0, 64);
			
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 6){
				data.setValue(new DataWatcherObject(10, DataWatcherSerializer.String), customName);
			} else {
				data.setValue(new DataWatcherObject(5, DataWatcherSerializer.String), customName);
			}
		}
			
	}
	
	public void setCustomNameVisible(boolean visible) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			data.setValue(new DataWatcherObject(3, DataWatcherSerializer.Boolean), visible);
		} else {
			data.setValue(new DataWatcherObject(3, DataWatcherSerializer.Byte), (byte)(visible?1:0));
		}
	}
	
	public void setHealth(float health) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 6) {
			data.setValue(new DataWatcherObject(6, DataWatcherSerializer.Float), health);
		} else {
			data.setValue(new DataWatcherObject(16, DataWatcherSerializer.Integer), (int)health);
		}
	}
	
	public void setArmorStandFlags(byte flags) {
		data.setValue(new DataWatcherObject(ARMOR_STAND_BYTEFLAGS_POSITION, DataWatcherSerializer.Byte), flags);
	}
}
