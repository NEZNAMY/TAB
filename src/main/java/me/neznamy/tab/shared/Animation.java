package me.neznamy.tab.shared;

import java.util.List;

/**
 * A class representing an animation from animations.yml
 */
public class Animation {
	
	private String name;
	private String[] messages;
	private int interval;
	
	public Animation(String name, List<String> list, int interval){
		this.name = name;
		this.interval = Shared.errorManager.fixAnimationInterval(name, interval);
		this.messages =  Shared.errorManager.fixAnimationFrames(name, list).toArray(new String[0]);
	}
	public String[] getAllMessages() {
		return messages;
	}
	public String getMessage(){
		return messages[(int) ((System.currentTimeMillis()%(messages.length*interval))/interval)];
	}
	public String getName(){
		return name;
	}
	public int getInterval() {
		return interval;
	}
}