package me.neznamy.tab.bukkit.packets;

import java.util.Optional;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;

public class FakeDataWatcher {
    
    private boolean customNameVisible = true;
    private boolean sneaking;

	public void setCustomNameVisible(boolean flag) {
		customNameVisible = flag;
    }
	public boolean isCustomNameVisible() {
		return customNameVisible;
    }
	public void setSneaking(boolean flag) {
		sneaking = flag;
	}
	public DataWatcher create(String name) {
		byte flag = 0;
		if (sneaking) flag += (byte)2;
		flag += (byte)32;
		DataWatcher datawatcher = new DataWatcher(null);
		if (name == null || name.length() == 0) name = "§r";
		datawatcher.setValue(new DataWatcherObject(0, DataWatcherSerializer.Byte), flag);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			datawatcher.setValue(new DataWatcherObject(2, DataWatcherSerializer.Optional_IChatBaseComponent), Optional.ofNullable(Shared.mainClass.createComponent(name)));
		} else {
			datawatcher.setValue(new DataWatcherObject(2, DataWatcherSerializer.String), name);
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			datawatcher.setValue(new DataWatcherObject(3, DataWatcherSerializer.Boolean), customNameVisible);
		} else {
			datawatcher.setValue(new DataWatcherObject(3, DataWatcherSerializer.Byte), (byte)(customNameVisible?1:0));
		}
		int markerPosition = ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 14 ? 13 : ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 10 ? 11 : 10;
		datawatcher.setValue(new DataWatcherObject(markerPosition, DataWatcherSerializer.Byte), (byte)16);
		return datawatcher;
    }
}