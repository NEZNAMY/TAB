package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

public class DataWatcherItem {
	
	//type of value (position + data type (1.9+))
	private final DataWatcherObject type;
	
	//actual data value
	private final Object value;
	
	/**
	 * Constructs new instance of the object with given parameters
	 * @param type - value type
	 * @param value - value
	 */
	public DataWatcherItem(DataWatcherObject type, Object value){
		this.type = type;
		this.value = value;
	}

	public DataWatcherObject getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}
}
