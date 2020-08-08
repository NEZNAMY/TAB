package me.neznamy.tab.shared;

import java.util.List;

import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.ServerConstant;

public class Animation {
	
	private String name;
	private String[] messages;
	private int interval;
	
	public Animation(String name, List<String> list, int interval){
		this.name = name;
		this.interval = Shared.errorManager.fixAnimationInterval(name, interval);
		this.messages =  Shared.errorManager.fixAnimationFrames(name, list).toArray(new String[0]);
		for (int i=0; i<messages.length; i++) {
			for (Placeholder c : Placeholders.getAllPlaceholders()) {
				if (c instanceof ServerConstant && messages[i].contains(c.getIdentifier())) {
					messages[i] = messages[i].replace(c.getIdentifier(), ((ServerConstant)c).get());
				}
			}
		}
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