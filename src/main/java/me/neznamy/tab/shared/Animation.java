package me.neznamy.tab.shared;

import java.util.List;

/**
 * A class representing an animation from animations.yml
 */
public class Animation {
	
	//name of the animations
	private String name;
	
	//all defined messages
	private String[] messages;
	
	//change interval
	private int interval;
	
	public Animation(String name, List<String> list, int interval){
		this.name = name;
		this.interval = Shared.errorManager.fixAnimationInterval(name, interval);
		this.messages =  Shared.errorManager.fixAnimationFrames(name, list).toArray(new String[0]);
	}
	
	/**
	 * Returns all messages
	 * @return all messages
	 */
	public String[] getAllMessages() {
		return messages;
	}
	
	/**
	 * Current message depending on current system time
	 * @return current message
	 */
	public String getMessage(){
		return messages[(int) ((System.currentTimeMillis()%(messages.length*interval))/interval)];
	}
	
	/**
	 * Returns animation's name
	 * @return animation's name
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Returns change interval defined in animations.yml
	 * @return change interval
	 */
	public int getInterval() {
		return interval;
	}
}