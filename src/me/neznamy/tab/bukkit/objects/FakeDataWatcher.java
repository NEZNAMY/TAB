package me.neznamy.tab.bukkit.objects;

import java.util.Optional;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.bukkit.packets.DataWatcher;
import me.neznamy.tab.bukkit.packets.DataWatcherObject;
import me.neznamy.tab.bukkit.packets.DataWatcherSerializer;
import me.neznamy.tab.bukkit.packets.NMSClass;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public class FakeDataWatcher {
    
    private boolean customNameVisible = true;
    private String customName = "";
    private boolean sneaking;
    private Player owner;
    
	public FakeDataWatcher(Player owner) {
		this.owner = owner;
	}
	public void setCustomName(String customName) {
        this.customName = customName;
    }
	public void setCustomNameVisible(boolean flag) {
		customNameVisible = flag;
    }
	public boolean isCustomNameVisible() {
		return customNameVisible;
    }
	public void setSneaking(boolean flag) {
		sneaking = flag;
	}
	public DataWatcher get(ITabPlayer other) {
		byte flag = 0;
		if (sneaking) flag += (byte)2;
		flag += (byte)32;
		DataWatcher datawatcher = new DataWatcher(null);
		String name = customName;
		if (me.neznamy.tab.shared.Placeholders.relationalPlaceholders) name = PlaceholderAPI.setRelationalPlaceholders((Player) other.getPlayer(), owner, name);
		if (name == null || name.length() == 0) name = "§r";
		datawatcher.setValue(new DataWatcherObject(0, DataWatcherSerializer.Byte), flag);
		if (NMSClass.versionNumber >= 13) {
			datawatcher.setValue(new DataWatcherObject(2, DataWatcherSerializer.Optional_IChatBaseComponent), Optional.ofNullable(Shared.mainClass.createComponent(name)));
		} else {
			datawatcher.setValue(new DataWatcherObject(2, DataWatcherSerializer.String), name);
		}
		if (NMSClass.versionNumber >= 9) {
			datawatcher.setValue(new DataWatcherObject(3, DataWatcherSerializer.Boolean), customNameVisible);
		} else {
			datawatcher.setValue(new DataWatcherObject(3, DataWatcherSerializer.Byte), (byte)(customNameVisible?1:0));
		}
		return datawatcher;
    }
}