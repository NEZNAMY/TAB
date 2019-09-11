package me.neznamy.tab.platforms.bukkit.packets;

public class DataWatcherObject{

	private int position;
	private Object classType;

	public DataWatcherObject(int position, Object classType){
		this.position = position;
		this.classType = classType;
	}
	public int getPosition(){
		return position;
	}
	public Object getClassType(){
		return classType;
	}
}