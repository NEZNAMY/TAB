package me.neznamy.tab.platforms.bukkit.nms.datawatcher;

/**
 * A class representing the n.m.s.DataWatcherObject class to make work with it much easier
 */
public class DataWatcherObject {

	//position in DataWatcher
	private final int position;
	
	//value class type used since 1.9
	private final Object classType;

	/**
	 * Constructs a new instance of this class with given parameters
	 * @param position - position in DataWatcher
	 * @param classType - value class type
	 */
	public DataWatcherObject(int position, Object classType){
		this.position = position;
		this.classType = classType;
	}

	public int getPosition() {
		return position;
	}

	public Object getClassType() {
		return classType;
	}
}